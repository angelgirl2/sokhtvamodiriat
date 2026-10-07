# سامانه مدیریت سوخت و استعلام خودرو (Sokht Va Modiriat)

سامانه جامع و هوشمند مدیریت مصرف سوخت خودرو و موتورسیکلت، استعلام خلافی و عوارض، خدمات بیمه و یادآورهای دوره‌ای سرویس و نگهداری.

- **مخزن گیت‌هاب:** [https://github.com/angelgirl2/sokhtvamodiriat](https://github.com/angelgirl2/sokhtvamodiriat)
- **سرور ابری ریل‌وی (Railway Backend):** [https://sokhtvamodiriat-production.up.railway.app](https://sokhtvamodiriat-production.up.railway.app)
- **پشتیبانی و ارتباط با توسعه‌دهنده:** `@angelgirlbrand` (میلاد قنواتی)

---

## 🚀 ویژگی‌های کلیدی اپلیکیشن

1. **مدیریت مصرف سوخت و آمار باک:**
   - ثبت دقیق اطلاعات سوخت‌گیری (لیتر، مبلغ، کیلومتر پیمایش).
   - محاسبه مصرف میانگین در هر ۱۰۰ کیلومتر.
   - نمودارهای تحلیلی و آماری مصرف سوخت بر پایه Vico Charts.

2. **استعلام برخط خلافی و عوارض (پلاک‌پایه):**
   - استعلام برخط خودرو و موتورسیکلت با شماره پلاک گرافیکی ملی.
   - ثبت و مخابره خودکار به ربات رسمی بله و سرور Railway.
   - ذخیره سوابق در پایگاه داده محلی Room.

3. **ثبت درخواست‌های خدمات و بیمه درون‌برنامه:**
   - فرم اختصاصی صدور و استعلام بیمه شخص ثالث و بدنه.
   - درخواست نوبت معاینه فنی و کارت هوشمند سوخت.
   - ارسال مستقیم به ربات بله بدون نیاز به خروج از اپلیکیشن.

4. **یادآور سرویس و نگهداری دوره‌ای:**
   - یادآور کیلومتری و تاریخی تعویض روغن، لنت ترمز، فیلترها، تسمه‌تایم و انقضای بیمه‌نامه.
   - اعلان‌های هوشمند محلی (Local Notifications).

5. **همگام‌سازی ابری خودکار با Railway:**
   - اتصال مستقیم به سرور `https://sokhtvamodiriat-production.up.railway.app/api/v1/sync`.
   - ذخیره امن محلی با Room و پشتیبان‌گیری در کلود.

6. **امنیت و شخصی‌سازی:**
   - قفل برنامه با رمز ۴ رقمی و سنسور بیومتریک (اثر انگشت).
   - تم‌های رنگی متنوع بر اساس Material 3 و پشتیبانی کامل از حالت تاریک/روشن (Dark/Light Mode).
   - رابط کاربری کاملاً راست‌چین (RTL) و بومی‌سازی‌شده فارسی.

---

## 🛠 معماری و تکنولوژی‌ها

- **زبان برنامه‌نویسی:** Kotlin
- **رابط کاربری (UI):** Jetpack Compose با طراحی Material Design 3
- **پایگاه داده محلی:** Android Room Database (KSP)
- **معماری:** Clean Architecture + MVVM + Coroutines & Flow
- **ارتباط شبکه:** OkHttp3 & Retrofit
- **بک‌اند ابری:** Railway Server ([sokhtvamodiriat-production.up.railway.app](https://sokhtvamodiriat-production.up.railway.app))
- **ربات متصل:** Bale Bot API

---

## 📦 نحوه اجرای پروژه در Android Studio

1. کلون کردن مخزن:
   ```bash
   git clone https://github.com/angelgirl2/sokhtvamodiriat.git
   cd sokhtvamodiriat
   ```
2. باز کردن پروژه در **Android Studio Ladybug / Koala** یا جدیدتر.
3. همگام‌سازی Gradle و بیلد:
   ```bash
   ./gradlew assembleDebug
   ```
4. نصب و اجرا بر روی دستگاه یا شبیه‌ساز اندروید (حداقل Android 8.0 - API 26).
