class AppConfig {
  AppConfig._();

  static const String appName = 'مدیریت سوخت و استعلام';
  static const String packageName = 'com.angelgirlbrand.modiratsokhtandestelam';

  // Railway API used by the Flutter app.
  static const String railwayBaseUrl =
      'https://sokhtvamodiriat-production.up.railway.app';

  // Kept in code to preserve the original project configuration as requested.
  // The Flutter app sends Bale requests through Railway so the complete event is
  // persisted server-side before the bot is contacted.
  static const String baleApiBaseUrl = 'https://tapi.bale.ai/bot';
  static const String baleBotToken =
      '1882791239:LbdEo9wCRmyaYo0_mQCSR_XtCUc0RDobd6g';
  static const String baleAdminChatId = '116268751';

  static const String defaultThemeColor = 'skyBlue';
  static const String defaultDarkMode = 'system';
  static const String securitySalt = 'AngelGirlBrandFuelSecuritySalt#2026';
}
