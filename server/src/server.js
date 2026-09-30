import http from 'node:http';
import { URL } from 'node:url';
import pg from 'pg';

const { Pool } = pg;
const port = Number(process.env.PORT || 3000);
const timeoutMs = Number(process.env.PROVIDER_TIMEOUT_MS || 25000);
const baleToken = process.env.BALE_BOT_TOKEN || '';
const baleChatId = process.env.BALE_ADMIN_CHAT_ID || '';
const baleRateLimit = Number(process.env.BALE_MAX_REQUESTS_PER_MINUTE || 20);
const databaseUrl = process.env.DATABASE_URL || '';
const dbSslDisabled = process.env.PGSSL_DISABLE === 'true';

const pool = databaseUrl
  ? new Pool({
      connectionString: databaseUrl,
      max: 5,
      idleTimeoutMillis: 30_000,
      connectionTimeoutMillis: 10_000,
      ssl: dbSslDisabled ? false : { rejectUnauthorized: false },
    })
  : null;

const providerMap = {
  violations: 'ITOLL',
  tolls: 'ITOLL',
  insurance: 'BIMEH',
  tax: 'ETAX',
  technical_inspection: 'ITOLL',
};

const rateWindow = new Map();

function json(res, status, body) {
  const payload = JSON.stringify(body);
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'Accept, Content-Type, Authorization, X-Device-Key',
    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
    'Cache-Control': 'no-store',
  });
  res.end(payload);
}

function validPlate(value) {
  const normalized = String(value || '').replace(/\s+/g, '');
  return /^(\d{2})[بپتثجچحخدذرزژسشصضطظعغفقکگلمنوهی](\d{3})(\d{2})$/.test(normalized);
}

function validDeviceId(value) {
  return /^[a-f0-9]{64}$/.test(String(value || '').trim());
}

function clientIp(req) {
  const forwarded = req.headers['x-forwarded-for'];
  return String(forwarded || req.socket.remoteAddress || 'unknown')
    .split(',')[0]
    .trim();
}

function rateLimited(req) {
  const now = Date.now();
  const key = clientIp(req);
  const item = rateWindow.get(key) || { count: 0, startedAt: now };
  if (now - item.startedAt >= 60_000) {
    item.count = 0;
    item.startedAt = now;
  }
  item.count += 1;
  rateWindow.set(key, item);
  return item.count > baleRateLimit;
}

async function readJsonBody(req, maxBytes = 8 * 1024 * 1024) {
  return await new Promise((resolve, reject) => {
    let size = 0;
    const chunks = [];
    let settled = false;
    req.on('data', (chunk) => {
      if (settled) return;
      size += chunk.length;
      if (size > maxBytes) {
        settled = true;
        reject(new Error('PAYLOAD_TOO_LARGE'));
        req.destroy();
        return;
      }
      chunks.push(chunk);
    });
    req.on('end', () => {
      if (settled) return;
      try {
        const text = Buffer.concat(chunks).toString('utf8');
        resolve(text ? JSON.parse(text) : {});
      } catch (error) {
        reject(error);
      }
    });
    req.on('error', (error) => {
      if (!settled) reject(error);
    });
  });
}

function envFor(provider) {
  return {
    ITOLL: { url: process.env.ITOLL_API_URL, key: process.env.ITOLL_API_KEY },
    BIMEH: { url: process.env.BIMEH_API_URL, key: process.env.BIMEH_API_KEY },
    ETAX: { url: process.env.ETAX_API_URL, key: process.env.ETAX_API_KEY },
  }[provider] || {};
}

