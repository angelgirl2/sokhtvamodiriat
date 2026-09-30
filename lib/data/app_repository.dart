import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:sqflite/sqflite.dart';

import 'app_database.dart';
import 'bale_bot_service.dart';
import 'models.dart';
import 'railway_sync_service.dart';
import 'security_manager.dart';

/// Single source of truth for all persistence + remote calls.
///
/// Direct port of the Kotlin `AppRepository`.
class AppRepository {
  AppRepository({
    required this.baleBotService,
    required this.railwaySyncService,
    required this.securityManager,
  });

  final BaleBotService baleBotService;
  final RailwaySyncService railwaySyncService;
  final SecurityManager securityManager;

  Future<Database> get _db => AppDatabase.instance();

  // ---------------------------------------------------------------------------
  // Vehicles
  // ---------------------------------------------------------------------------
  Future<List<Vehicle>> getAllVehicles() async {
    final db = await _db;
    final rows = await db.query('vehicles', orderBy: 'createdAt ASC');
    return rows.map(Vehicle.fromMap).toList();
  }

  Future<Vehicle?> getVehicleById(int id) async {
    final db = await _db;
    final rows = await db.query('vehicles', where: 'id = ?', whereArgs: [id], limit: 1);
    return rows.isEmpty ? null : Vehicle.fromMap(rows.first);
  }

  Future<int> insertVehicle(Vehicle vehicle) async {
    final db = await _db;
    return db.insert('vehicles', vehicle.toMap());
  }

  Future<void> updateVehicle(Vehicle vehicle) async {
    final db = await _db;
    await db.update('vehicles', vehicle.toMap(), where: 'id = ?', whereArgs: [vehicle.id]);
  }

  Future<void> deleteVehicle(Vehicle vehicle) async {
    final db = await _db;
    await db.delete('vehicles', where: 'id = ?', whereArgs: [vehicle.id]);
  }

  Future<int> getVehicleCount() async {
    final db = await _db;
    final result = await db.rawQuery('SELECT COUNT(*) FROM vehicles');
    return Sqflite.firstIntValue(result) ?? 0;
  }

  // ---------------------------------------------------------------------------
  // Fuel logs
  // ---------------------------------------------------------------------------
  Future<List<FuelLog>> getAllFuelLogs() async {
    final db = await _db;
    final rows = await db.query('fuel_logs', orderBy: 'dateMillis DESC');
    return rows.map(FuelLog.fromMap).toList();
  }

  Future<List<FuelLog>> getFuelLogsForVehicle(int vehicleId) async {
    final db = await _db;
    final rows = await db.query(
      'fuel_logs',
      where: 'vehicleId = ?',
      whereArgs: [vehicleId],
      orderBy: 'dateMillis DESC',
    );
    return rows.map(FuelLog.fromMap).toList();
  }

  /// Inserts a log and bumps the vehicle odometer when the new reading is higher.
  Future<int> insertFuelLog(FuelLog log) async {
    final db = await _db;
    final id = await db.insert('fuel_logs', log.toMap());
    final vehicle = await getVehicleById(log.vehicleId);
    if (vehicle != null && log.odometer > vehicle.currentOdometer) {
      await updateVehicle(vehicle.copyWith(currentOdometer: log.odometer));
    }
    return id;
  }

  Future<void> updateFuelLog(FuelLog log) async {
    final db = await _db;
    await db.update('fuel_logs', log.toMap(), where: 'id = ?', whereArgs: [log.id]);
  }

  Future<void> deleteFuelLog(FuelLog log) async {
    final db = await _db;
    await db.delete('fuel_logs', where: 'id = ?', whereArgs: [log.id]);
  }

  Future<int> getTotalFuelCost() async {
    final db = await _db;
    final result = await db.rawQuery('SELECT SUM(totalCost) FROM fuel_logs');
    return (Sqflite.firstIntValue(result) ?? 0);
  }

  Future<double> getTotalFuelLiters() async {
    final db = await _db;
    final result = await db.rawQuery('SELECT SUM(liters) FROM fuel_logs');
    if (result.isEmpty || result.first.values.first == null) return 0;
    return ((result.first.values.first) as num).toDouble();
  }

  // ---------------------------------------------------------------------------
  // Service reminders
  // ---------------------------------------------------------------------------
  Future<List<ServiceReminder>> getAllReminders() async {
    final db = await _db;
    final rows = await db.query('service_reminders', orderBy: 'targetDateMillis ASC');
    return rows.map(ServiceReminder.fromMap).toList();
  }

