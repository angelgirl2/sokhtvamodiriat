import 'package:flutter/foundation.dart';

import 'data/local_db.dart';
import 'data/models.dart';
import 'data/security.dart';
import 'data/services.dart';

class AppController extends ChangeNotifier {
  AppController({
    required this.db,
    required this.repository,
    required this.security,
  });

  final LocalDatabase db;
  final AppRepository repository;
  final SecurityService security;

  List<Vehicle> vehicles = [];
  List<FuelLog> fuelLogs = [];
  List<ServiceReminder> reminders = [];
  List<ServiceHistory> serviceHistory = [];
  List<ServiceRequest> requests = [];
  List<InquiryRecord> inquiries = [];
  int? selectedVehicleId;
  bool busy = false;
  String statusMessage = '';
  bool isRailwayOnline = false;
  int tab = 0;
  bool showSplash = true;
  bool unlocked = true;

  Future<void> load() async {
    vehicles = await repository.vehicles();
    fuelLogs = await repository.fuelLogs();
    reminders = await repository.reminders();
    serviceHistory = await repository.serviceHistory();
    requests = await repository.requests();
    inquiries = await repository.inquiries();
    selectedVehicleId ??= vehicles.isNotEmpty ? vehicles.first.id : null;
    unlocked = !security.isPinEnabled;
    isRailwayOnline = await repository.railway.health();
    notifyListeners();
    await repository.flushQueue();
  }

  Vehicle? get activeVehicle => vehicles.firstWhereOrNull((v) => v.id == selectedVehicleId) ?? (vehicles.isNotEmpty ? vehicles.first : null);
  List<FuelLog> get activeFuelLogs => activeVehicle == null ? fuelLogs : fuelLogs.where((l) => l.vehicleId == activeVehicle!.id).toList();
  int get totalFuelCost => fuelLogs.fold(0, (sum, e) => sum + e.totalCost);
  double get totalLiters => fuelLogs.fold(0, (sum, e) => sum + e.liters);
  int get pendingRequests => requests.where((r) => r.status.contains('در انتظار') || r.status.contains('بررسی')).length;

  Future<void> afterChange() async {
    vehicles = await repository.vehicles();
    fuelLogs = await repository.fuelLogs();
    reminders = await repository.reminders();
    serviceHistory = await repository.serviceHistory();
    requests = await repository.requests();
    inquiries = await repository.inquiries();
    if (vehicles.isEmpty) {
      selectedVehicleId = null;
    } else if (!vehicles.any((e) => e.id == selectedVehicleId)) {
      selectedVehicleId = vehicles.first.id;
    }
    notifyListeners();
  }

  void refresh() => notifyListeners();

  void selectVehicle(int? id) {
    selectedVehicleId = id;
    notifyListeners();
  }

  Future<void> addVehicle(Vehicle v) async {
    busy = true;
    notifyListeners();
    final id = await repository.addVehicle(v);
    selectedVehicleId = id;
    await afterChange();
    busy = false;
    notifyListeners();
  }

  Future<void> updateVehicle(Vehicle v) async {
    await repository.updateVehicle(v);
    await afterChange();
  }

  Future<void> deleteVehicle(Vehicle v) async {
    if (v.id != null) await repository.deleteVehicle(v.id!);
    await afterChange();
  }

  Future<void> addFuelLog(FuelLog log) async {
    await repository.addFuelLog(log);
    final v = vehicles.firstWhereOrNull((e) => e.id == log.vehicleId);
    if (v != null && log.odometer > v.currentOdometer) {
      await repository.updateVehicle(v.copyWith(currentOdometer: log.odometer));
    }
    await afterChange();
  }

  Future<void> deleteFuelLog(FuelLog log) async {
    if (log.id != null) await repository.deleteFuelLog(log.id!);
    await afterChange();
  }

  Future<void> addReminder(ServiceReminder item) async {
    await repository.addReminder(item);
    await afterChange();
  }

  Future<void> toggleReminder(ServiceReminder item) async {
    await repository.updateReminder(item.copyWith(isCompleted: !item.isCompleted));
    await afterChange();
  }

  Future<void> deleteReminder(ServiceReminder item) async {
    if (item.id != null) await repository.deleteReminder(item.id!);
    await afterChange();
  }

  Future<void> addHistory(ServiceHistory item) async {
    await repository.addServiceHistory(item);
    await afterChange();
  }

  Future<void> deleteHistory(ServiceHistory item) async {
    if (item.id != null) await repository.deleteServiceHistory(item.id!);
    await afterChange();
  }

