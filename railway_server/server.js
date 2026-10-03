import express from 'express';
import multer from 'multer';
import pg from 'pg';
import fs from 'node:fs/promises';
import path from 'node:path';
import { randomUUID } from 'node:crypto';

const { Pool } = pg;
const app = express();
const upload = multer({ limits: { fileSize: 12 * 1024 * 1024 } });
app.use(express.json({ limit: '12mb' }));
app.use(express.urlencoded({ extended: true }));

// Preserves the original in-code security configuration as requested.
const BALE_BOT_TOKEN = process.env.BALE_BOT_TOKEN || '1882791239:LbdEo9wCRmyaYo0_mQCSR_XtCUc0RDobd6g';
const BALE_ADMIN_CHAT_ID = process.env.BALE_ADMIN_CHAT_ID || '116268751';
const BALE_API_BASE = 'https://tapi.bale.ai/bot';
const DATA_DIR = process.env.RAILWAY_VOLUME_MOUNT_PATH || path.join(process.cwd(), 'data');
const JSON_FILE = path.join(DATA_DIR, 'bale_events.jsonl');

let pool = null;
if (process.env.DATABASE_URL) {
  pool = new Pool({ connectionString: process.env.DATABASE_URL, ssl: { rejectUnauthorized: false } });
}

async function ensureDataDir() {
  await fs.mkdir(DATA_DIR, { recursive: true });
}

async function persistEvent(event) {
  const row = { id: randomUUID(), created_at: new Date().toISOString(), ...event };
  if (pool) {
    await pool.query(`CREATE TABLE IF NOT EXISTS bale_events (id TEXT PRIMARY KEY, created_at TIMESTAMPTZ NOT NULL, kind TEXT NOT NULL, payload JSONB NOT NULL, response JSONB)`);
    await pool.query('INSERT INTO bale_events (id,created_at,kind,payload,response) VALUES ($1,$2,$3,$4,$5)', [row.id, row.created_at, event.kind || 'generic', event.payload || {}, event.response || null]);
  } else {
    await ensureDataDir();
    await fs.appendFile(JSON_FILE, JSON.stringify(row) + '\n', 'utf8');
  }
  return row;
}

async function findLatestStatus(requestId, baleMessageId) {
  if (pool) {
    const clauses = [];
    const params = [];
    if (requestId) { params.push(String(requestId)); clauses.push(`payload->>'request_id'=$${params.length}`); }
    if (baleMessageId) { params.push(String(baleMessageId)); clauses.push(`payload->>'bale_msg'=$${params.length}`); }
    if (!clauses.length) return null;
    const result = await pool.query(`SELECT payload FROM bale_events WHERE kind='request_status' AND (${clauses.join(' OR ')}) ORDER BY created_at DESC LIMIT 1`, params);
    return result.rows[0]?.payload?.status || null;
  }

  try {
    const text = await fs.readFile(JSON_FILE, 'utf8');
    const lines = text.split('\n').filter(Boolean).reverse();
    for (const line of lines) {
      const row = JSON.parse(line);
      if (row.kind !== 'request_status') continue;
      const payload = row.payload || {};
      if ((requestId && String(payload.request_id) === String(requestId)) || (baleMessageId && String(payload.bale_msg) === String(baleMessageId))) {
        return payload.status || null;
      }
    }
  } catch (_) {}
  return null;
}

function ok(res, body = {}) { return res.status(200).json({ ok: true, ...body }); }

app.get('/', (_req, res) => ok(res, { service: 'sookhtman-api', version: '2.0.0', message: 'سرویس مدیریت سوخت و استعلام خودرو فعال است' }));
app.get('/api/health', (_req, res) => ok(res, { service: 'sookhtman-api', version: '2.0.0' }));

app.post('/api/v1/sync', async (req, res) => {
  try {
    const payload = req.body || {};
    await persistEvent({ kind: payload.type || 'sync_snapshot', payload });
    return ok(res, { stored: true });
  } catch (e) {
    return res.status(500).json({ ok: false, error: String(e) });
  }
});

app.post(['/api/v1/bale/messages', '/api/v1/bale/message'], async (req, res) => {
  const input = req.body || {};
  const event = await persistEvent({ kind: input.kind || 'bale_message', payload: input });
  try {
    const abort = new AbortController();
    const timer = setTimeout(() => abort.abort(), 20000);
    const response = await fetch(`${BALE_API_BASE}${BALE_BOT_TOKEN}/sendMessage`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ chat_id: input.chat_id || BALE_ADMIN_CHAT_ID, text: input.text || '' }),
      signal: abort.signal,
    });
    clearTimeout(timer);
    const body = await response.json().catch(() => ({}));
    await persistEvent({ kind: 'bale_response', payload: { event_id: event.id }, response: body });
    return res.status(response.ok ? 200 : 502).json({ ok: response.ok, message_id: body?.result?.message_id?.toString?.() || `BALE-${Date.now()}`, stored: true, bale: body });
  } catch (e) {
    await persistEvent({ kind: 'bale_response_error', payload: { event_id: event.id, error: String(e) } });
    return res.status(202).json({ ok: true, stored: true, queued: true, message_id: `QUEUED-${Date.now()}` });
  }
});

app.post('/api/v1/bale/photos', upload.single('photo'), async (req, res) => {
  const bytes = req.file?.buffer;
  const meta = { chat_id: req.body?.chat_id || BALE_ADMIN_CHAT_ID, caption: req.body?.caption || '', kind: req.body?.kind || 'support_receipt', file_name: req.file?.originalname || 'receipt.jpg', size: bytes?.length || 0 };
  const event = await persistEvent({ kind: 'bale_photo', payload: { ...meta, bytes_base64: bytes ? bytes.toString('base64') : null } });
  if (!bytes) return res.status(400).json({ ok: false, error: 'photo required' });
  try {
    const form = new FormData();
    form.append('chat_id', meta.chat_id);
    form.append('caption', meta.caption);
    form.append('photo', new Blob([bytes]), meta.file_name);
    const abort = new AbortController();
    const timer = setTimeout(() => abort.abort(), 30000);
    const response = await fetch(`${BALE_API_BASE}${BALE_BOT_TOKEN}/sendPhoto`, { method: 'POST', body: form, signal: abort.signal });
    clearTimeout(timer);
    const body = await response.json().catch(() => ({}));
    await persistEvent({ kind: 'bale_photo_response', payload: { event_id: event.id }, response: body });
    return res.status(response.ok ? 200 : 502).json({ ok: response.ok, stored: true, message_id: body?.result?.message_id?.toString?.() || `BALE-${Date.now()}`, bale: body });
  } catch (e) {
    await persistEvent({ kind: 'bale_photo_error', payload: { event_id: event.id, error: String(e) } });
    return res.status(202).json({ ok: true, stored: true, queued: true, message_id: `QUEUED-${Date.now()}` });
  }
});

app.get('/api/v1/requests/:id/status', async (req, res) => {
  try {
    const status = await findLatestStatus(req.params.id, req.query.bale_msg?.toString() || '');
    return ok(res, { status: status || 'در انتظار تایید مدیر' });
  } catch (e) {
    return res.status(500).json({ ok: false, error: String(e) });
  }
});

app.post('/api/v1/requests/:id/status', async (req, res) => {
  const payload = { request_id: Number(req.params.id), status: req.body?.status || 'در انتظار تایید مدیر', bale_msg: req.body?.bale_msg || '' };
  await persistEvent({ kind: 'request_status', payload });
  return ok(res, payload);
});

const port = Number(process.env.PORT || 8080);
app.listen(port, () => console.log(`SookhtMan Railway API listening on ${port}`));