  Future<int> insertReminder(ServiceReminder reminder) async {
    final db = await _db;
    return db.insert('service_reminders', reminder.toMap());
  }

  Future<void> updateReminder(ServiceReminder reminder) async {
    final db = await _db;
    await db.update('service_reminders', reminder.toMap(), where: 'id = ?', whereArgs: [reminder.id]);
  }

  Future<void> deleteReminder(ServiceReminder reminder) async {
    final db = await _db;
    await db.delete('service_reminders', where: 'id = ?', whereArgs: [reminder.id]);
  }

  // ---------------------------------------------------------------------------
  // Service history
  // ---------------------------------------------------------------------------
  Future<List<ServiceHistory>> getAllServiceHistory() async {
    final db = await _db;
    final rows = await db.query('service_history', orderBy: 'dateMillis DESC');
    return rows.map(ServiceHistory.fromMap).toList();
  }

  Future<List<ServiceHistory>> getServiceHistoryForVehicle(int vehicleId) async {
    final db = await _db;
    final rows = await db.query(
      'service_history',
      where: 'vehicleId = ?',
      whereArgs: [vehicleId],
      orderBy: 'dateMillis DESC',
    );
    return rows.map(ServiceHistory.fromMap).toList();
  }

  Future<int> insertServiceHistory(ServiceHistory history) async {
    final db = await _db;
    return db.insert('service_history', history.toMap());
  }

  Future<void> updateServiceHistory(ServiceHistory history) async {
    final db = await _db;
    await db.update('service_history', history.toMap(), where: 'id = ?', whereArgs: [history.id]);
  }

  Future<void> deleteServiceHistory(ServiceHistory history) async {
    final db = await _db;
    await db.delete('service_history', where: 'id = ?', whereArgs: [history.id]);
  }

  Future<int> getTotalServiceCostForVehicle(int vehicleId) async {
    final db = await _db;
    final result = await db.rawQuery(
      'SELECT SUM(cost) FROM service_history WHERE vehicleId = ?',
      [vehicleId],
    );
    return Sqflite.firstIntValue(result) ?? 0;
  }

  // ---------------------------------------------------------------------------
  // Service requests (Bale bot)
  // ---------------------------------------------------------------------------
  Future<List<ServiceRequest>> getAllRequests() async {
    final db = await _db;
    final rows = await db.query('service_requests', orderBy: 'submissionDateMillis DESC');
    return rows.map(ServiceRequest.fromMap).toList();
  }

  /// Builds the notification payload, pushes it to Bale, then persists locally.
  Future<ServiceRequest> submitServiceRequest({
    required String requestType,
    required String title,
    required String fullName,
    required String nationalCode,
    required String phoneNumber,
    required String vehiclePlate,
    String vinCode = '',
    String barcode = '',
    String engineNumber = '',
    String chassisNumber = '',
    String postalCode = '',
    String address = '',
    String insuranceCategory = '',
    String insuranceCompany = '',
    int durationMonths = 12,
    int discountPercent = 0,
    String details = '',
  }) async {
    final entity = ServiceRequest(
      requestType: requestType,
      title: title,
      fullName: fullName,
      nationalCode: nationalCode,
      phoneNumber: phoneNumber,
      vehiclePlate: vehiclePlate,
      vinCode: vinCode,
      barcodeNumber: barcode,
      engineNumber: engineNumber,
      chassisNumber: chassisNumber,
      postalCode: postalCode,
      address: address,
      insuranceCategory: insuranceCategory,
      insuranceCompany: insuranceCompany,
      durationMonths: durationMonths,
      discountPercent: discountPercent,
      additionalDetails: details,
      status: ServiceRequest.statusPending,
      baleMessageId: 'LOCAL-PENDING',
      submissionDateMillis: DateTime.now().millisecondsSinceEpoch,
      updatedDateMillis: DateTime.now().millisecondsSinceEpoch,
    );

    final db = await _db;
    final id = await db.insert('service_requests', entity.toMap());
    final saved = ServiceRequest.fromMap({...entity.toMap(), 'id': id});

    final result = await _sendServiceRequestMessage(saved);
    if (!result.isLocalFallback) {
      await _setRequestBaleMessageId(id, result.messageId);
      return ServiceRequest.fromMap({
        ...saved.toMap(),
        'id': id,
        'baleMessageId': result.messageId,
      });
    }
    return saved;
  }

