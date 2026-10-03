import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:http/http.dart' as http;

import '../core/app_config.dart';
import 'local_db.dart';
import 'models.dart';

class RailwayApi {
  RailwayApi({this.baseUrl = AppConfig.railwayBaseUrl});
  String baseUrl;

  Uri _uri(String path, [Map<String, String>? query]) =>
      Uri.parse('${baseUrl.replaceFirst(RegExp(r'\/$'), '')}/$path').replace(queryParameters: query);

  Future<bool> health() async {
    try {
      final r = await http.get(_uri('')).timeout(const Duration(seconds: 10));
      return r.statusCode >= 200 && r.statusCode < 300;
    } catch (_) {
      return false;
    }
  }

  Future<String?> syncSnapshot(Map<String, dynamic> payload) async {
    try {
      final r = await http
          .post(
            _uri('api/v1/sync'),
            headers: {'Content-Type': 'application/json'},
            body: jsonEncode(payload),
          )
          .timeout(const Duration(seconds: 20));
      if (r.statusCode >= 200 && r.statusCode < 300) return r.body;
    } catch (_) {}
    return null;
  }

  Future<Map<String, dynamic>?> sendBaleText({
    required String chatId,
    required String text,
    required String kind,
    String? localRef,
  }) async {
    final payload = {
      'chat_id': chatId,
      'text': text,
      'kind': kind,
      'local_ref': localRef,
      'source': 'flutter',
      'created_at': DateTime.now().toIso8601String(),
      // Kept for server-side auditing/compatibility. The server should use its
      // own configured token when actually calling Bale.
      'bot_token_present': AppConfig.baleBotToken.isNotEmpty,
    };
    try {
      final r = await http
          .post(
            _uri('api/v1/bale/messages'),
            headers: {'Content-Type': 'application/json'},
            body: jsonEncode(payload),
          )
          .timeout(const Duration(seconds: 20));
      if (r.statusCode >= 200 && r.statusCode < 300) {
        return jsonDecode(r.body) as Map<String, dynamic>;
      }
    } catch (_) {}

    // Compatibility with the currently deployed legacy Railway route.
    try {
      final legacyPayload = {...payload, 'message': text};
      final r = await http
          .post(
            _uri('api/v1/bale/message'),
            headers: {'Content-Type': 'application/json'},
            body: jsonEncode(legacyPayload),
          )
          .timeout(const Duration(seconds: 20));
      if (r.statusCode >= 200 && r.statusCode < 300) {
        return jsonDecode(r.body) as Map<String, dynamic>;
      }
    } catch (_) {}

    // Final fallback: persist the exact event using the existing /api/v1/sync
    // endpoint so no Bale submission is lost.
    await syncSnapshot({
      'app': 'sookht_man',
      'type': 'bale_message_queue',
      'bale': payload,
    });
    return null;
  }

  Future<Map<String, dynamic>?> sendBalePhoto({
    required String chatId,
    required String caption,
    required Uint8List bytes,
    required String fileName,
    String? localRef,
  }) async {
    try {
      final request = http.MultipartRequest('POST', _uri('api/v1/bale/photos'))
        ..fields['chat_id'] = chatId
        ..fields['caption'] = caption
        ..fields['kind'] = 'support_receipt'
        ..fields['local_ref'] = localRef ?? ''
        ..fields['source'] = 'flutter'
        ..files.add(http.MultipartFile.fromBytes('photo', bytes, filename: fileName));
      final response = await request.send().timeout(const Duration(seconds: 30));
      final text = await response.stream.bytesToString();
      if (response.statusCode >= 200 && response.statusCode < 300 && text.isNotEmpty) {
        return jsonDecode(text) as Map<String, dynamic>;
      }
    } catch (_) {}

    await syncSnapshot({
      'app': 'sookht_man',
      'type': 'bale_photo_queue',
      'bale': {
        'chat_id': chatId,
        'caption': caption,
        'kind': 'support_receipt',
        'local_ref': localRef,
        'file_name': fileName,
        'bytes_base64': base64Encode(bytes),
        'created_at': DateTime.now().toIso8601String(),
      },
    });
    return null;
  }

