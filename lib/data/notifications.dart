import 'package:flutter_local_notifications/flutter_local_notifications.dart';

class NotificationService {
  final FlutterLocalNotificationsPlugin plugin = FlutterLocalNotificationsPlugin();

  Future<void> initialize() async {
    const settings = AndroidInitializationSettings('@mipmap/ic_launcher');
    await plugin.initialize(settings: const InitializationSettings(android: settings));
  }

  Future<void> showReminder(String title, String body) async {
    const details = AndroidNotificationDetails(
      'sookht_reminders',
      'یادآورهای خودرو',
      channelDescription: 'یادآوری سرویس، بیمه و معاینه فنی',
      importance: Importance.high,
      priority: Priority.high,
    );
    await plugin.show(
      id: DateTime.now().millisecondsSinceEpoch.remainder(2147483647),
      title: title,
      body: body,
      notificationDetails: const NotificationDetails(android: details),
    );
  }
}
