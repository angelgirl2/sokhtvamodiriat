# SookhtMan Railway API

این پوشه قرارداد سرور مورد استفاده نسخه Flutter است.

## محیط Railway

`BALE_BOT_TOKEN` و `BALE_ADMIN_CHAT_ID` می‌توانند در Environment Variables باشند؛ برای سازگاری با نسخه قبلی، مقدارها در کد نیز نگه داشته شده‌اند.

در صورت وجود `DATABASE_URL` داده‌ها در PostgreSQL و در غیر این صورت در `RAILWAY_VOLUME_MOUNT_PATH/data/bale_events.jsonl` ذخیره می‌شوند. برای Railway استفاده از PostgreSQL یا Volume توصیه می‌شود.

## Routes

- `GET /` سلامت سرویس
- `GET /api/health` سلامت سرویس (سازگار با استقرار فعلی)
- `POST /api/v1/sync` ذخیره snapshot و eventهای صف
- `POST /api/v1/bale/messages` ذخیره درخواست + ارسال به Bale
- `POST /api/v1/bale/message` همان route قبلی برای سازگاری با Railway فعلی
- `POST /api/v1/bale/photos` ذخیره فیش + ارسال به Bale
- `GET /api/v1/requests/:id/status?bale_msg=...` وضعیت درخواست
- `POST /api/v1/requests/:id/status` ثبت وضعیت از سمت ادمین
