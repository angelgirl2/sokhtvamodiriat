import 'dart:async';

import 'package:flutter/services.dart';

import 'package:flutter/foundation.dart';
import 'package:printing/printing.dart';
import 'package:share_plus/share_plus.dart';

import 'core/app_colors.dart';
import 'data/app_repository.dart';
import 'data/models.dart';
import 'data/notification_helper.dart';
import 'data/security_manager.dart';
import 'services/report_exporter.dart';

class AppState extends ChangeNotifier {
  AppState({
    required this.repository,
    required this.securityManager,
    required this.notificationHelper,
  }) {
    currentThemeColor = securityManager.getSelectedThemeColor();
    darkModePref = securityManager.getDarkModePref();
    _cloudSyncTimer = Timer.periodic(
      const Duration(seconds: 45),
      (_) => unawaited(_backgroundCloudSync()),
    );
  }

  final AppRepository repository;
  final SecurityManager securityManager;
  final NotificationHelper notificationHelper;

  List<Vehicle> vehicles = const [];
  List<FuelLog> fuelLogs = const [];
  List<ServiceReminder> reminders = const [];
  List<ServiceHistory> serviceHistory = const [];
  List<ServiceRequest> serviceRequests = const [];
  List<InquiryRecord> inquiries = const [];

  AppThemeColor currentThemeColor = AppThemeColor.skyBlue;
  DarkModePref darkModePref = DarkModePref.system;

  int? selectedVehicleId;
  bool loading = true;
  bool isSubmitting = false;
  String? submissionMessage;
  String? syncStatus;
  late final Timer _cloudSyncTimer;
  bool _syncInFlight = false;

  Vehicle? get activeVehicle {
    if (vehicles.isEmpty) return null;
    if (selectedVehicleId == null) return vehicles.first;
    return vehicles
        .where((v) => v.id == selectedVehicleId)
        .cast<Vehicle?>()
        .firstWhere((v) => v != null, orElse: () => vehicles.first);
  }

  int get totalCost =>
      fuelLogs.fold<int>(0, (sum, item) => sum + item.totalCost);
  double get totalLiters =>
      fuelLogs.fold<double>(0, (sum, item) => sum + item.liters);

  Future<void> load() async {
    loading = true;
    notifyListeners();
    // A fresh installation can restore its own Railway snapshot when local
    // SQLite is empty. Existing local data always remains authoritative first.
    await repository.restoreLocalFromRailwayIfEmpty();
    await refreshAll();
    await repository.retryPendingServiceRequests();
    await repository.retryPendingInquiries();
    await repository.retryPendingBaleOutbox();
    await refreshAll();
    await _backgroundCloudSync();
    loading = false;
    notifyListeners();
  }

  Future<void> refreshAll() async {
    vehicles = await repository.getAllVehicles();
    fuelLogs = await repository.getAllFuelLogs();
    reminders = await repository.getAllReminders();
    serviceHistory = await repository.getAllServiceHistory();
    serviceRequests = await repository.getAllRequests();
    inquiries = await repository.getAllInquiries();
    if (selectedVehicleId == null && vehicles.isNotEmpty) {
      selectedVehicleId = vehicles.first.id;
    } else if (selectedVehicleId != null &&
        vehicles.every((v) => v.id != selectedVehicleId)) {
      selectedVehicleId = vehicles.isEmpty ? null : vehicles.first.id;
    }
    notifyListeners();
  }

  void clearMessage() {
    submissionMessage = null;
    syncStatus = null;
    notifyListeners();
  }

  void selectVehicle(int? id) {
    selectedVehicleId = id;
    notifyListeners();
  }

  Future<void> setThemeColor(AppThemeColor color) async {
    currentThemeColor = color;
    await securityManager.setSelectedThemeColor(color);
    notifyListeners();
  }

  Future<void> setDarkModePref(DarkModePref pref) async {
    darkModePref = pref;
    await securityManager.setDarkModePref(pref);
    notifyListeners();
  }

