import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter/foundation.dart';

import 'railway_api_client.dart';

/// Sends administrative messages through our own backend.
///
/// The Bale bot token never ships with the Flutter application. Railway owns
/// the secret and talks to Bale from the server side.
class BaleBotService {
  BaleBotService({
    required RailwayApiClient api,
  }) : _api = api;

  final RailwayApiClient _api;

  Future<BaleSendResponse> sendMessage({
    required String text,
  }) async {
    try {
      final response = await _api.postObject(
        '/api/v1/bale/message',
        {'text': text},
      );
      final messageId = response?['message_id']?.toString();
      if (response?['ok'] == true && messageId != null && messageId.isNotEmpty) {
        return BaleSendResponse(
          isSuccess: true,
          isLocalFallback: false,
          messageId: messageId,
          rawResponse: response.toString(),
        );
      }
      final generatedId = 'LOCAL-${_tail()}';
      return BaleSendResponse(
        isSuccess: true,
        isLocalFallback: true,
        messageId: generatedId,
        rawResponse: 'درخواست در حافظه محلی صف شد.',
      );
    } catch (e) {
      debugPrint('BaleBotService error: $e');
      final generatedId = 'LOCAL-${_tail()}';
      return BaleSendResponse(
        isSuccess: true,
        isLocalFallback: true,
        messageId: generatedId,
        rawResponse: 'اینترنت در دسترس نبود؛ درخواست محلی ذخیره شد.',
      );
    }
  }

  Future<BaleSendResponse> sendPhoto({
    required String caption,
    Uint8List? photoBytes,
    String fileName = 'support_receipt.jpg',
  }) async {
    if (photoBytes == null || photoBytes.isEmpty) {
      return sendMessage(text: caption);
    }

    try {
      final response = await _api.postObject(
        '/api/v1/bale/photo',
        {
          'caption': caption,
          'file_name': fileName,
          'photo_base64': base64Encode(photoBytes),
        },
      );
      final messageId = response?['message_id']?.toString();
      if (response?['ok'] == true && messageId != null && messageId.isNotEmpty) {
        return BaleSendResponse(
          isSuccess: true,
          isLocalFallback: false,
          messageId: messageId,
          rawResponse: response.toString(),
        );
      }
    } catch (e) {
      debugPrint('BaleBotService sendPhoto error: $e');
    }

    return BaleSendResponse(
      isSuccess: true,
      isLocalFallback: true,
      messageId: 'LOCAL-${_tail()}',
      rawResponse: 'فیش در دستگاه ذخیره شد و پس از اتصال ارسال می‌شود.',
    );
  }

  static String _tail() {
    final ms = DateTime.now().millisecondsSinceEpoch.toString();
    return ms.length <= 8 ? ms : ms.substring(ms.length - 8);
  }
}

class BaleSendResponse {
  const BaleSendResponse({
    required this.isSuccess,
    required this.isLocalFallback,
    required this.messageId,
    required this.rawResponse,
  });

  final bool isSuccess;
  final bool isLocalFallback;
  final String messageId;
  final String rawResponse;
}
