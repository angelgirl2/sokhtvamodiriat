/// Data models mirroring the original Room entities 1:1.
///
/// Naming, default values and status constants are kept identical to the
/// Kotlin entities so the port stays behaviour-compatible.
library;

class Vehicle {
  final int id;
  final String title;
  final String plateFirst2;
  final String plateLetter;
  final String plateLast3;
  final String plateCityCode;
  final String fuelType;
  final double tankCapacity;
  final int currentOdometer;
  final int insuranceExpiryMillis;
  final String insuranceCompany;
  final String insuranceType;
  final int inspectionExpiryMillis;
  final String inspectionCenter;
  final int inspectionNotifyDaysBefore;
  final int createdAt;

  const Vehicle({
    this.id = 0,
    required this.title,
    required this.plateFirst2,
    required this.plateLetter,
    required this.plateLast3,
    required this.plateCityCode,
    required this.fuelType,
    required this.tankCapacity,
    required this.currentOdometer,
    this.insuranceExpiryMillis = 0,
    this.insuranceCompany = 'بیمه ایران',
    this.insuranceType = 'بیمه شخص ثالث',
    this.inspectionExpiryMillis = 0,
    this.inspectionCenter = '',
    this.inspectionNotifyDaysBefore = 15,
    this.createdAt = 0,
  });

  /// `ایران 11 | 345 ب 12`
  String get formattedPlate => 'ایران $plateCityCode | $plateLast3 $plateLetter $plateFirst2';

  int get effectiveInsuranceExpiryMillis => insuranceExpiryMillis > 0
      ? insuranceExpiryMillis
      : (createdAt > 0 ? createdAt : DateTime.now().millisecondsSinceEpoch) +
          const Duration(days: 365).inMilliseconds;

  int get effectiveInspectionExpiryMillis => inspectionExpiryMillis > 0
      ? inspectionExpiryMillis
      : (createdAt > 0 ? createdAt : DateTime.now().millisecondsSinceEpoch) +
          const Duration(days: 365).inMilliseconds;

  Vehicle copyWith({
    int? id,
    String? title,
    String? plateFirst2,
    String? plateLetter,
    String? plateLast3,
    String? plateCityCode,
    String? fuelType,
    double? tankCapacity,
    int? currentOdometer,
    int? insuranceExpiryMillis,
    String? insuranceCompany,
    String? insuranceType,
    int? inspectionExpiryMillis,
    String? inspectionCenter,
    int? inspectionNotifyDaysBefore,
    int? createdAt,
  }) {
    return Vehicle(
      id: id ?? this.id,
      title: title ?? this.title,
      plateFirst2: plateFirst2 ?? this.plateFirst2,
      plateLetter: plateLetter ?? this.plateLetter,
      plateLast3: plateLast3 ?? this.plateLast3,
      plateCityCode: plateCityCode ?? this.plateCityCode,
      fuelType: fuelType ?? this.fuelType,
      tankCapacity: tankCapacity ?? this.tankCapacity,
      currentOdometer: currentOdometer ?? this.currentOdometer,
      insuranceExpiryMillis: insuranceExpiryMillis ?? this.insuranceExpiryMillis,
      insuranceCompany: insuranceCompany ?? this.insuranceCompany,
      insuranceType: insuranceType ?? this.insuranceType,
      inspectionExpiryMillis: inspectionExpiryMillis ?? this.inspectionExpiryMillis,
      inspectionCenter: inspectionCenter ?? this.inspectionCenter,
      inspectionNotifyDaysBefore: inspectionNotifyDaysBefore ?? this.inspectionNotifyDaysBefore,
      createdAt: createdAt ?? this.createdAt,
    );
  }