  Future<void> addVehicle({
    required String title,
    required String plateFirst2,
    required String plateLetter,
    required String plateLast3,
    required String plateCityCode,
    required String fuelType,
    required double tankCapacity,
    required int odometer,
  }) async {
    final vehicle = Vehicle(
      title: title.trim(),
      plateFirst2: plateFirst2.trim(),
      plateLetter: plateLetter.trim(),
      plateLast3: plateLast3.trim(),
      plateCityCode: plateCityCode.trim(),
      fuelType: fuelType,
      tankCapacity: tankCapacity,
      currentOdometer: odometer,
      createdAt: DateTime.now().millisecondsSinceEpoch,
    );
    final id = await repository.insertVehicle(vehicle);
    selectedVehicleId ??= id;
    await securityManager.markCloudSyncDirty();
    await refreshAll();
  }

  Future<void> updateVehicle(Vehicle vehicle) async {
    await repository.updateVehicle(vehicle);
    await securityManager.markCloudSyncDirty();
    await refreshAll();
  }

  Future<void> updateVehicleInsurance({
    required Vehicle vehicle,
    required int expiryMillis,
    required String company,
    required String type,
  }) async {
    await repository.updateVehicle(
      vehicle.copyWith(
        insuranceExpiryMillis: expiryMillis,
        insuranceCompany: company.trim(),
        insuranceType: type.trim(),
      ),
    );
    await securityManager.markCloudSyncDirty();
    await refreshAll();
  }

  Future<void> updateVehicleInspection({
    required Vehicle vehicle,
    required int expiryMillis,
    required String centerName,
    required int notifyDaysBefore,
  }) async {
    final updated = vehicle.copyWith(
      inspectionExpiryMillis: expiryMillis,
      inspectionCenter: centerName.trim(),
      inspectionNotifyDaysBefore: notifyDaysBefore,
    );
    await repository.updateVehicle(updated);

    final daysRemaining =
        ((expiryMillis - DateTime.now().millisecondsSinceEpoch) /
                Duration.millisecondsPerDay)
            .floor();
    final message = daysRemaining < 0
        ? 'معاینه فنی خودروی ${vehicle.title} (${vehicle.formattedPlate}) منقضی شده است! نسبت به تمدید آن اقدام فرمایید.'
        : daysRemaining <= notifyDaysBefore
        ? 'هشدار: تنها $daysRemaining روز تا تاریخ انقضای معاینه فنی خودروی ${vehicle.title} باقی مانده است.'
        : 'یادآور تاریخ انقضای معاینه فنی خودروی ${vehicle.title} برای موعد مقرر با موفقیت تنظیم شد ($notifyDaysBefore روز قبل هشدار داده می‌شود).';

    await notificationHelper.showServiceAlert(
      title: 'معاینه فنی خودرو ${vehicle.title}',
      message: message,
    );
    await securityManager.markCloudSyncDirty();
    await refreshAll();
  }

  Future<void> deleteVehicle(Vehicle vehicle) async {
    await repository.deleteVehicle(vehicle);
    if (selectedVehicleId == vehicle.id) selectedVehicleId = null;
    await securityManager.markCloudSyncDirty();
    await refreshAll();
  }

  Future<void> addFuelLog({
    required int vehicleId,
    required int odometer,
    required double liters,
    required int pricePerLiter,
    required String stationName,
    required bool isFullTank,
    required String notes,
  }) async {
    final totalCost = (liters * pricePerLiter).round();
    await repository.insertFuelLog(
      FuelLog(
        vehicleId: vehicleId,
        dateMillis: DateTime.now().millisecondsSinceEpoch,
        odometer: odometer,
        liters: liters,
        pricePerLiter: pricePerLiter,
        totalCost: totalCost,
        stationName: stationName.trim(),
        isFullTank: isFullTank,
        notes: notes.trim(),
      ),
    );
    await securityManager.markCloudSyncDirty();
    await refreshAll();
  }

  Future<void> deleteFuelLog(FuelLog log) async {
    await repository.deleteFuelLog(log);
    await securityManager.markCloudSyncDirty();
    await refreshAll();
  }