  Future<void> _setRequestBaleMessageId(int requestId, String messageId) async {
    final db = await _db;
    await db.update(
      'service_requests',
      {
        'baleMessageId': messageId,
        'updatedDateMillis': DateTime.now().millisecondsSinceEpoch,
      },
      where: 'id = ?',
      whereArgs: [requestId],
    );
  }

  Future<BaleSendResponse> _sendServiceRequestMessage(ServiceRequest request) async {
    final buffer = StringBuffer()
      ..writeln('📋 *درخواست جدید خدمات و استعلام خودرو*')
      ..writeln()
      ..writeln('🔹 *نوع خدمت:* ${request.requestType}')
      ..writeln('📌 *عنوان:* ${request.title}');
    if (request.insuranceCategory.isNotEmpty) {
      buffer.writeln('🛡 *دسته‌بندی بیمه:* ${request.insuranceCategory}');
    }
    if (request.insuranceCompany.isNotEmpty) {
      buffer
        ..writeln('🏢 *شرکت بیمه‌گر:* ${request.insuranceCompany}')
        ..writeln('⏳ *مدت اعتبار:* ${request.durationMonths} ماهه')
        ..writeln('🏷 *تخفیف عدم خسارت:* ${request.discountPercent}٪');
    }
    buffer
      ..writeln('👤 *اطلاعات فردی متقاضی:*')
      ..writeln('  • نام و نام خانوادگی: ${request.fullName}')
      ..writeln('  • کد ملی: ${request.nationalCode}')
      ..writeln('  • شماره تماس: ${request.phoneNumber}')
      ..writeln()
      ..writeln('🚗 *مشخصات کامل خودرو:*')
      ..writeln('  • پلاک: ${request.vehiclePlate}');
    if (request.vinCode.isNotEmpty) buffer.writeln('  • کد شناسایی (VIN): ${request.vinCode}');
    if (request.barcodeNumber.isNotEmpty) buffer.writeln('  • بارکد کارت خودرو: ${request.barcodeNumber}');
    if (request.engineNumber.isNotEmpty) buffer.writeln('  • شماره موتور: ${request.engineNumber}');
    if (request.chassisNumber.isNotEmpty) buffer.writeln('  • شماره شاسی: ${request.chassisNumber}');
    if (request.postalCode.isNotEmpty || request.address.isNotEmpty) {
      buffer
        ..writeln()
        ..writeln('📮 *اطلاعات پستی و سکونت:*');
      if (request.postalCode.isNotEmpty) buffer.writeln('  • کد پستی: ${request.postalCode}');
      if (request.address.isNotEmpty) buffer.writeln('  • آدرس: ${request.address}');
    }
    if (request.additionalDetails.isNotEmpty) {
      buffer
        ..writeln()
        ..writeln('📝 *توضیحات تکمیلی:* ${request.additionalDetails}');
    }
    buffer
      ..writeln()
      ..writeln('⏳ وضعیت: #در_انتظار_تایید_مدیر')
      ..write('📱 سامانه هوشمند مدیریت خدمات خودرو');
    return baleBotService.sendMessage(text: buffer.toString());
  }

  Future<int> retryPendingServiceRequests() async {
    var sent = 0;
    final pending = (await getAllRequests())
        .where((request) => request.baleMessageId.startsWith('LOCAL-'));
    for (final request in pending) {
      final result = await _sendServiceRequestMessage(request);
      if (!result.isLocalFallback) {
        await _setRequestBaleMessageId(request.id, result.messageId);
        sent++;
      }
    }
    return sent;
  }

  Future<void> updateRequestStatus(int requestId, String newStatus) async {
    final db = await _db;
    final rows =
        await db.query('service_requests', where: 'id = ?', whereArgs: [requestId], limit: 1);
    if (rows.isEmpty) return;
    final existing = ServiceRequest.fromMap(rows.first);
    await db.update(
      'service_requests',
      existing.copyWith(
        status: newStatus,
        updatedDateMillis: DateTime.now().millisecondsSinceEpoch,
      ).toMap(),
      where: 'id = ?',
      whereArgs: [requestId],
    );
  }

  Future<void> deleteRequest(ServiceRequest request) async {
    final db = await _db;
    await db.delete('service_requests', where: 'id = ?', whereArgs: [request.id]);
  }