  Map<String, Object?> toMap() => {
        'id': id == 0 ? null : id,
        'title': title,
        'plateFirst2': plateFirst2,
        'plateLetter': plateLetter,
        'plateLast3': plateLast3,
        'plateCityCode': plateCityCode,
        'fuelType': fuelType,
        'tankCapacity': tankCapacity,
        'currentOdometer': currentOdometer,
        'insuranceExpiryMillis': insuranceExpiryMillis,
        'insuranceCompany': insuranceCompany,
        'insuranceType': insuranceType,
        'inspectionExpiryMillis': inspectionExpiryMillis,
        'inspectionCenter': inspectionCenter,
        'inspectionNotifyDaysBefore': inspectionNotifyDaysBefore,
        'createdAt': createdAt == 0 ? DateTime.now().millisecondsSinceEpoch : createdAt,
      };

  factory Vehicle.fromMap(Map<String, Object?> map) => Vehicle(
        id: (map['id'] as int?) ?? 0,
        title: (map['title'] as String?) ?? '',
        plateFirst2: (map['plateFirst2'] as String?) ?? '',
        plateLetter: (map['plateLetter'] as String?) ?? '',
        plateLast3: (map['plateLast3'] as String?) ?? '',
        plateCityCode: (map['plateCityCode'] as String?) ?? '',
        fuelType: (map['fuelType'] as String?) ?? 'بنزین معمولی',
        tankCapacity: ((map['tankCapacity'] as num?) ?? 45).toDouble(),
        currentOdometer: (map['currentOdometer'] as int?) ?? 0,
        insuranceExpiryMillis: (map['insuranceExpiryMillis'] as int?) ?? 0,
        insuranceCompany: (map['insuranceCompany'] as String?) ?? 'بیمه ایران',
        insuranceType: (map['insuranceType'] as String?) ?? 'بیمه شخص ثالث',
        inspectionExpiryMillis: (map['inspectionExpiryMillis'] as int?) ?? 0,
        inspectionCenter: (map['inspectionCenter'] as String?) ?? '',
        inspectionNotifyDaysBefore: (map['inspectionNotifyDaysBefore'] as int?) ?? 15,
        createdAt: (map['createdAt'] as int?) ?? 0,
      );
}

class FuelLog {
  final int id;
  final int vehicleId;
  final int dateMillis;
  final int odometer;
  final double liters;
  final int pricePerLiter;
  final int totalCost;
  final String stationName;
  final bool isFullTank;
  final String notes;

  const FuelLog({
    this.id = 0,
    required this.vehicleId,
    required this.dateMillis,
    required this.odometer,
    required this.liters,
    required this.pricePerLiter,
    required this.totalCost,
    required this.stationName,
    this.isFullTank = true,
    this.notes = '',
  });

  Map<String, Object?> toMap() => {
        'id': id == 0 ? null : id,
        'vehicleId': vehicleId,
        'dateMillis': dateMillis,
        'odometer': odometer,
        'liters': liters,
        'pricePerLiter': pricePerLiter,
        'totalCost': totalCost,
        'stationName': stationName,
        'isFullTank': isFullTank ? 1 : 0,
        'notes': notes,
      };

  factory FuelLog.fromMap(Map<String, Object?> map) => FuelLog(
        id: (map['id'] as int?) ?? 0,
        vehicleId: (map['vehicleId'] as int?) ?? 0,
        dateMillis: (map['dateMillis'] as int?) ?? 0,
        odometer: (map['odometer'] as int?) ?? 0,
        liters: ((map['liters'] as num?) ?? 0).toDouble(),
        pricePerLiter: (map['pricePerLiter'] as int?) ?? 0,
        totalCost: (map['totalCost'] as int?) ?? 0,
        stationName: (map['stationName'] as String?) ?? '',
        isFullTank: ((map['isFullTank'] as int?) ?? 1) == 1,
        notes: (map['notes'] as String?) ?? '',
      );
}

class ServiceReminder {
  final int id;
  final int vehicleId;
  final String serviceType;
  final int targetDateMillis;
  final int targetOdometer;
  final String notes;
  final bool isCompleted;
  final bool notified;

  const ServiceReminder({
    this.id = 0,
    required this.vehicleId,
    required this.serviceType,
    required this.targetDateMillis,
    required this.targetOdometer,
    this.notes = '',
    this.isCompleted = false,
    this.notified = false,
  });

