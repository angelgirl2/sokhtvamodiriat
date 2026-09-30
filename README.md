# مدیریت سوخت و استعلام خودرو

این پروژه یک اپ Flutter با معماری **offline-first** است: SQLite روی دستگاه منبع اصلی کار روزمره است و Railway فقط لایه آنلاین/پشتیبان و صف ارسال را فراهم می‌کند.

## رفتار نهایی برنامه

- برنامه بدون اینترنت هم باز می‌شود و ثبت خودرو، ثبت سوخت، سرویس، یادآورها، گزارش‌ها و فرم‌های درخواست روی دستگاه انجام می‌شود.
- ثبت سوخت‌گیری (`fuel_logs`) هیچ پیام مستقیمی به ربات بله ندارد.
- درخواست‌های بیمه و خدمات خودرو، از جمله درخواست کارت سوخت/المثنی و اسناد، با فرم کامل به backend می‌روند و backend آنها را به ربات بله ارسال می‌کند.
- درخواست‌های استعلام نیز با فرم کامل به ربات بله می‌روند؛ گزینه‌ی پرداخت مستقیمِ نمایشی حذف شده است چون در این نسخه درگاه واقعی پشت آن وجود نداشت.
- اگر هنگام ارسال اینترنت یا Railway در دسترس نباشد، درخواست در SQLite با شناسه محلی `LOCAL-*` باقی می‌ماند و در اجرای بعدی یا در چرخه‌ی همگام‌سازی خودکار دوباره ارسال می‌شود.
- داده‌های محلی پس از تغییر، با فاصله‌ی کوتاه و همچنین با دکمه‌ی «همگام‌سازی Railway» به PostgreSQL روی Railway فرستاده می‌شوند.
- اگر SQLite یک نصب تازه و خالی باشد، برنامه می‌تواند snapshot همان نصب را از Railway برگرداند.
- آدرس backend می‌تواند دو مقدار داشته باشد: یک دامنه اصلی و یک دامنه پشتیبان. این برای کاهش وابستگی به یک endpoint است؛ اما هیچ زیرساختی نمی‌تواند دسترسی اینترنت را روی تمام شبکه‌های فیلترشده تضمین کند.

## معماری

```text
                    ┌────────────────────────────┐
                    │       Flutter Android      │
                    │                            │
                    │  SQLite / Offline-First    │
                    │  ├─ خودرو                  │
                    │  ├─ ثبت سوخت               │
                    │  ├─ سرویس و گزارش          │
                    │  └─ صف درخواست‌ها          │
                    └─────────────┬──────────────┘
                                  │ HTTPS
                     ┌────────────▼─────────────┐
                     │      Railway API         │
                     │      Node.js + Docker    │
                     ├────────────┬─────────────┤
                     │            │             │
                     ▼            ▼             ▼
               PostgreSQL      Bale API     Provider APIs
               snapshot DB     فرم‌ها        اختیاری/B2B
```

Railway سرویس PostgreSQL را با `DATABASE_URL` در اختیار سرویس API قرار می‌دهد؛ دیتابیس Railway به‌صورت پیش‌فرض private است و از داخل همان پروژه قابل اتصال است. citeturn817416search0

## امنیت GitHub

هیچ token یا کلید سرویس‌دهنده‌ای داخل Flutter، Dockerfile یا workflow گیت‌هاب قرار نمی‌گیرد. مقادیر حساس فقط در Variables سرویس Railway تنظیم می‌شوند؛ Railway این متغیرها را در زمان build/run به سرویس می‌دهد. citeturn817416search2

متغیرهای Flutter با `--dart-define` فقط شامل URLهای عمومی backend هستند، نه secretهای بله یا provider.

فایل `.env.example` فقط نام متغیرها را دارد و نباید با مقدار واقعی commit شود.

اگر token قدیمی بله قبلاً در repository واقعی قرار گرفته بوده، قبل از production باید آن token در سرویس بله تعویض/باطل شود.

## ساختار Railway

در Railway یک Project بسازید و این دو Service را داخل همان Project داشته باشید:

1. `sookhtman-api` از همین repository و Dockerfile ریشه.
2. `Postgres` از قالب PostgreSQL خود Railway.

Railway با Dockerfile ریشه پروژه را build می‌کند. citeturn817416search1turn817416search5

بعد، `DATABASE_URL` را برای سرویس API از سرویس Postgres به‌صورت reference متغیر قرار دهید؛ Railway syntax ارجاع بین سرویس‌ها را پشتیبانی می‌کند، مثلاً `${{Postgres.DATABASE_URL}}`. citeturn817416search4turn817416search7

### Variables سرویس API