  Future<void> submitServiceRequest(ServiceRequest request) async {
    busy = true;
    statusMessage = 'در حال ثبت درخواست در Railway و ارسال به صف بله...';
    notifyListeners();
    final msgId = await repository.submitServiceRequest(request);
    statusMessage = 'درخواست ثبت شد. کد پیگیری: $msgId';
    await afterChange();
    busy = false;
    notifyListeners();
  }

  Future<void> deleteRequest(ServiceRequest request) async {
    if (request.id != null) await repository.deleteRequest(request.id!);
    await afterChange();
  }

  Future<void> approveRequest(ServiceRequest request) async {
    if (request.id == null) return;
    const status = 'تایید شد و برای شما اطلاعات ارسال میگردد';
    await repository.updateRequest(request.copyWith(status: status));
    await repository.updateRequestStatus(
      requestId: request.id!,
      baleMessageId: request.baleMessageId,
      status: status,
    );
    await afterChange();
  }

  Future<void> submitInquiry(InquiryRecord inquiry) async {
    busy = true;
    statusMessage = 'در حال ثبت استعلام...';
    notifyListeners();
    final ref = await repository.submitInquiry(inquiry);
    statusMessage = ref.isEmpty ? 'استعلام ثبت شد.' : 'استعلام به کارشناس ارسال شد؛ کد پیگیری $ref';
    await afterChange();
    busy = false;
    notifyListeners();
  }

  Future<void> directPay(InquiryRecord inquiry) async {
    busy = true;
    notifyListeners();
    await Future<void>.delayed(const Duration(milliseconds: 1200));
    final ref = 'SHP-${10000000 + DateTime.now().millisecondsSinceEpoch % 90000000}';
    await repository.updateInquiry(inquiry.copyWith(status: 'پرداخت شد و تسویه گردید', transactionRef: ref));
    statusMessage = 'پرداخت با شماره پیگیری $ref با موفقیت ثبت شد.';
    await afterChange();
    busy = false;
    notifyListeners();
  }

  Future<void> approveInquiry(InquiryRecord item) async {
    final updated = item.copyWith(
      status: 'پرداخت شد و تسویه گردید',
      transactionRef: item.transactionRef.isEmpty ? 'ADMIN-${DateTime.now().millisecondsSinceEpoch}' : item.transactionRef,
    );
    await repository.updateInquiry(updated);
    if (item.id != null) {
      await repository.railway.updateRequestStatus(
        requestId: item.id!,
        baleMessageId: item.transactionRef,
        status: updated.status,
      );
    }
    await afterChange();
  }

  Future<void> deleteInquiry(InquiryRecord item) async {
    if (item.id != null) await repository.deleteInquiry(item.id!);
    await afterChange();
  }

  Future<void> donation({
    required int amount,
    required String customAmountText,
    required String payerName,
    required String payerPhone,
    required String note,
    Uint8List? photoBytes,
  }) async {
    busy = true;
    statusMessage = 'رسید و اطلاعات حمایت در حال ارسال به Railway است...';
    notifyListeners();
    final id = await repository.sendDonation(
      amount: amount,
      customAmountText: customAmountText,
      payerName: payerName,
      payerPhone: payerPhone,
      note: note,
      photoBytes: photoBytes,
    );
    statusMessage = 'رسید در صف بله ثبت شد. کد: $id';
    busy = false;
    notifyListeners();
  }

  Future<void> sync() async {
    busy = true;
    notifyListeners();
    statusMessage = await repository.syncEverything();
    isRailwayOnline = await repository.railway.health();
    busy = false;
    notifyListeners();
  }

  Future<void> checkRequest(ServiceRequest request) async {
    if (request.id == null) return;
    final status = await repository.railway.requestStatus(request.id!, request.baleMessageId);
    await repository.updateRequest(request.copyWith(status: status));
    await afterChange();
  }

  Future<void> lock() async {
    if (security.isPinEnabled) {
      unlocked = false;
      notifyListeners();
    }
  }

  Future<bool> unlockWithPin(String pin) async {
    if (security.verifyPin(pin)) {
      unlocked = true;
      notifyListeners();
      return true;
    }
    return false;
  }

  Future<bool> unlockBiometric() async {
    if (!security.isBiometricEnabled) return false;
    final ok = await security.authenticate();
    if (ok) {
      unlocked = true;
      notifyListeners();
    }
    return ok;
  }
}

extension FirstWhereOrNull<E> on Iterable<E> {
  E? firstWhereOrNull(bool Function(E e) test) {
    for (final e in this) {
      if (test(e)) return e;
    }
    return null;
  }
}