  ServiceReminder copyWith({bool? isCompleted, bool? notified}) => ServiceReminder(
        id: id,
        vehicleId: vehicleId,
        serviceType: serviceType,
        targetDateMillis: targetDateMillis,
        targetOdometer: targetOdometer,
        notes: notes,
        isCompleted: isCompleted ?? this.isCompleted,
        notified: notified ?? this.notified,
      );

  Map<String, Object?> toMap() => {
        'id': id == 0 ? null : id,
        'vehicleId': vehicleId,
        'serviceType': serviceType,
        'targetDateMillis': targetDateMillis,
        'targetOdometer': targetOdometer,
        'notes': notes,
        'isCompleted': isCompleted ? 1 : 0,
        'notified': notified ? 1 : 0,
      };

  factory ServiceReminder.fromMap(Map<String, Object?> map) => ServiceReminder(
        id: (map['id'] as int?) ?? 0,
        vehicleId: (map['vehicleId'] as int?) ?? 0,
        serviceType: (map['serviceType'] as String?) ?? '',
        targetDateMillis: (map['targetDateMillis'] as int?) ?? 0,
        targetOdometer: (map['targetOdometer'] as int?) ?? 0,
        notes: (map['notes'] as String?) ?? '',
        isCompleted: ((map['isCompleted'] as int?) ?? 0) == 1,
        notified: ((map['notified'] as int?) ?? 0) == 1,
      );
}

class ServiceHistory {
  final int id;
  final int vehicleId;
  final String serviceType;
  final String itemsChanged;
  final int odometer;
  final int nextDueOdometer;
  final int dateMillis;
  final int cost;
  final String mechanicOrShop;
  final String notes;

  const ServiceHistory({
    this.id = 0,
    required this.vehicleId,
    required this.serviceType,
    required this.itemsChanged,
    required this.odometer,
    this.nextDueOdometer = 0,
    required this.dateMillis,
    required this.cost,
    this.mechanicOrShop = '',
    this.notes = '',
  });

  Map<String, Object?> toMap() => {
        'id': id == 0 ? null : id,
        'vehicleId': vehicleId,
        'serviceType': serviceType,
        'itemsChanged': itemsChanged,
        'odometer': odometer,
        'nextDueOdometer': nextDueOdometer,
        'dateMillis': dateMillis,
        'cost': cost,
        'mechanicOrShop': mechanicOrShop,
        'notes': notes,
      };

  factory ServiceHistory.fromMap(Map<String, Object?> map) => ServiceHistory(
        id: (map['id'] as int?) ?? 0,
        vehicleId: (map['vehicleId'] as int?) ?? 0,
        serviceType: (map['serviceType'] as String?) ?? '',
        itemsChanged: (map['itemsChanged'] as String?) ?? '',
        odometer: (map['odometer'] as int?) ?? 0,
        nextDueOdometer: (map['nextDueOdometer'] as int?) ?? 0,
        dateMillis: (map['dateMillis'] as int?) ?? 0,
        cost: (map['cost'] as int?) ?? 0,
        mechanicOrShop: (map['mechanicOrShop'] as String?) ?? '',
        notes: (map['notes'] as String?) ?? '',
      );
}

class ServiceRequest {
  static const statusPending = 'در انتظار تایید مدیر';
  static const statusApproved = 'تایید شد و برای شما اطلاعات ارسال میگردد';
  static const statusRejected = 'رد شده - نیاز به بررسی مجدد';

  final int id;
  final String requestType;
  final String title;
  final String fullName;
  final String nationalCode;
  final String phoneNumber;
  final String vehiclePlate;
  final String vinCode;
  final String barcodeNumber;
  final String engineNumber;
  final String chassisNumber;
  final String postalCode;
  final String address;
  final String insuranceCategory;
  final String insuranceCompany;
  final int durationMonths;
  final int discountPercent;
  final String additionalDetails;
  final String status;
  final String baleMessageId;
  final int submissionDateMillis;
  final int updatedDateMillis;