  Future<void> addServiceReminder({
    required int vehicleId,
    required String serviceType,
    required int targetDateMillis,
    required int targetOdometer,
    required String notes,
  }) async {
    await repository.insertReminder(
      ServiceReminder(
        vehicleId: vehicleId,
        serviceType: serviceType,
        targetDateMillis: targetDateMillis,
        targetOdometer: targetOdometer,
        notes: notes,
      ),
    );
    await notificationHelper.showServiceAlert(
      title: serviceType,
      message:
          'یادآور سرویس $serviceType برای کیلومتر $targetOdometer با موفقیت در سامانه فعال شد.',
    );
    await securityManager.markCloudSyncDirty();
    await refreshAll();
  }

  Future<void> toggleReminderCompleted(ServiceReminder reminder) async {
    await repository.updateReminder(
      reminder.copyWith(isCompleted: !reminder.isCompleted),
    );
    await securityManager.markCloudSyncDirty();
    await refreshAll();
  }

  Future<void> deleteReminder(ServiceReminder reminder) async {
    await repository.deleteReminder(reminder);
    await securityManager.markCloudSyncDirty();
    await refreshAll();
  }

  Future<void> addServiceHistory({
    required int vehicleId,
    required String serviceType,
    required String itemsChanged,
    required int odometer,
    required int nextDueOdometer,
    required int dateMillis,
    required int cost,
    required String mechanicOrShop,
    required String notes,
  }) async {
    await repository.insertServiceHistory(
      ServiceHistory(
        vehicleId: vehicleId,
        serviceType: serviceType.trim(),
        itemsChanged: itemsChanged.trim(),
        odometer: odometer,
        nextDueOdometer: nextDueOdometer,
        dateMillis: dateMillis,
        cost: cost,
        mechanicOrShop: mechanicOrShop.trim(),
        notes: notes.trim(),
      ),
    );
    final vehicle = vehicles
        .where((v) => v.id == vehicleId)
        .cast<Vehicle?>()
        .firstWhere((v) => v != null, orElse: () => null);
    if (vehicle != null && odometer > vehicle.currentOdometer) {
      await repository.updateVehicle(
        vehicle.copyWith(currentOdometer: odometer),
      );
    }
    await securityManager.markCloudSyncDirty();
    await refreshAll();
  }

  Future<void> deleteServiceHistory(ServiceHistory history) async {
    await repository.deleteServiceHistory(history);
    await securityManager.markCloudSyncDirty();
    await refreshAll();
  }

