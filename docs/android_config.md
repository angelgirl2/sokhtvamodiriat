# تنظیم Android برای سوخت من

بعد از `flutter create --platforms=android .`، این موارد را بررسی کنید:

## android/app/build.gradle(.kts)

- `minSdk = 24`
- `compileSdk = 36`
- `targetSdk = 36`

اگر قالب Flutter شما از `flutter.compileSdkVersion` استفاده می‌کند، کافی است compile SDK نصب‌شده حداقل 36 باشد.

## AndroidManifest.xml

مجوز اعلان برای Android 13+ با permission زیر لازم است:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

برنامه برای خودروها از آیکون داخلی استفاده می‌کند و به مجوزهای فایل یا گالری نیاز ندارد.

## دکمه اعلان‌ها

کلاس `LocalReminderService` از `flutter_local_notifications` و `timezone` استفاده می‌کند و برای یادآوری‌های معمول از `inexactAllowWhileIdle` استفاده شده تا نیاز به مجوز exact alarm ایجاد نشود.