async function callProvider(provider, kind, plate) {
  const config = envFor(provider);
  if (!config.url) {
    return {
      ok: false,
      status: 503,
      message:
        `Provider ${provider} is not configured. ` +
        'Use the provider authorized API/B2B endpoint and keep its key on Railway.',
    };
  }

  const base = new URL(config.url);
  const endpoint = new URL(`/inquiry/${encodeURIComponent(kind)}`, base);
  endpoint.searchParams.set('plate', plate);

  const headers = { accept: 'application/json' };
  if (config.key) headers.authorization = `Bearer ${config.key}`;

  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);
  try {
    const response = await fetch(endpoint, { headers, signal: controller.signal });
    const text = await response.text();
    let body;
    try {
      body = JSON.parse(text);
    } catch {
      body = null;
    }
    if (!response.ok) {
      return {
        ok: false,
        status: response.status,
        message: body?.message || `Provider HTTP ${response.status}`,
      };
    }
    if (!body || body.ok !== true) {
      return {
        ok: false,
        status: 502,
        message: body?.message || 'Provider returned an invalid response.',
      };
    }
    return {
      ok: true,
      status: 200,
      kind,
      plate,
      data: body.data ?? {},
      message: body.message ?? null,
      provider,
    };
  } catch (error) {
    return {
      ok: false,
      status: 502,
      message:
        error?.name === 'AbortError'
          ? 'Provider timeout.'
          : `Provider connection failed: ${error?.message || error}`,
    };
  } finally {
    clearTimeout(timer);
  }
}

