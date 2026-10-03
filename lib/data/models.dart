class Vehicle {
  final int? id;
  String title;
  String plateFirst2;
  String plateLetter;
  String plateLast3;
  String plateCityCode;
  String fuelType;
  double tankCapacity;
  int currentOdometer;
  int insuranceExpiryMillis;
  String insuranceCompany;
  String insuranceType;
  int inspectionExpiryMillis;
  String inspectionCenter;
  int inspectionNotifyDaysBefore;
  int createdAt;

  Vehicle({
    this.id,
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
    int? createdAt,
  }) : createdAt = createdAt ?? DateTime.now().millisecondsSinceEpoch;

  String get formattedPlate =>
      'ایران $plateCityCode | $plateLast3 $plateLetter $plateFirst2';

  int get effectiveInsuranceExpiry =>
      insuranceExpiryMillis > 0
          ? insuranceExpiryMillis
          : createdAt + 365 * 24 * 3600 * 1000;

  int get effectiveInspectionExpiry =>
      inspectionExpiryMillis > 0
          ? inspectionExpiryMillis
          : createdAt + 365 * 24 * 3600 * 1000;

  int get effectiveInspectionExpiryMillis => effectiveInspectionExpiry;

  Map<String, Object?> toMap() => {
        'id': id,
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
        'createdAt': createdAt,
      };

  factory Vehicle.fromMap(Map<String, Object?> m) => Vehicle(
        id: m['id'] as int?,
        title: m['title'] as String,
        plateFirst2: m['plateFirst2'] as String,
        plateLetter: m['plateLetter'] as String,
        plateLast3: m['plateLast3'] as String,
        plateCityCode: m['plateCityCode'] as String,
        fuelType: m['fuelType'] as String,
        tankCapacity: (m['tankCapacity'] as num).toDouble(),
        currentOdometer: (m['currentOdometer'] as num).toInt(),
        insuranceExpiryMillis: (m['insuranceExpiryMillis'] as num?)?.toInt() ?? 0,
        insuranceCompany: m['insuranceCompany'] as String? ?? 'بیمه ایران',
        insuranceType: m['insuranceType'] as String? ?? 'بیمه شخص ثالث',
        inspectionExpiryMillis: (m['inspectionExpiryMillis'] as num?)?.toInt() ?? 0,
        inspectionCenter: m['inspectionCenter'] as String? ?? '',
        inspectionNotifyDaysBefore:
            (m['inspectionNotifyDaysBefore'] as num?)?.toInt() ?? 15,
        createdAt: (m['createdAt'] as num?)?.toInt(),
      );

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
  }) => Vehicle(
        id: id ?? id,
        title: title ?? this.title,
        plateFirst2: plateFirst2 ?? this.plateFirst2,
        plateLetter: plateLetter ?? this.plateLetter,
        plateLast3: plateLast3 ?? this.plateLast3,
        plateCityCode: plateCityCode ?? this.plateCityCode,
        fuelType: fuelType ?? this.fuelType,
        tankCapacity: tankCapacity ?? this.tankCapacity,
        currentOdometer: currentOdometer ?? this.currentOdometer,
        insuranceExpiryMillis:
            insuranceExpiryMillis ?? this.insuranceExpiryMillis,
        insuranceCompany: insuranceCompany ?? this.insuranceCompany,
        insuranceType: insuranceType ?? this.insuranceType,
        inspectionExpiryMillis:
            inspectionExpiryMillis ?? this.inspectionExpiryMillis,
        inspectionCenter: inspectionCenter ?? this.inspectionCenter,
        inspectionNotifyDaysBefore:
            inspectionNotifyDaysBefore ?? this.inspectionNotifyDaysBefore,
        createdAt: createdAt,
      );
}

class FuelLog {
  final int? id;
  final int vehicleId;
  final int dateMillis;
  final int odometer;
  final double liters;
  final int pricePerLiter;
  final int totalCost;
  final String stationName;
  final bool isFullTank;
  final String notes;

