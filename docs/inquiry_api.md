# API استعلام خودرو

اپ کاربر برای درخواست‌های بیمه/استعلام در این نسخه از **فرم → Railway → ربات بله** استفاده می‌کند. مسیرهای provider پایین‌تر برای backend نگه داشته شده‌اند تا بعداً در صورت داشتن API رسمی/B2B مجاز بتوان آن‌ها را فعال کرد.

## مسیرهای provider در backend

`GET /api/inquiry/{kind}?plate=...`

انواع فعلی:

- `violations`
- `insurance`
- `tolls`
- `tax`
- `technical_inspection`

این endpointها به کلیدهای provider که فقط روی Railway نگهداری می‌شوند متکی هستند.

## پاسخ موفق نمونه

```json
{
  "ok": true,
  "kind": "insurance",
  "plate": "12الف34567",
  "data": {
    "valid": true,
    "expires_at": "2027-03-20",
    "amount": 0
  },
  "message": "بیمه معتبر است"
}
```

این پروژه scraping صفحات عمومی وب را انجام نمی‌دهد. برای production فقط API رسمی یا B2B مجاز provider را در Railway تنظیم کنید.