```text
DATABASE_URL=${{Postgres.DATABASE_URL}}
BALE_BOT_TOKEN=توکن واقعی ربات بله
BALE_ADMIN_CHAT_ID=شناسه چت مدیر/پذیرنده درخواست‌ها
BALE_MAX_REQUESTS_PER_MINUTE=20
PROVIDER_TIMEOUT_MS=25000
```

فقط در صورتی که استعلام مستقیم provider را بعداً فعال کنید، متغیرهای `ITOLL_*`، `BIMEH_*` و `ETAX_*` را هم تنظیم کنید.

بعد از Deploy، برای سرویس API یک Public Domain بسازید و مسیر زیر را تست کنید:

```text
GET https://YOUR-DOMAIN/api/health
```

باید پاسخ شامل `ok: true` و وضعیت `database` و `bale` بدهد.

## GitHub و Deploy خودکار

می‌توان repository را به Railway وصل کرد تا با push به branch انتخاب‌شده، deployment جدید ساخته شود. citeturn817416search6

Workflow موجود GitHub برای APK فقط این دو متغیر عمومی را می‌گیرد:

```text
SOKHT_API_PRIMARY_URL
SOKHT_API_FALLBACK_URL
```

این دو مقدار را در **GitHub Actions Variables** قرار دهید؛ secretهای Railway یا Bale را به GitHub APK build منتقل نکنید.

## مسیرهای API اصلی

```text
GET  /api/health
GET  /api/inquiry/config
POST /api/v1/sync
GET  /api/v1/sync/latest?device_id=...
POST /api/v1/bale/message
POST /api/v1/bale/photo
GET  /api/v1/requests/:id/status
```

`/api/v1/sync` یک snapshot از داده‌های SQLite را برای همان نصب برنامه در PostgreSQL ذخیره می‌کند. درخواست sync با یک شناسه تصادفی ۶۴کاراکتری برای همان نصب محدود می‌شود و header `X-Device-Key` نیز لازم است.

## ارسال به بله

### بیمه و خدمات

فرم خدمات بیمه و فرم‌های خدمات خودرو یک رکورد محلی می‌سازند و سپس متن فرم را از طریق backend به ربات بله می‌فرستند.

### استعلام

فرم استعلام همیشه در workflow مدیریتی/بله ثبت می‌شود. در صورت نبود اینترنت، رکورد با `LOCAL-PENDING` ذخیره می‌شود و در تلاش بعدی به بله ارسال می‌شود.

### کارت سوخت

درخواست کارت سوخت داخل همان مسیر خدماتی قرار دارد، بنابراین فرم آن نیز مانند سایر درخواست‌های خدماتی به ربات بله ارسال می‌شود.

### ثبت سوخت

`addFuelLog()` فقط SQLite را تغییر می‌دهد. هیچ call به `BaleBotService` از مسیر ثبت سوخت‌گیری انجام نمی‌شود. داده‌ی ثبت سوخت در همگام‌سازی PostgreSQL ذخیره می‌شود، اما پیام بله برای آن ساخته نمی‌شود.

## تست آفلاین

1. اینترنت گوشی را خاموش کنید.
2. خودرو اضافه کنید.
3. چند بار سوخت ثبت کنید.
4. یک سرویس و یادآور اضافه کنید.
5. یک درخواست بیمه یا کارت سوخت ثبت کنید.
6. باید داده‌ها بدون خطا روی دستگاه بمانند.

## تست آنلاین

1. اینترنت را روشن کنید.
2. چند ثانیه برنامه را باز نگه دارید یا «همگام‌سازی Railway» را بزنید.
3. درخواست صف‌شده باید برای بله ارسال شود.
4. `GET /api/health` باید `database: true` را نشان دهد.

## محدودیت شبکه‌های فیلترشده

این معماری قابلیت آفلاین را مستقل از اینترنت نگه می‌دارد و امکان دو endpoint عمومی برای backend دارد. با این حال اگر یک شبکه دسترسی HTTPS به دامنه‌های خارجی/Railway را مسدود کند، هیچ کدی داخل APK به‌تنهایی نمی‌تواند آن endpoint را مجبور به در دسترس بودن کند. برای چنین شرایطی یک endpoint دوم باید واقعاً از آن شبکه قابل دسترس باشد.

## اجرای محلی backend

```powershell
cd server
npm install
$env:PORT="3000"
node src/server.js
```

برای اجرای کامل sync محلی، `DATABASE_URL` نیز باید به یک PostgreSQL قابل دسترس اشاره کند.

## Build دستی Flutter

```powershell
flutter pub get
flutter build apk --release `
  --dart-define=SOKHT_API_PRIMARY_URL=https://YOUR-DOMAIN `
  --dart-define=SOKHT_API_FALLBACK_URL=https://YOUR-RAILWAY-DOMAIN.up.railway.app
```