  // ---------------------------------------------------------------------------
  // Inquiries & payments
  // ---------------------------------------------------------------------------
  Future<List<InquiryRecord>> getAllInquiries() async {
    final db = await _db;
    final rows = await db.query('inquiry_records', orderBy: 'dateMillis DESC');
    return rows.map(InquiryRecord.fromMap).toList();
  }

  Future<InquiryRecord> submitInquiry({
    required String inquiryType,
    required String title,
    required String plateNumber,
    required String barcodeOrVin,
    required String nationalId,
    required int amount,
    required String workflowMethod,
    String fullName = '',
    String phoneNumber = '',
    String vinCode = '',
    String barcode = '',
    String engineNumber = '',
    String chassisNumber = '',
    String postalCode = '',
    String address = '',
  }) async {
    // All inquiry requests use the Bale/admin workflow. There is no fake
    // direct-payment path in the offline-first client.
    const status = 'در حال بررسی توسط کارشناس';
    var ref = '';

    final effectiveVin = vinCode.isNotEmpty ? vinCode : barcodeOrVin;
    final effectiveBarcode = barcode.isNotEmpty ? barcode : barcodeOrVin;

    final record = InquiryRecord(
      inquiryType: inquiryType,
      title: title,
      plateNumber: plateNumber,
      barcodeOrVin: barcodeOrVin,
      nationalId: nationalId,
      fullName: fullName,
      phoneNumber: phoneNumber,
      vinCode: effectiveVin,
      barcode: effectiveBarcode,
      engineNumber: engineNumber,
      chassisNumber: chassisNumber,
      postalCode: postalCode,
      address: address,
      amount: amount,
      workflowMethod: workflowMethod,
      status: status,
      transactionRef: ref,
      dateMillis: DateTime.now().millisecondsSinceEpoch,
    );

    final db = await _db;
    final id = await db.insert(
      'inquiry_records',
      record.copyWith(
        transactionRef: ref.isNotEmpty ? ref : 'LOCAL-PENDING',
      ).toMap(),
    );
    final saved = InquiryRecord.fromMap({
      ...record.toMap(),
      'id': id,
      'transactionRef': ref.isNotEmpty ? ref : 'LOCAL-PENDING',
    });

    final result = await _sendInquiryMessage(saved);
    if (!result.isLocalFallback) {
      await _setInquiryRemoteRef(id, result.messageId);
      return InquiryRecord.fromMap({
        ...saved.toMap(),
        'id': id,
        'transactionRef': result.messageId,
      });
    }
    return saved;
  }

  Future<void> _setInquiryRemoteRef(int inquiryId, String ref) async {
    final db = await _db;
    await db.update(
      'inquiry_records',
      {'transactionRef': ref},
      where: 'id = ?',
      whereArgs: [inquiryId],
    );
  }

  Future<BaleSendResponse> _sendInquiryMessage(InquiryRecord inquiry) async {
    final buffer = StringBuffer()
      ..writeln('💳 *استعلام و تسویه عوارض و خلافی خودرو*')
      ..writeln()
      ..writeln('📑 *نوع استعلام:* ${inquiry.inquiryType}');
    if (inquiry.fullName.isNotEmpty) buffer.writeln('👤 *نام مالک:* ${inquiry.fullName}');
    if (inquiry.nationalId.isNotEmpty) buffer.writeln('🆔 *کد ملی:* ${inquiry.nationalId}');
    if (inquiry.phoneNumber.isNotEmpty) buffer.writeln('📱 *شماره تماس:* ${inquiry.phoneNumber}');
    buffer
      ..writeln()
      ..writeln('🚗 *مشخصات کامل خودرو:*')
      ..writeln('  • پلاک: ${inquiry.plateNumber}');
    if (inquiry.vinCode.isNotEmpty) buffer.writeln('  • کد شناسایی (VIN): ${inquiry.vinCode}');
    if (inquiry.barcode.isNotEmpty) buffer.writeln('  • بارکد کارت خودرو: ${inquiry.barcode}');
    if (inquiry.engineNumber.isNotEmpty) buffer.writeln('  • شماره موتور: ${inquiry.engineNumber}');
    if (inquiry.chassisNumber.isNotEmpty) buffer.writeln('  • شماره شاسی: ${inquiry.chassisNumber}');
    if (inquiry.postalCode.isNotEmpty || inquiry.address.isNotEmpty) {
      buffer
        ..writeln()
        ..writeln('📮 *مشخصات سکونت:*');
      if (inquiry.postalCode.isNotEmpty) buffer.writeln('  • کد پستی: ${inquiry.postalCode}');
      if (inquiry.address.isNotEmpty) buffer.writeln('  • آدرس: ${inquiry.address}');
    }
    buffer
      ..writeln()
      ..writeln('💰 *مبلغ برآوردی:* ${_formatInt(inquiry.amount)} تومان')
      ..writeln()
      ..write('لطفا پس از بررسی و تسویه، وضعیت را تایید بفرمایید.');
    return baleBotService.sendMessage(text: buffer.toString());
  }