  Future<bool> updateRequestStatus({required int requestId, required String baleMessageId, required String status}) async {
    try {
      final r = await http.post(
        _uri('api/v1/requests/$requestId/status'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'request_id': requestId, 'bale_msg': baleMessageId, 'status': status}),
      ).timeout(const Duration(seconds: 15));
      return r.statusCode >= 200 && r.statusCode < 300;
    } catch (_) {
      return false;
    }
  }

  Future<String> requestStatus(int requestId, String baleMessageId) async {
    try {
      final r = await http
          .get(
            _uri(
              'api/v1/requests/$requestId/status',
              {'bale_msg': baleMessageId},
            ),
          )
          .timeout(const Duration(seconds: 15));
      if (r.statusCode >= 200 && r.statusCode < 300) {
        final json = jsonDecode(r.body) as Map<String, dynamic>;
        return json['status']?.toString() ?? 'در انتظار تایید مدیر';
      }
    } catch (_) {}
    return 'در انتظار تایید مدیر';
  }
}

class BaleService {
  BaleService(this.railway);
  final RailwayApi railway;

  Future<String> sendMessage(String text, {String kind = 'generic'}) async {
    final localRef = 'BALE-${DateTime.now().millisecondsSinceEpoch}';
    final result = await railway.sendBaleText(
      chatId: AppConfig.baleAdminChatId,
      text: text,
      kind: kind,
      localRef: localRef,
    );
    final id = result?['message_id']?.toString();
    return id?.isNotEmpty == true ? id! : localRef;
  }

  Future<String> sendPhoto(String caption, Uint8List bytes, String fileName) async {
    final localRef = 'BALE-${DateTime.now().millisecondsSinceEpoch}';
    final result = await railway.sendBalePhoto(
      chatId: AppConfig.baleAdminChatId,
      caption: caption,
      bytes: bytes,
      fileName: fileName,
      localRef: localRef,
    );
    final id = result?['message_id']?.toString();
    return id?.isNotEmpty == true ? id! : localRef;
  }
}

class AppRepository {
  AppRepository({required this.db, required this.railway, required this.bale});

  final LocalDatabase db;
  final RailwayApi railway;
  final BaleService bale;

  Future<List<Vehicle>> vehicles() => db.vehicles();
  Future<List<FuelLog>> fuelLogs() => db.fuelLogs();
  Future<List<ServiceReminder>> reminders() => db.reminders();
  Future<List<ServiceHistory>> serviceHistory() => db.serviceHistory();
  Future<List<ServiceRequest>> requests() => db.requests();
  Future<List<InquiryRecord>> inquiries() => db.inquiries();

  Future<int> addVehicle(Vehicle v) => db.insertVehicle(v);
  Future<void> updateVehicle(Vehicle v) => db.updateVehicle(v);
  Future<void> deleteVehicle(int id) => db.deleteVehicle(id);
  Future<int> addFuelLog(FuelLog l) => db.insertFuelLog(l);
  Future<void> deleteFuelLog(int id) => db.deleteFuelLog(id);
  Future<int> addReminder(ServiceReminder r) => db.insertReminder(r);
  Future<void> updateReminder(ServiceReminder r) => db.updateReminder(r);
  Future<void> deleteReminder(int id) => db.deleteReminder(id);
  Future<int> addServiceHistory(ServiceHistory h) => db.insertServiceHistory(h);
  Future<void> deleteServiceHistory(int id) => db.deleteServiceHistory(id);
  Future<int> addRequest(ServiceRequest r) => db.insertRequest(r);
  Future<void> updateRequest(ServiceRequest r) => db.updateRequest(r);
  Future<void> deleteRequest(int id) => db.deleteRequest(id);
  Future<void> updateRequestStatus({
    required int requestId,
    required String? baleMessageId,
    required String status,
  }) async {
    ServiceRequest? current;
    for (final item in await db.requests()) {
      if (item.id == requestId) {
        current = item;
        break;
      }
    }
    if (current == null) return;
    await db.updateRequest(
      current.copyWith(
        status: status,
        baleMessageId: baleMessageId,
        updatedDateMillis: DateTime.now().millisecondsSinceEpoch,
      ),
    );
    await railway.updateRequestStatus(
      requestId: requestId,
      baleMessageId: baleMessageId ?? current.baleMessageId,
      status: status,
    );
  }
  Future<int> addInquiry(InquiryRecord i) => db.insertInquiry(i);
  Future<void> updateInquiry(InquiryRecord i) => db.updateInquiry(i);
  Future<void> deleteInquiry(int id) => db.deleteInquiry(id);