async function baleRequest(method, payload) {
  if (!baleToken || !baleChatId) {
    return {
      ok: false,
      status: 503,
      message: 'Bale integration is not configured on Railway.',
    };
  }

  const response = await fetch(`https://tapi.bale.ai/bot${baleToken}/${method}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json; charset=utf-8' },
    body: JSON.stringify(payload),
  });

  const text = await response.text();
  let body;
  try {
    body = JSON.parse(text);
  } catch {
    body = null;
  }

  if (!response.ok || body?.ok !== true) {
    return {
      ok: false,
      status: response.status || 502,
      message: body?.description || body?.message || 'Bale request failed.',
    };
  }

  return {
    ok: true,
    status: 200,
    message_id: body?.result?.message_id?.toString() || '',
  };
}

async function initDatabase() {
  if (!pool) {
    console.warn('DATABASE_URL is not configured; cloud sync will be unavailable.');
    return;
  }
  await pool.query(`
    CREATE TABLE IF NOT EXISTS app_device_snapshots (
      device_id VARCHAR(64) PRIMARY KEY,
      schema_version INTEGER NOT NULL DEFAULT 1,
      snapshot JSONB NOT NULL,
      updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    )
  `);
  await pool.query(`
    CREATE INDEX IF NOT EXISTS idx_app_device_snapshots_updated_at
    ON app_device_snapshots(updated_at DESC)
  `);
  console.log('PostgreSQL cloud storage is ready.');
}

function requireDeviceKey(req, deviceId) {
  const header = String(req.headers['x-device-key'] || '').trim();
  return validDeviceId(deviceId) && header === deviceId;
}

async function upsertSnapshot(body) {
  const deviceId = String(body?.device_id || '').trim();
  if (!requireDeviceKeyFromBody(body)) {
    return { ok: false, status: 400, message: 'شناسه دستگاه نامعتبر است.' };
  }
  if (!pool) {
    return { ok: false, status: 503, message: 'ذخیره‌سازی ابری Railway هنوز فعال نشده است.' };
  }

  const snapshot = body?.snapshot;
  if (!snapshot || typeof snapshot !== 'object' || Array.isArray(snapshot)) {
    return { ok: false, status: 400, message: 'داده همگام‌سازی نامعتبر است.' };
  }

  const schemaVersion = Number(body?.schema_version || snapshot?.schema_version || 1);
  await pool.query(
    `
      INSERT INTO app_device_snapshots (device_id, schema_version, snapshot, updated_at)
      VALUES ($1, $2, $3::jsonb, CURRENT_TIMESTAMP)
      ON CONFLICT (device_id) DO UPDATE SET
        schema_version = EXCLUDED.schema_version,
        snapshot = EXCLUDED.snapshot,
        updated_at = CURRENT_TIMESTAMP
    `,
    [deviceId, schemaVersion, JSON.stringify(snapshot)],
  );
  return { ok: true, message: 'داده‌ها با موفقیت در Railway ذخیره شدند.', timestamp: Date.now() };
}

function requireDeviceKeyFromBody(body) {
  const deviceId = String(body?.device_id || '').trim();
  return validDeviceId(deviceId);
}

async function getSnapshot(deviceId) {
  if (!validDeviceId(deviceId)) {
    return { ok: false, status: 400, message: 'شناسه دستگاه نامعتبر است.' };
  }
  if (!pool) {
    return { ok: false, status: 503, message: 'ذخیره‌سازی ابری Railway هنوز فعال نشده است.' };
  }
  const result = await pool.query(
    'SELECT schema_version, snapshot, updated_at FROM app_device_snapshots WHERE device_id = $1 LIMIT 1',
    [deviceId],
  );
  if (!result.rows.length) {
    return { ok: true, snapshot: null, message: 'نسخه ابری برای این دستگاه وجود ندارد.' };
  }
  const row = result.rows[0];
  return {
    ok: true,
    schema_version: row.schema_version,
    snapshot: row.snapshot,
    updated_at: row.updated_at,
  };
}

const server = http.createServer(async (req, res) => {
  if (req.method === 'OPTIONS') {
    res.writeHead(204, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Headers': 'Accept, Content-Type, Authorization, X-Device-Key',
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
    });
    res.end();
    return;
  }

  try {
    const url = new URL(req.url || '/', `http://${req.headers.host || 'localhost'}`);

    if (req.method === 'GET' && url.pathname === '/') {
      json(res, 200, {
        ok: true,
        service: 'sookhtman-api',
        message: 'سرویس مدیریت سوخت و استعلام خودرو فعال است.',
        health: '/api/health',
        sync: '/api/v1/sync',
        bale: '/api/v1/bale/message',
      });
      return;
    }

    if (req.method === 'GET' && url.pathname === '/api/health') {
      json(res, 200, {
        ok: true,
        service: 'sookhtman-api',
        database: Boolean(pool),
        providers: {
          itoll: Boolean(process.env.ITOLL_API_URL),
          bimeh: Boolean(process.env.BIMEH_API_URL),
          etax: Boolean(process.env.ETAX_API_URL),
        },
        bale: Boolean(baleToken && baleChatId),
        time: new Date().toISOString(),
      });
      return;
    }

    if (req.method === 'GET' && url.pathname === '/api/inquiry/config') {
      json(res, 200, {
        ok: true,
        available: Object.fromEntries(
          Object.entries(providerMap).map(([kind, provider]) => [kind, Boolean(envFor(provider).url)]),
        ),
        workflow: 'BALE_ADMIN_FORM',
      });
      return;
    }

    const inquiryMatch = url.pathname.match(/^\/(?:api\/)?inquiry\/([a-z_]+)$/);
    if (req.method === 'GET' && inquiryMatch) {
      const kind = inquiryMatch[1];
      if (!providerMap[kind]) {
        json(res, 404, { ok: false, message: 'نوع استعلام ناشناخته است.' });
        return;
      }

      const plate = String(url.searchParams.get('plate') || '');
      if (!validPlate(plate)) {
        json(res, 400, { ok: false, message: 'پلاک نامعتبر است.' });
        return;
      }

      const provider = providerMap[kind];
      const result = await callProvider(provider, kind, plate);
      json(res, result.ok ? 200 : result.status || 502, result);
      return;
    }

    if (req.method === 'GET' && url.pathname === '/api/v1/sync/latest') {
      const deviceId = String(url.searchParams.get('device_id') || '').trim();
      if (!requireDeviceKey(req, deviceId)) {
        json(res, 401, { ok: false, message: 'دسترسی همگام‌سازی مجاز نیست.' });
        return;
      }
      const result = await getSnapshot(deviceId);
      json(res, result.ok ? 200 : result.status || 502, result);
      return;
    }

    if (req.method === 'POST' && url.pathname === '/api/v1/sync') {
      const body = await readJsonBody(req, 8 * 1024 * 1024);
      const deviceId = String(body?.device_id || '').trim();
      if (!requireDeviceKey(req, deviceId)) {
        json(res, 401, { ok: false, message: 'دسترسی همگام‌سازی مجاز نیست.' });
        return;
      }
      const result = await upsertSnapshot(body);
      json(res, result.ok ? 200 : result.status || 502, result);
      return;
    }

    if (req.method === 'POST' && url.pathname === '/api/v1/bale/message') {
      if (rateLimited(req)) {
        json(res, 429, { ok: false, message: 'تعداد درخواست‌ها بیش از حد مجاز است.' });
        return;
      }

      const body = await readJsonBody(req, 128 * 1024);
      const text = String(body?.text || '').trim();
      if (!text) {
        json(res, 400, { ok: false, message: 'متن پیام خالی است.' });
        return;
      }

      const result = await baleRequest('sendMessage', {
        chat_id: baleChatId,
        text,
      });
      json(res, result.ok ? 200 : result.status || 502, result);
      return;
    }

    if (req.method === 'POST' && url.pathname === '/api/v1/bale/photo') {
      if (rateLimited(req)) {
        json(res, 429, { ok: false, message: 'تعداد درخواست‌ها بیش از حد مجاز است.' });
        return;
      }

      const body = await readJsonBody(req, 8 * 1024 * 1024);
      const caption = String(body?.caption || '').trim();
      const fileName = String(body?.file_name || 'support_receipt.jpg');
      const base64 = String(body?.photo_base64 || '');
      if (!caption || !base64) {
        json(res, 400, { ok: false, message: 'اطلاعات فیش ناقص است.' });
        return;
      }

      const bytes = Buffer.from(base64, 'base64');
      if (!bytes.length || bytes.length > 5 * 1024 * 1024) {
        json(res, 413, { ok: false, message: 'حجم تصویر مجاز نیست.' });
        return;
      }

      const form = new FormData();
      form.append('chat_id', baleChatId);
      form.append('caption', caption);
      form.append('photo', new Blob([bytes], { type: 'image/jpeg' }), fileName || 'support_receipt.jpg');

      const response = await fetch(`https://tapi.bale.ai/bot${baleToken}/sendPhoto`, {
        method: 'POST',
        body: form,
      });
      const text = await response.text();
      let result;
      try {
        result = JSON.parse(text);
      } catch {
        result = null;
      }

      if (!response.ok || result?.ok !== true) {
        json(res, response.status || 502, {
          ok: false,
          message: result?.description || 'ارسال تصویر به بله انجام نشد.',
        });
        return;
      }

      json(res, 200, {
        ok: true,
        message_id: result?.result?.message_id?.toString() || '',
      });
      return;
    }

    const requestStatusMatch = url.pathname.match(/^\/api\/v1\/requests\/(\d+)\/status$/);
    if (req.method === 'GET' && requestStatusMatch) {
      json(res, 200, {
        ok: true,
        request_id: Number(requestStatusMatch[1]),
        status: 'در انتظار تایید مدیر',
      });
      return;
    }

    json(res, 404, { ok: false, message: 'مسیر پیدا نشد.' });
  } catch (error) {
    console.error('Unhandled request error:', error);
    const status = error?.message === 'PAYLOAD_TOO_LARGE' ? 413 : 500;
    json(res, status, {
      ok: false,
      message: status === 413 ? 'حجم درخواست بیش از حد مجاز است.' : 'خطای داخلی سرور.',
    });
  }
});

await initDatabase();

server.listen(port, '0.0.0.0', () => {
  console.log(`SookhtMan API listening on :${port}`);
});