  Future<int> retryPendingInquiries() async {
    var sent = 0;
    final pending = (await getAllInquiries()).where(
      (inquiry) => inquiry.transactionRef.startsWith('LOCAL-'),
    );
    for (final inquiry in pending) {
      final result = await _sendInquiryMessage(inquiry);
      if (!result.isLocalFallback) {
        await _setInquiryRemoteRef(inquiry.id, result.messageId);
        sent++;
      }
    }
    return sent;
  }

  Future<void> updateInquiry(InquiryRecord inquiry) async {
    final db = await _db;
    await db.update('inquiry_records', inquiry.toMap(), where: 'id = ?', whereArgs: [inquiry.id]);
  }

  Future<void> deleteInquiry(InquiryRecord inquiry) async {
    final db = await _db;
    await db.delete('inquiry_records', where: 'id = ?', whereArgs: [inquiry.id]);
  }

  // ---------------------------------------------------------------------------
  // Developer support / donation
  // ---------------------------------------------------------------------------
  Future<String> submitDonation({
    required int amount,
    required String customAmountText,
    required String payerName,
    required String payerPhone,
    required String note,
    Uint8List? photoBytes,
  }) async {
    final finalAmountText =
        amount > 0 ? '${_formatInt(amount)} تومان' : customAmountText;
    final buffer = StringBuffer()
      ..writeln('🎁 *رسید حمایت مالی جدید از برنامه‌نویس*')
      ..writeln()
      ..writeln('💰 *مبلغ حمایت:* $finalAmountText');
    if (payerName.isNotEmpty) buffer.writeln('👤 *نام پرداخت‌کننده:* $payerName');
    if (payerPhone.isNotEmpty) buffer.writeln('📞 *شماره تماس:* $payerPhone');
    if (note.isNotEmpty) {
      buffer
        ..writeln('📝 *پیام/یادداشت:* $note')
        ..writeln();
    }
    buffer
      ..writeln('💳 *شماره کارت واریز:* ۶۲۱۹-۸۶۱۹-۲۰۶۹-۶۲۰۹ (میلاد قنواتی)')
      ..write('📱 *برنامه:* مدیریت سوخت و استعلام خودرو');

    if (photoBytes != null && photoBytes.isNotEmpty) {
      final res = await baleBotService.sendPhoto(
        caption: buffer.toString(),
        photoBytes: photoBytes,
      );
      if (res.isLocalFallback) {
        await _enqueuePendingBale(
          kind: 'photo',
          payload: {'caption': buffer.toString(), 'file_name': 'support_receipt.jpg'},
          photoBytes: photoBytes,
        );
      }
      return res.messageId;
    }
    final res = await baleBotService.sendMessage(
      text: buffer.toString(),
    );
    if (res.isLocalFallback) {
      await _enqueuePendingBale(
        kind: 'message',
        payload: {'text': buffer.toString()},
      );
    }
    return res.messageId;
  }

  Future<void> _enqueuePendingBale({
    required String kind,
    required Map<String, dynamic> payload,
    Uint8List? photoBytes,
  }) async {
    final db = await _db;
    await db.insert('pending_bale_outbox', {
      'kind': kind,
      'payloadJson': jsonEncode(payload),
      'photoBase64': photoBytes == null ? '' : base64Encode(photoBytes),
      'createdAt': DateTime.now().millisecondsSinceEpoch,
    });
  }

