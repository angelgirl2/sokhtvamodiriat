import 'package:flutter/foundation.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';

/// Local notification helper ported from `NotificationHelper.kt`.
///
/// A single high-importance channel named «یادآورهای سرویس و نگهداری خودرو»
/// carries all service / inspection alerts.
class NotificationHelper {
  NotificationHelper._();

  static final NotificationHelper instance = NotificationHelper._();

  static const channelId = 'fuel_service_reminders_channel';
  static const channelName = 'یادآورهای سرویس و نگهداری خودرو';
  static const channelDescription =
      'هشدارهای موعد تعویض روغن، فیلترها و سرویس دوره‌ای خودرو';

  final FlutterLocalNotificationsPlugin _plugin = FlutterLocalNotificationsPlugin();
  bool _initialized = false;

  Future<void> init() async {
    if (_initialized) return;
    try {
      const androidInit = AndroidInitializationSettings('@mipmap/ic_launcher');
      const initSettings = InitializationSettings(android: androidInit);
      await _plugin.initialize(initSettings);

      final androidImpl = _plugin.resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin>();
      await androidImpl?.createNotificationChannel(
        const AndroidNotificationChannel(
          channelId,
          channelName,
          description: channelDescription,
          importance: Importance.high,
          enableVibration: true,
        ),
      );
      await androidImpl?.requestNotificationsPermission();
      _initialized = true;
    } catch (e) {
      debugPrint('NotificationHelper init failed: $e');
    }
  }

  Future<void> showServiceAlert({
    required String title,
    required String message,
    int? notificationId,
  }) async {
    try {
      await init();
      const details = NotificationDetails(
        android: AndroidNotificationDetails(
          channelId,
          channelName,
          channelDescription: channelDescription,
          importance: Importance.high,
          priority: Priority.high,
          styleInformation: BigTextStyleInformation(''),
        ),
      );
      await _plugin.show(
        notificationId ?? (DateTime.now().millisecondsSinceEpoch % 10000),
        '🚗 یادآور خودرو: $title',
        message,
        details,
      );
    } catch (e) {
      debugPrint('NotificationHelper could not show notification: $e');
    }
  }
}