  Future<String> submitServiceRequest(ServiceRequest request) async {
    final now = DateTime.now().millisecondsSinceEpoch;
    final normalized = ServiceRequest(
      id: request.id,
      requestType: request.requestType,
      title: request.title,
      fullName: request.fullName,
      nationalCode: request.nationalCode,
      phoneNumber: request.phoneNumber,
      vehiclePlate: request.vehiclePlate,
      vinCode: request.vinCode,
      barcodeNumber: request.barcodeNumber,
      engineNumber: request.engineNumber,
      chassisNumber: request.chassisNumber,
      postalCode: request.postalCode,
      address: request.address,
      insuranceCategory: request.insuranceCategory,
      insuranceCompany: request.insuranceCompany,
      durationMonths: request.durationMonths,
      discountPercent: request.discountPercent,
      additionalDetails: request.additionalDetails,
      status: 'در انتظار تایید مدیر',
      submissionDateMillis: now,
      updatedDateMillis: now,
    );

    final message = _serviceMessage(normalized);
    final msgId = await bale.sendMessage(message, kind: 'service_request');
    final saved = normalized.copyWith(baleMessageId: msgId);
    await db.insertRequest(saved);
    return msgId;
  }

  Future<String> submitInquiry(InquiryRecord inquiry) async {
    final now = DateTime.now().millisecondsSinceEpoch;
    var ref = '';
    if (inquiry.workflowMethod != 'DIRECT_PAYMENT') {
      ref = await bale.sendMessage(_inquiryMessage(inquiry), kind: 'inquiry');
    }
    final saved = InquiryRecord(
      id: inquiry.id,
      inquiryType: inquiry.inquiryType,
      title: inquiry.title,
      plateNumber: inquiry.plateNumber,
      barcodeOrVin: inquiry.barcodeOrVin,
      nationalId: inquiry.nationalId,
      fullName: inquiry.fullName,
      phoneNumber: inquiry.phoneNumber,
      vinCode: inquiry.vinCode,
      barcode: inquiry.barcode,
      engineNumber: inquiry.engineNumber,
      chassisNumber: inquiry.chassisNumber,
      postalCode: inquiry.postalCode,
      address: inquiry.address,
      amount: inquiry.amount,
      workflowMethod: inquiry.workflowMethod,
      status: inquiry.workflowMethod == 'DIRECT_PAYMENT'
          ? 'در انتظار پرداخت'
          : 'در حال بررسی توسط کارشناس',
      transactionRef: ref,
      dateMillis: now,
    );
    await db.insertInquiry(saved);
    return ref;
  }

  Future<String> sendDonation({
    required int amount,
    required String customAmountText,
    required String payerName,
    required String payerPhone,
    required String note,
    Uint8List? photoBytes,
  }) async {
    final amountText = amount > 0 ? '${_money(amount)} تومان' : customAmountText;
    final msg = '🎁 رسید حمایت مالی جدید از برنامه‌نویس\n\n'
        '💰 مبلغ حمایت: $amountText\n'
        '${payerName.trim().isEmpty ? '' : '👤 نام: $payerName\n'}'
        '${payerPhone.trim().isEmpty ? '' : '📞 تماس: $payerPhone\n'}'
        '${note.trim().isEmpty ? '' : '📝 یادداشت: $note\n'}\n'
        '💳 شماره کارت واریز: ۶۲۱۹-۸۶۱۹-۲۰۶۹-۶۲۰۹\n'
        '📱 برنامه: مدیریت سوخت و استعلام خودرو';
    if (photoBytes != null) {
      return bale.sendPhoto(msg, photoBytes, 'support_receipt.jpg');
    }
    return bale.sendMessage(msg, kind: 'donation');
  }