  Future<int> retryPendingBaleOutbox() async {
    final db = await _db;
    final rows = await db.query(
      'pending_bale_outbox',
      orderBy: 'createdAt ASC',
      limit: 10,
    );
    var sent = 0;
    for (final row in rows) {
      try {
        final kind = (row['kind'] as String?) ?? 'message';
        final payloadJson = (row['payloadJson'] as String?) ?? '{}';
        final payload = jsonDecode(payloadJson) as Map<String, dynamic>;
        final base64Photo = (row['photoBase64'] as String?) ?? '';
        final result = kind == 'photo'
            ? await baleBotService.sendPhoto(
                caption: payload['caption']?.toString() ?? '',
                fileName: payload['file_name']?.toString() ?? 'support_receipt.jpg',
                photoBytes: base64Photo.isEmpty ? null : base64Decode(base64Photo),
              )
            : await baleBotService.sendMessage(
                text: payload['text']?.toString() ?? '',
              );
        if (!result.isLocalFallback) {
          await db.delete(
            'pending_bale_outbox',
            where: 'id = ?',
            whereArgs: [row['id']],
          );
          sent++;
        }
      } catch (_) {
        // Keep the item for the next online attempt.
      }
    }
    return sent;
  }

  // ---------------------------------------------------------------------------
  // Cloud sync
  // ---------------------------------------------------------------------------
  Future<Map<String, dynamic>> buildLocalSnapshot() async {
    final db = await _db;
    final tables = <String, List<Map<String, Object?>>>{};
    for (final table in const [
      'vehicles',
      'fuel_logs',
      'service_reminders',
      'service_history',
      'service_requests',
      'inquiry_records',
    ]) {
      final rows = await db.query(table);
      tables[table] = rows;
    }
    return {
      'schema_version': 1,
      'app': 'com.angelgirlbrand.sokhtandmodiriat',
      'created_at': DateTime.now().millisecondsSinceEpoch,
      'tables': tables,
    };
  }

  Future<bool> syncLocalToRailway() async {
    try {
      final snapshot = await buildLocalSnapshot();
      final ok = await railwaySyncService.syncSnapshot(
        deviceId: securityManager.getDeviceId(),
        snapshot: snapshot,
      );
      if (ok) await securityManager.markCloudSyncComplete();
      return ok;
    } catch (e) {
      debugLog('Cloud sync failed: $e');
      return false;
    }
  }

  Future<bool> restoreLocalFromRailwayIfEmpty() async {
    final db = await _db;
    final localCounts = <String, int>{};
    for (final table in const [
      'vehicles',
      'fuel_logs',
      'service_reminders',
      'service_history',
      'service_requests',
      'inquiry_records',
    ]) {
      final row = await db.rawQuery('SELECT COUNT(*) AS c FROM $table');
      localCounts[table] = (row.first['c'] as int?) ?? 0;
    }
    final hasLocalData = localCounts.values.any((count) => count > 0);
    if (hasLocalData) return false;

    final snapshot = await railwaySyncService.fetchSnapshot(
      deviceId: securityManager.getDeviceId(),
    );
    if (snapshot == null) return false;
    final rawTables = snapshot['tables'];
    if (rawTables is! Map<String, dynamic>) return false;

    final tableOrder = const [
      'vehicles',
      'fuel_logs',
      'service_reminders',
      'service_history',
      'service_requests',
      'inquiry_records',
    ];
    final batch = db.batch();
    for (final table in tableOrder.reversed) {
      batch.delete(table);
    }

    for (final table in tableOrder) {
      final rawRows = rawTables[table];
      if (rawRows is! List) continue;
      for (final item in rawRows) {
        if (item is! Map) continue;
        batch.insert(
          table,
          Map<String, Object?>.from(item),
          conflictAlgorithm: ConflictAlgorithm.replace,
        );
      }
    }
    await batch.commit(noResult: true);
    return true;
  }

  Future<String> syncAllToRailway(
    List<Vehicle> vehicles,
    List<FuelLog> logs,
  ) async {
    final ok = await syncLocalToRailway();
    return ok
        ? 'داده‌های محلی در Railway ذخیره و همگام شدند.'
        : 'اطلاعات در دستگاه باقی ماند و پس از اتصال دوباره همگام می‌شود.';
  }

  static String _formatInt(int value) {
    final s = value.toString();
    final buffer = StringBuffer();
    for (var i = 0; i < s.length; i++) {
      if (i > 0 && (s.length - i) % 3 == 0) buffer.write(',');
      buffer.write(s[i]);
    }
    return buffer.toString();
  }
}

/// Debug-only helper retained for parity with the original repository surface.
void debugLog(Object? message) {
  if (kDebugMode) debugPrint('$message');
}