  Future<void> submitServiceRequest({
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
    isSubmitting = true;
    notifyListeners();
    try {
      await repository.submitServiceRequest(
        requestType: requestType,
        title: title,
        fullName: fullName,
        nationalCode: nationalCode,
        phoneNumber: phoneNumber,
        vehiclePlate: vehiclePlate,
        vinCode: vinCode,
        barcode: barcode,
        engineNumber: engineNumber,
        chassisNumber: chassisNumber,
        postalCode: postalCode,
        address: address,
        insuranceCategory: insuranceCategory,
        insuranceCompany: insuranceCompany,
        durationMonths: durationMonths,
        discountPercent: discountPercent,
        details: details,
      );
      submissionMessage =
          'درخواست شما با موفقیت ثبت شد و در انتظار تایید کارشناس قرار گرفت.';
      await securityManager.markCloudSyncDirty();
      await refreshAll();
    } catch (e) {
      submissionMessage = 'خطا در ارسال: $e';
      notifyListeners();
    } finally {
      isSubmitting = false;
      notifyListeners();
    }
  }

  Future<void> approveRequest(int requestId) async {
    await repository.updateRequestStatus(
      requestId,
      ServiceRequest.statusApproved,
    );
    submissionMessage =
        'وضعیت به: «${ServiceRequest.statusApproved}» به‌روزرسانی شد.';
    await securityManager.markCloudSyncDirty();
    await refreshAll();
  }

  Future<void> deleteRequest(ServiceRequest request) async {
    await repository.deleteRequest(request);
    await securityManager.markCloudSyncDirty();
    await refreshAll();
  }

  Future<void> submitInquiry({
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
    isSubmitting = true;
    notifyListeners();
    try {
      await repository.submitInquiry(
        inquiryType: inquiryType,
        title: title,
        plateNumber: plateNumber,
        barcodeOrVin: barcodeOrVin,
        nationalId: nationalId,
        amount: amount,
        workflowMethod: workflowMethod,
        fullName: fullName,
        phoneNumber: phoneNumber,
        vinCode: vinCode,
        barcode: barcode,
        engineNumber: engineNumber,
        chassisNumber: chassisNumber,
        postalCode: postalCode,
        address: address,
      );
      submissionMessage =
          'درخواست استعلام ثبت شد و برای ربات بله ارسال می‌شود؛ در صورت قطع اینترنت در صف محلی می‌ماند.';
      await securityManager.markCloudSyncDirty();
      await refreshAll();
    } catch (e) {
      submissionMessage = 'خطا: $e';
      notifyListeners();
    } finally {
      isSubmitting = false;
      notifyListeners();
    }
  }

  Future<void> approveInquiry(int inquiryId) async {
    final inquiry = inquiries
        .where((i) => i.id == inquiryId)
        .cast<InquiryRecord?>()
        .firstWhere((v) => v != null, orElse: () => null);
    if (inquiry == null) return;
    await repository.updateInquiry(
      inquiry.copyWith(
        status: 'تسویه و انجام شد (تایید مدیر)',
        transactionRef: SecurityManager.randomRef('ADM'),
      ),
    );
    submissionMessage = 'استعلام و تسویه توسط مدیر تایید و اطلاعات ثبت گردید.';
    await securityManager.markCloudSyncDirty();
    await refreshAll();
  }

  Future<void> processDirectPayment(InquiryRecord inquiry) async {
    isSubmitting = true;
    notifyListeners();
    await Future<void>.delayed(const Duration(milliseconds: 1200));
    final ref = SecurityManager.randomRef('SHP');
    await repository.updateInquiry(
      inquiry.copyWith(status: 'پرداخت شد و تسویه گردید', transactionRef: ref),
    );
    isSubmitting = false;
    submissionMessage =
        'پرداخت مبلغ ${inquiry.amount} تومان با شماره پیگیری $ref با موفقیت تسویه گردید.';
    await securityManager.markCloudSyncDirty();
    await refreshAll();
  }

  Future<void> deleteInquiry(InquiryRecord inquiry) async {
    await repository.deleteInquiry(inquiry);
    await securityManager.markCloudSyncDirty();
    await refreshAll();
  }

  Future<void> sendDonationSupport({
    required int amount,
    required String customAmountText,
    required String payerName,
    required String payerPhone,
    required String note,
    Uint8List? photoBytes,
  }) async {
    isSubmitting = true;
    notifyListeners();
    try {
      final messageId = await repository.submitDonation(
        amount: amount,
        customAmountText: customAmountText,
        payerName: payerName,
        payerPhone: payerPhone,
        note: note,
        photoBytes: photoBytes,
      );
      submissionMessage =
          'فیش و اطلاعات حمایت مالی با موفقیت به ربات ارسال شد (کد: $messageId). از همراهی و لطف شما صمیمانه متشکریم!';
    } catch (e) {
      submissionMessage = 'خطا در ارسال: $e';
    } finally {
      isSubmitting = false;
      notifyListeners();
    }
  }

  Future<void> syncWithRailway() async {
    isSubmitting = true;
    notifyListeners();
    await securityManager.markCloudSyncDirty();
    final remoteSent = await repository.retryPendingServiceRequests() +
        await repository.retryPendingInquiries() +
        await repository.retryPendingBaleOutbox();
    syncStatus = await repository.syncAllToRailway(vehicles, fuelLogs);
    if (remoteSent > 0) {
      syncStatus = '$syncStatus\n$remoteSent درخواست معوق به سرور ارسال شد.';
    }
    isSubmitting = false;
    notifyListeners();
  }

  Future<void> _backgroundCloudSync() async {
    if (_syncInFlight || !securityManager.isCloudSyncDirty) return;
    _syncInFlight = true;
    try {
      final remoteSent = await repository.retryPendingServiceRequests() +
          await repository.retryPendingInquiries() +
          await repository.retryPendingBaleOutbox();
      final ok = await repository.syncLocalToRailway();
      if (ok && remoteSent > 0) {
        syncStatus = '$remoteSent درخواست معوق به سرور ارسال شد و همگام‌سازی انجام شد.';
        notifyListeners();
      }
    } finally {
      _syncInFlight = false;
    }
  }

  @override
  void dispose() {
    _cloudSyncTimer.cancel();
    super.dispose();
  }

  Future<void> exportPdf() async {
    final font = await PdfGoogleFonts.nunitoExtraLight();
    final bytes = await ReportExporter.buildPdfBytes(
      vehicles: vehicles,
      logs: fuelLogs,
      serviceHistory: serviceHistory,
      reminders: reminders,
      font: font,
    );
    await Printing.sharePdf(
      bytes: bytes,
      filename: 'Car_Report_${DateTime.now().millisecondsSinceEpoch}.pdf',
      subject: 'گزارش جامع PDF خودرو و سوخت',
      body: 'گزارش رسمی صادر شده از سامانه مدیریت سوخت و استعلام خودرو',
    );
  }

  Future<void> exportCsv() async {
    final bytes = ReportExporter.buildFuelCsvBytes(
      vehicles: vehicles,
      logs: fuelLogs,
    );
    final file = await ReportExporter.writeTemp(
      'Fuel_Logs_${DateTime.now().millisecondsSinceEpoch}.csv',
      bytes,
    );
    await SharePlus.instance.share(
      ShareParams(
        files: [XFile(file.path)],
        title: 'فایل اکسل مصرف سوخت',
        subject: 'فایل اکسل مصرف سوخت',
        text: 'فایل CSV سازگار با Excel از سامانه مدیریت سوخت',
      ),
    );
  }

  Future<void> shareReportText() async {
    final vehicle = activeVehicle;
    final vehicleLogs = vehicle == null
        ? fuelLogs
        : fuelLogs.where((e) => e.vehicleId == vehicle.id).toList();
    final vehicleHistory = vehicle == null
        ? serviceHistory
        : serviceHistory.where((e) => e.vehicleId == vehicle.id).toList();
    final vehicleReminders = vehicle == null
        ? reminders
        : reminders.where((e) => e.vehicleId == vehicle.id).toList();
    final text = ReportExporter.generateFullDossierTextReport(
      vehicle: vehicle,
      logs: vehicleLogs,
      serviceHistory: vehicleHistory,
      reminders: vehicleReminders,
    );
    await SharePlus.instance.share(
      ShareParams(
        text: text,
        title: 'گزارش متنی مصرف سوخت و سرویس‌های خودرو',
        subject: 'گزارش متنی مصرف سوخت و سرویس‌های خودرو',
      ),
    );
  }

  Future<void> copyReportText() async {
    final vehicle = activeVehicle;
    final vehicleLogs = vehicle == null
        ? fuelLogs
        : fuelLogs.where((e) => e.vehicleId == vehicle.id).toList();
    final vehicleHistory = vehicle == null
        ? serviceHistory
        : serviceHistory.where((e) => e.vehicleId == vehicle.id).toList();
    final vehicleReminders = vehicle == null
        ? reminders
        : reminders.where((e) => e.vehicleId == vehicle.id).toList();
    final text = ReportExporter.generateFullDossierTextReport(
      vehicle: vehicle,
      logs: vehicleLogs,
      serviceHistory: vehicleHistory,
      reminders: vehicleReminders,
    );
    await Clipboard.setData(ClipboardData(text: text));
  }
}