  FuelLog({
    this.id,
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
        'id': id,
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

  factory FuelLog.fromMap(Map<String, Object?> m) => FuelLog(
        id: m['id'] as int?,
        vehicleId: (m['vehicleId'] as num).toInt(),
        dateMillis: (m['dateMillis'] as num).toInt(),
        odometer: (m['odometer'] as num).toInt(),
        liters: (m['liters'] as num).toDouble(),
        pricePerLiter: (m['pricePerLiter'] as num).toInt(),
        totalCost: (m['totalCost'] as num).toInt(),
        stationName: m['stationName'] as String? ?? '',
        isFullTank: (m['isFullTank'] as num?)?.toInt() == 1,
        notes: m['notes'] as String? ?? '',
      );
}

class ServiceReminder {
  final int? id;
  final int vehicleId;
  final String serviceType;
  final int targetDateMillis;
  final int targetOdometer;
  final String notes;
  final bool isCompleted;
  final bool notified;

  ServiceReminder({
    this.id,
    required this.vehicleId,
    required this.serviceType,
    required this.targetDateMillis,
    required this.targetOdometer,
    this.notes = '',
    this.isCompleted = false,
    this.notified = false,
  });

  Map<String, Object?> toMap() => {
        'id': id,
        'vehicleId': vehicleId,
        'serviceType': serviceType,
        'targetDateMillis': targetDateMillis,
        'targetOdometer': targetOdometer,
        'notes': notes,
        'isCompleted': isCompleted ? 1 : 0,
        'notified': notified ? 1 : 0,
      };

  factory ServiceReminder.fromMap(Map<String, Object?> m) => ServiceReminder(
        id: m['id'] as int?,
        vehicleId: (m['vehicleId'] as num).toInt(),
        serviceType: m['serviceType'] as String,
        targetDateMillis: (m['targetDateMillis'] as num).toInt(),
        targetOdometer: (m['targetOdometer'] as num).toInt(),
        notes: m['notes'] as String? ?? '',
        isCompleted: (m['isCompleted'] as num?)?.toInt() == 1,
        notified: (m['notified'] as num?)?.toInt() == 1,
      );

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
}

class ServiceHistory {
  final int? id;
  final int vehicleId;
  final String serviceType;
  final String itemsChanged;
  final int odometer;
  final int nextDueOdometer;
  final int dateMillis;
  final int cost;
  final String mechanicOrShop;
  final String notes;

  ServiceHistory({
    this.id,
    required this.vehicleId,
    required this.serviceType,
    required this.itemsChanged,
    required this.odometer,
    required this.nextDueOdometer,
    required this.dateMillis,
    required this.cost,
    this.mechanicOrShop = '',
    this.notes = '',
  });

  Map<String, Object?> toMap() => {
        'id': id,
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

  factory ServiceHistory.fromMap(Map<String, Object?> m) => ServiceHistory(
        id: m['id'] as int?,
        vehicleId: (m['vehicleId'] as num).toInt(),
        serviceType: m['serviceType'] as String,
        itemsChanged: m['itemsChanged'] as String? ?? '',
        odometer: (m['odometer'] as num).toInt(),
        nextDueOdometer: (m['nextDueOdometer'] as num?)?.toInt() ?? 0,
        dateMillis: (m['dateMillis'] as num).toInt(),
        cost: (m['cost'] as num).toInt(),
        mechanicOrShop: m['mechanicOrShop'] as String? ?? '',
        notes: m['notes'] as String? ?? '',
      );
}

class ServiceRequest {
  static const String statusPending = 'در انتظار تایید مدیر';
  static const String statusApproved = 'تایید شد و برای شما اطلاعات ارسال میگردد';

  final int? id;
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
    this.id,
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
    this.status = 'در انتظار تایید مدیر',
    this.baleMessageId = '',
    int? submissionDateMillis,
    int? updatedDateMillis,
  })  : submissionDateMillis = submissionDateMillis ?? 0,
        updatedDateMillis = updatedDateMillis ?? 0;

  Map<String, Object?> toMap() => {
        'id': id,
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
        'submissionDateMillis': submissionDateMillis,
        'updatedDateMillis': updatedDateMillis,
      };

  factory ServiceRequest.fromMap(Map<String, Object?> m) => ServiceRequest(
        id: m['id'] as int?,
        requestType: m['requestType'] as String,
        title: m['title'] as String,
        fullName: m['fullName'] as String,
        nationalCode: m['nationalCode'] as String,
        phoneNumber: m['phoneNumber'] as String,
        vehiclePlate: m['vehiclePlate'] as String,
        vinCode: m['vinCode'] as String? ?? '',
        barcodeNumber: m['barcodeNumber'] as String? ?? '',
        engineNumber: m['engineNumber'] as String? ?? '',
        chassisNumber: m['chassisNumber'] as String? ?? '',
        postalCode: m['postalCode'] as String? ?? '',
        address: m['address'] as String? ?? '',
        insuranceCategory: m['insuranceCategory'] as String? ?? '',
        insuranceCompany: m['insuranceCompany'] as String? ?? '',
        durationMonths: (m['durationMonths'] as num?)?.toInt() ?? 12,
        discountPercent: (m['discountPercent'] as num?)?.toInt() ?? 0,
        additionalDetails: m['additionalDetails'] as String? ?? '',
        status: m['status'] as String? ?? 'در انتظار تایید مدیر',
        baleMessageId: m['baleMessageId'] as String? ?? '',
        submissionDateMillis:
            (m['submissionDateMillis'] as num?)?.toInt() ?? 0,
        updatedDateMillis: (m['updatedDateMillis'] as num?)?.toInt() ?? 0,
      );

  ServiceRequest copyWith({
    int? id,
    String? status,
    String? baleMessageId,
    int? submissionDateMillis,
    int? updatedDateMillis,
  }) => ServiceRequest(
        id: id ?? id,
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
        submissionDateMillis: submissionDateMillis ?? this.submissionDateMillis,
        updatedDateMillis: updatedDateMillis ?? DateTime.now().millisecondsSinceEpoch,
      );
}

class InquiryRecord {
  final int? id;
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
    this.id,
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
    int? dateMillis,
  }) : dateMillis = dateMillis ?? 0;

  Map<String, Object?> toMap() => {
        'id': id,
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
        'dateMillis': dateMillis,
      };

  factory InquiryRecord.fromMap(Map<String, Object?> m) => InquiryRecord(
        id: m['id'] as int?,
        inquiryType: m['inquiryType'] as String,
        title: m['title'] as String,
        plateNumber: m['plateNumber'] as String,
        barcodeOrVin: m['barcodeOrVin'] as String,
        nationalId: m['nationalId'] as String? ?? '',
        fullName: m['fullName'] as String? ?? '',
        phoneNumber: m['phoneNumber'] as String? ?? '',
        vinCode: m['vinCode'] as String? ?? '',
        barcode: m['barcode'] as String? ?? '',
        engineNumber: m['engineNumber'] as String? ?? '',
        chassisNumber: m['chassisNumber'] as String? ?? '',
        postalCode: m['postalCode'] as String? ?? '',
        address: m['address'] as String? ?? '',
        amount: (m['amount'] as num).toInt(),
        workflowMethod: m['workflowMethod'] as String,
        status: m['status'] as String,
        transactionRef: m['transactionRef'] as String? ?? '',
        dateMillis: (m['dateMillis'] as num?)?.toInt() ?? 0,
      );

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
}

class QueueItem {
  final int? id;
  final String kind;
  final String payloadJson;
  final int createdAt;
  final int attempts;

  const QueueItem({
    this.id,
    required this.kind,
    required this.payloadJson,
    required this.createdAt,
    this.attempts = 0,
  });

  Map<String, Object?> toMap() => {
        'id': id,
        'kind': kind,
        'payloadJson': payloadJson,
        'createdAt': createdAt,
        'attempts': attempts,
      };

  factory QueueItem.fromMap(Map<String, Object?> m) => QueueItem(
        id: m['id'] as int?,
        kind: m['kind'] as String,
        payloadJson: m['payloadJson'] as String,
        createdAt: (m['createdAt'] as num).toInt(),
        attempts: (m['attempts'] as num?)?.toInt() ?? 0,
      );
}