  const ServiceRequest({
    this.id = 0,
    required this.requestType,
    required this.title,
    required this.fullName,
    required this.nationalCode,
    required this.phoneNumber,
    required this.vehiclePlate,
    this.vinCode = '',
    this.barcodeNumber = '',
    this.engineNumber = '',
    this.chassisNumber = '',
    this.postalCode = '',
    this.address = '',
    this.insuranceCategory = '',
    this.insuranceCompany = '',
    this.durationMonths = 12,
    this.discountPercent = 0,
    this.additionalDetails = '',
    this.status = statusPending,
    this.baleMessageId = '',
    this.submissionDateMillis = 0,
    this.updatedDateMillis = 0,
  });

  ServiceRequest copyWith({String? status, String? baleMessageId, int? updatedDateMillis}) => ServiceRequest(
        id: id,
        requestType: requestType,
        title: title,
        fullName: fullName,
        nationalCode: nationalCode,
        phoneNumber: phoneNumber,
        vehiclePlate: vehiclePlate,
        vinCode: vinCode,
        barcodeNumber: barcodeNumber,
        engineNumber: engineNumber,
        chassisNumber: chassisNumber,
        postalCode: postalCode,
        address: address,
        insuranceCategory: insuranceCategory,
        insuranceCompany: insuranceCompany,
        durationMonths: durationMonths,
        discountPercent: discountPercent,
        additionalDetails: additionalDetails,
        status: status ?? this.status,
        baleMessageId: baleMessageId ?? this.baleMessageId,
        submissionDateMillis: submissionDateMillis,
        updatedDateMillis: updatedDateMillis ?? this.updatedDateMillis,
      );

  Map<String, Object?> toMap() => {
        'id': id == 0 ? null : id,
        'requestType': requestType,
        'title': title,
        'fullName': fullName,
        'nationalCode': nationalCode,
        'phoneNumber': phoneNumber,
        'vehiclePlate': vehiclePlate,
        'vinCode': vinCode,
        'barcodeNumber': barcodeNumber,
        'engineNumber': engineNumber,
        'chassisNumber': chassisNumber,
        'postalCode': postalCode,
        'address': address,
        'insuranceCategory': insuranceCategory,
        'insuranceCompany': insuranceCompany,
        'durationMonths': durationMonths,
        'discountPercent': discountPercent,
        'additionalDetails': additionalDetails,
        'status': status,
        'baleMessageId': baleMessageId,
        'submissionDateMillis':
            submissionDateMillis == 0 ? DateTime.now().millisecondsSinceEpoch : submissionDateMillis,
        'updatedDateMillis':
            updatedDateMillis == 0 ? DateTime.now().millisecondsSinceEpoch : updatedDateMillis,
      };

  factory ServiceRequest.fromMap(Map<String, Object?> map) => ServiceRequest(
        id: (map['id'] as int?) ?? 0,
        requestType: (map['requestType'] as String?) ?? '',
        title: (map['title'] as String?) ?? '',
        fullName: (map['fullName'] as String?) ?? '',
        nationalCode: (map['nationalCode'] as String?) ?? '',
        phoneNumber: (map['phoneNumber'] as String?) ?? '',
        vehiclePlate: (map['vehiclePlate'] as String?) ?? '',
        vinCode: (map['vinCode'] as String?) ?? '',
        barcodeNumber: (map['barcodeNumber'] as String?) ?? '',
        engineNumber: (map['engineNumber'] as String?) ?? '',
        chassisNumber: (map['chassisNumber'] as String?) ?? '',
        postalCode: (map['postalCode'] as String?) ?? '',
        address: (map['address'] as String?) ?? '',
        insuranceCategory: (map['insuranceCategory'] as String?) ?? '',
        insuranceCompany: (map['insuranceCompany'] as String?) ?? '',
        durationMonths: (map['durationMonths'] as int?) ?? 12,
        discountPercent: (map['discountPercent'] as int?) ?? 0,
        additionalDetails: (map['additionalDetails'] as String?) ?? '',
        status: (map['status'] as String?) ?? statusPending,
        baleMessageId: (map['baleMessageId'] as String?) ?? '',
        submissionDateMillis: (map['submissionDateMillis'] as int?) ?? 0,
        updatedDateMillis: (map['updatedDateMillis'] as int?) ?? 0,
      );
}

