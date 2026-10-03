import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'app_controller.dart';
import 'core/app_config.dart';
import 'core/theme.dart';
import 'data/local_db.dart';
import 'data/notifications.dart';
import 'data/security.dart';
import 'data/services.dart';
import 'screens/home_shell.dart';
import 'screens/lock_screen.dart';
import 'screens/splash_screen.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final prefs = await SharedPreferences.getInstance();
  final db = LocalDatabase.instance;
  final railway = RailwayApi();
  final bale = BaleService(railway);
  final repository = AppRepository(db: db, railway: railway, bale: bale);
  final security = SecurityService(prefs);
  await security.migrateLegacyAndroidPrefs();
  await NotificationService().initialize();
  final controller = AppController(db: db, repository: repository, security: security);
  await controller.load();
  runApp(SookhtManApp(controller: controller));
}

class SookhtManApp extends StatelessWidget {
  const SookhtManApp({super.key, required this.controller});
  final AppController controller;

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: controller,
      builder: (context, _) {
        final darkPref = controller.security.darkMode;
        final brightness = darkPref == 'dark'
            ? Brightness.dark
            : darkPref == 'light'
                ? Brightness.light
                : MediaQuery.platformBrightnessOf(context);
        final seed = switch (controller.security.themeColor) {
          'red' => AppTheme.red,
          'purple' => AppTheme.purple,
          'emerald' => AppTheme.turquoise,
          _ => AppTheme.skyBlue,
        };
        final theme = brightness == Brightness.dark
            ? AppTheme.dark(seedColor: seed)
            : AppTheme.light(seedColor: seed);
        return MaterialApp(
          debugShowCheckedModeBanner: false,
          title: AppConfig.appName,
          theme: theme,
          builder: (context, child) => Directionality(
            textDirection: TextDirection.rtl,
            child: child ?? const SizedBox.shrink(),
          ),
          home: AnimatedSwitcher(
            duration: AppMotion.normal,
            switchInCurve: AppMotion.curve,
            switchOutCurve: AppMotion.curve,
            child: controller.showSplash
                ? SplashScreen(
                    key: const ValueKey('splash'),
                    onDone: () {
                      controller.showSplash = false;
                      if (controller.security.isPinEnabled) controller.unlocked = false;
                      controller.notifyListeners();
                    },
                  )
                : (!controller.unlocked
                    ? LockScreen(key: const ValueKey('lock'), controller: controller)
                    : HomeShell(key: const ValueKey('home'), controller: controller)),
          ),
        );
      },
    );
  }
}
