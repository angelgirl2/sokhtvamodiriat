import 'package:flutter/foundation.dart';

import 'railway_api_client.dart';

class RailwaySyncService {
  RailwaySyncService({required this._api});

  final RailwayApiClient _api;

  Future<bool> syncSnapshot({
    required String deviceId,
    required Map<String, dynamic> snapshot,
  }) async {
    try {
      final response = await _api.postObject(
        '/api/v1/sync',
        {
          'device_id': deviceId,
          'schema_version': 1,
          'snapshot': snapshot,
          'timestamp': DateTime.now().millisecondsSinceEpoch,
        },
        headers: {'X-Device-Key': deviceId},
      );
      return response?['ok'] == true;
    } catch (e) {
      debugPrint('Railway sync skipped/queued: $e');
      return false;
    }
  }

  Future<Map<String, dynamic>?> fetchSnapshot({required String deviceId}) async {
    try {
      final response = await _api.getObject(
        '/api/v1/sync/latest?device_id=${Uri.encodeQueryComponent(deviceId)}',
        headers: {'X-Device-Key': deviceId},
      );
      if (response?['ok'] != true) return null;
      final snapshot = response?['snapshot'];
      return snapshot is Map<String, dynamic> ? snapshot : null;
    } catch (e) {
      debugPrint('Railway restore skipped: $e');
      return null;
    }
  }

  Future<String> checkAdminApprovalStatus(
    int requestId,
    String baleMessageId,
  ) async {
    try {
      final response = await _api.getObject(
        '/api/v1/requests/$requestId/status?bale_msg=${Uri.encodeQueryComponent(baleMessageId)}',
      );
      return response?['status']?.toString() ?? 'در انتظار تایید مدیر';
    } catch (e) {
      debugPrint('RailwaySync status fallback: $e');
      return 'در انتظار تایید مدیر';
    }
  }
}
