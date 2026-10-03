# Android build fix

Fixed the release-resource merge failure caused by duplicate `LaunchTheme` and `NormalTheme` resources.

The project already defines these themes in:
- `android/app/src/main/res/values/styles.xml`
- `android/app/src/main/res/values-night/styles.xml`
- `android/app/src/main/res/values-v31/styles.xml`

The duplicate `android/app/src/main/res/values/themes.xml` was removed.

Recommended validation on Windows:

```powershell
flutter clean
flutter pub get
flutter analyze
flutter build apk --release --dart-define=SOKHT_API_PRIMARY_URL=https://sokhtvamodiriat-production.up.railway.app
```