  Future<String> syncEverything() async {
    final snapshot = await db.snapshot();
    final result = await railway.syncSnapshot(snapshot);
    if (result != null) return 'همگام‌سازی با Railway با موفقیت انجام شد.';

    await db.enqueue(QueueItem(
      kind: 'snapshot',
      payloadJson: jsonEncode(snapshot),
      createdAt: DateTime.now().millisecondsSinceEpoch,
    ));
    return 'اتصال ابری در دسترس نبود؛ اطلاعات در صف همگام‌سازی محلی قرار گرفت.';
  }

  Future<int> flushQueue() async {
    final items = await db.queue();
    var completed = 0;
    for (final item in items) {
      try {
        final payload = jsonDecode(item.payloadJson) as Map<String, dynamic>;
        final result = await railway.syncSnapshot(payload);
        if (result != null && item.id != null) {
          await db.deleteQueueItem(item.id!);
          completed++;
        } else if (item.id != null) {
          await db.bumpQueueItem(item.id!, item.attempts + 1);
        }
      } catch (_) {
        if (item.id != null) await db.bumpQueueItem(item.id!, item.attempts + 1);
      }
    }
    return completed;
  }

  String _serviceMessage(ServiceRequest r) => '''🚗 درخواست خدمات خودرو

📑 نوع درخواست: ${r.requestType}
👤 نام و نام خانوادگی: ${r.fullName}
🆔 کد ملی: ${r.nationalCode}
📱 شماره تماس: ${r.phoneNumber}

🚘 پلاک: ${r.vehiclePlate}
${r.vinCode.isEmpty ? '' : '🔎 VIN: ${r.vinCode}\n'}${r.barcodeNumber.isEmpty ? '' : '📊 بارکد: ${r.barcodeNumber}\n'}${r.engineNumber.isEmpty ? '' : '⚙️ موتور: ${r.engineNumber}\n'}${r.chassisNumber.isEmpty ? '' : '🧰 شاسی: ${r.chassisNumber}\n'}${r.postalCode.isEmpty ? '' : '📮 کد پستی: ${r.postalCode}\n'}${r.address.isEmpty ? '' : '🏠 آدرس: ${r.address}\n'}
${r.insuranceCategory.isEmpty ? '' : '🛡️ دسته بیمه: ${r.insuranceCategory}\n'}${r.insuranceCompany.isEmpty ? '' : '🏢 شرکت بیمه: ${r.insuranceCompany}\n'}${r.insuranceCategory.isEmpty ? '' : '📆 مدت: ${r.durationMonths} ماه\n'}${r.insuranceCategory.isEmpty ? '' : '🏷️ تخفیف: ${r.discountPercent}%\n'}${r.additionalDetails.isEmpty ? '' : '📝 توضیحات: ${r.additionalDetails}\n'}
⏳ وضعیت: #در_انتظار_تایید_مدیر''';

  String _inquiryMessage(InquiryRecord i) => '''💳 استعلام و تسویه خودرو

📑 نوع استعلام: ${i.inquiryType}
👤 مالک: ${i.fullName}
🆔 کد ملی: ${i.nationalId}
📱 تماس: ${i.phoneNumber}
🚘 پلاک: ${i.plateNumber}
${i.vinCode.isEmpty ? '' : '🔎 VIN: ${i.vinCode}\n'}${i.barcode.isEmpty ? '' : '📊 بارکد: ${i.barcode}\n'}${i.engineNumber.isEmpty ? '' : '⚙️ موتور: ${i.engineNumber}\n'}${i.chassisNumber.isEmpty ? '' : '🧰 شاسی: ${i.chassisNumber}\n'}${i.address.isEmpty ? '' : '🏠 آدرس: ${i.address}\n'}
💰 مبلغ برآوردی: ${_money(i.amount)} تومان''';
}

String _money(num value) => value.toString().replaceAllMapped(
      RegExp(r'(?<=\d)(?=(\d{3})+$)'),
      (_) => ',',
    );

// Kept for compatibility with the old native service layer; the app no longer
// writes directly to Bale from the client.
Future<Uint8List> readFileBytes(File file) => file.readAsBytes();