class InquiryRecord {
  final int id;
  final String inquiryType;
  final String title;
  final String plateNumber;
  final String barcodeOrVin;
  final String nationalId;
  final String fullName;
  final String phoneNumber;
  final String vinCode;
  final String barcode;
  final String engineNumber;
  final String chassisNumber;
  final String postalCode;
  final String address;
  final int amount;
  final String workflowMethod;
  final String status;
  final String transactionRef;
  final int dateMillis;

  const InquiryRecord({
    this.id = 0,
    required this.inquiryType,
    required this.title,
    required this.plateNumber,
    required this.barcodeOrVin,
    this.nationalId = '',
    this.fullName = '',
    this.phoneNumber = '',
    this.vinCode = '',
    this.barcode = '',
    this.engineNumber = '',
    this.chassisNumber = '',
    this.postalCode = '',
    this.address = '',
    required this.amount,
    required this.workflowMethod,
    required this.status,
    this.transactionRef = '',
    this.dateMillis = 0,
  });

  InquiryRecord copyWith({String? status, String? transactionRef}) => InquiryRecord(
        id: id,
        inquiryType: inquiryType,
        title: title,
        plateNumber: plateNumber,
        barcodeOrVin: barcodeOrVin,
        nationalId: nationalId,
        fullName: fullName,
        phoneNumber: phoneNumber,
        vinCode: vinCode,
        barcode: barcode,
        engineNumber: engineNumber,
        chassisNumber: chassisNumber,
        postalCode: postalCode,
        address: address,
        amount: amount,
        workflowMethod: workflowMethod,
        status: status ?? this.status,
        transactionRef: transactionRef ?? this.transactionRef,
        dateMillis: dateMillis,
      );

  Map<String, Object?> toMap() => {
        'id': id == 0 ? null : id,
        'inquiryType': inquiryType,
        'title': title,
        'plateNumber': plateNumber,
        'barcodeOrVin': barcodeOrVin,
        'nationalId': nationalId,
        'fullName': fullName,
        'phoneNumber': phoneNumber,
        'vinCode': vinCode,
        'barcode': barcode,
        'engineNumber': engineNumber,
        'chassisNumber': chassisNumber,
        'postalCode': postalCode,
        'address': address,
        'amount': amount,
        'workflowMethod': workflowMethod,
        'status': status,
        'transactionRef': transactionRef,
        'dateMillis': dateMillis == 0 ? DateTime.now().millisecondsSinceEpoch : dateMillis,
      };

  factory InquiryRecord.fromMap(Map<String, Object?> map) => InquiryRecord(
        id: (map['id'] as int?) ?? 0,
        inquiryType: (map['inquiryType'] as String?) ?? '',
        title: (map['title'] as String?) ?? '',
        plateNumber: (map['plateNumber'] as String?) ?? '',
        barcodeOrVin: (map['barcodeOrVin'] as String?) ?? '',
        nationalId: (map['nationalId'] as String?) ?? '',
        fullName: (map['fullName'] as String?) ?? '',
        phoneNumber: (map['phoneNumber'] as String?) ?? '',
        vinCode: (map['vinCode'] as String?) ?? '',
        barcode: (map['barcode'] as String?) ?? '',
        engineNumber: (map['engineNumber'] as String?) ?? '',
        chassisNumber: (map['chassisNumber'] as String?) ?? '',
        postalCode: (map['postalCode'] as String?) ?? '',
        address: (map['address'] as String?) ?? '',
        amount: (map['amount'] as int?) ?? 0,
        workflowMethod: (map['workflowMethod'] as String?) ?? '',
        status: (map['status'] as String?) ?? '',
        transactionRef: (map['transactionRef'] as String?) ?? '',
        dateMillis: (map['dateMillis'] as int?) ?? 0,
      );
}
