import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';

import 'models.dart';

class LocalDatabase {
  LocalDatabase._();

  static final LocalDatabase instance = LocalDatabase._();
  Database? _db;

  Future<Database> get db async {
    if (_db != null) return _db!;
    final path = join(await getDatabasesPath(), 'sookht_man_v2.db');
    _db = await openDatabase(
      path,
      version: 1,
      onConfigure: (db) async {
        await db.execute('PRAGMA foreign_keys = ON');
      },
      onCreate: (db, version) async {
      await db.execute('''CREATE TABLE vehicles (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        title TEXT NOT NULL,
        plateFirst2 TEXT NOT NULL,
        plateLetter TEXT NOT NULL,
        plateLast3 TEXT NOT NULL,
        plateCityCode TEXT NOT NULL,
        fuelType TEXT NOT NULL,
        tankCapacity REAL NOT NULL,
        currentOdometer INTEGER NOT NULL,
        insuranceExpiryMillis INTEGER NOT NULL DEFAULT 0,
        insuranceCompany TEXT NOT NULL DEFAULT 'بیمه ایران',
        insuranceType TEXT NOT NULL DEFAULT 'بیمه شخص ثالث',
        inspectionExpiryMillis INTEGER NOT NULL DEFAULT 0,
        inspectionCenter TEXT NOT NULL DEFAULT '',
        inspectionNotifyDaysBefore INTEGER NOT NULL DEFAULT 15,
        createdAt INTEGER NOT NULL
      )''');
      await db.execute('''CREATE TABLE fuel_logs (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        vehicleId INTEGER NOT NULL,
        dateMillis INTEGER NOT NULL,
        odometer INTEGER NOT NULL,
        liters REAL NOT NULL,
        pricePerLiter INTEGER NOT NULL,
        totalCost INTEGER NOT NULL,
        stationName TEXT NOT NULL,
        isFullTank INTEGER NOT NULL DEFAULT 1,
        notes TEXT NOT NULL DEFAULT '',
        FOREIGN KEY(vehicleId) REFERENCES vehicles(id) ON DELETE CASCADE
      )''');
      await db.execute('''CREATE TABLE service_reminders (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        vehicleId INTEGER NOT NULL,
        serviceType TEXT NOT NULL,
        targetDateMillis INTEGER NOT NULL,
        targetOdometer INTEGER NOT NULL,
        notes TEXT NOT NULL DEFAULT '',
        isCompleted INTEGER NOT NULL DEFAULT 0,
        notified INTEGER NOT NULL DEFAULT 0,
        FOREIGN KEY(vehicleId) REFERENCES vehicles(id) ON DELETE CASCADE
      )''');
      await db.execute('''CREATE TABLE service_history (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        vehicleId INTEGER NOT NULL,
        serviceType TEXT NOT NULL,
        itemsChanged TEXT NOT NULL,
        odometer INTEGER NOT NULL,
        nextDueOdometer INTEGER NOT NULL DEFAULT 0,
        dateMillis INTEGER NOT NULL,
        cost INTEGER NOT NULL,
        mechanicOrShop TEXT NOT NULL DEFAULT '',
        notes TEXT NOT NULL DEFAULT '',
        FOREIGN KEY(vehicleId) REFERENCES vehicles(id) ON DELETE CASCADE
      )''');
      await db.execute('''CREATE TABLE service_requests (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        requestType TEXT NOT NULL,
        title TEXT NOT NULL,
        fullName TEXT NOT NULL,
        nationalCode TEXT NOT NULL,
        phoneNumber TEXT NOT NULL,
        vehiclePlate TEXT NOT NULL,
        vinCode TEXT NOT NULL DEFAULT '',
        barcodeNumber TEXT NOT NULL DEFAULT '',
        engineNumber TEXT NOT NULL DEFAULT '',
        chassisNumber TEXT NOT NULL DEFAULT '',
        postalCode TEXT NOT NULL DEFAULT '',
        address TEXT NOT NULL DEFAULT '',
        insuranceCategory TEXT NOT NULL DEFAULT '',
        insuranceCompany TEXT NOT NULL DEFAULT '',
        durationMonths INTEGER NOT NULL DEFAULT 12,
        discountPercent INTEGER NOT NULL DEFAULT 0,
        additionalDetails TEXT NOT NULL DEFAULT '',
        status TEXT NOT NULL,
        baleMessageId TEXT NOT NULL DEFAULT '',
        submissionDateMillis INTEGER NOT NULL,
        updatedDateMillis INTEGER NOT NULL
      )''');
      await db.execute('''CREATE TABLE inquiry_records (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        inquiryType TEXT NOT NULL,
        title TEXT NOT NULL,
        plateNumber TEXT NOT NULL,
        barcodeOrVin TEXT NOT NULL,
        nationalId TEXT NOT NULL DEFAULT '',
        fullName TEXT NOT NULL DEFAULT '',
        phoneNumber TEXT NOT NULL DEFAULT '',
        vinCode TEXT NOT NULL DEFAULT '',
        barcode TEXT NOT NULL DEFAULT '',
        engineNumber TEXT NOT NULL DEFAULT '',
        chassisNumber TEXT NOT NULL DEFAULT '',
        postalCode TEXT NOT NULL DEFAULT '',
        address TEXT NOT NULL DEFAULT '',
        amount INTEGER NOT NULL,
        workflowMethod TEXT NOT NULL,
        status TEXT NOT NULL,
        transactionRef TEXT NOT NULL DEFAULT '',
        dateMillis INTEGER NOT NULL
      )''');
      await db.execute('''CREATE TABLE sync_queue (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        kind TEXT NOT NULL,
        payloadJson TEXT NOT NULL,
        createdAt INTEGER NOT NULL,
        attempts INTEGER NOT NULL DEFAULT 0
      )''');
      await _migrateLegacyRoomDatabase(db, path);
    });
    return _db!;
  }


  Future<void> _migrateLegacyRoomDatabase(Database target, String newPath) async {
    final legacyPath = join(await getDatabasesPath(), 'fuel_and_inquiry_db');
    if (legacyPath == newPath) return;

    Database? legacy;
    try {
      legacy = await openDatabase(legacyPath, readOnly: true);
      const tables = <String>[
        'vehicles',
        'fuel_logs',
        'service_reminders',
        'service_history',
        'service_requests',
        'inquiry_records',
      ];
      for (final table in tables) {
        final rows = await legacy.query(table);
        for (final row in rows) {
          await target.insert(table, Map<String, Object?>.from(row), conflictAlgorithm: ConflictAlgorithm.replace);
        }
      }
    } catch (_) {
      // A fresh install has no legacy Room database. Migration errors must
      // never prevent the Flutter app from opening its new local database.
    } finally {
      await legacy?.close();
    }
  }

  Future<List<Vehicle>> vehicles() async => (await db.query('vehicles', orderBy: 'id DESC'))
      .map((e) => Vehicle.fromMap(e))
      .toList();
  Future<int> insertVehicle(Vehicle item) async => (await db).insert('vehicles', item.toMap()..remove('id'));
  Future<void> updateVehicle(Vehicle item) async => (await db).update('vehicles', item.toMap()..remove('id'), where: 'id=?', whereArgs: [item.id]);
  Future<void> deleteVehicle(int id) async => (await db).delete('vehicles', where: 'id=?', whereArgs: [id]);

  Future<List<FuelLog>> fuelLogs() async => (await db.query('fuel_logs', orderBy: 'dateMillis DESC'))
      .map((e) => FuelLog.fromMap(e))
      .toList();
  Future<int> insertFuelLog(FuelLog item) async => (await db).insert('fuel_logs', item.toMap()..remove('id'));
  Future<void> deleteFuelLog(int id) async => (await db).delete('fuel_logs', where: 'id=?', whereArgs: [id]);

  Future<List<ServiceReminder>> reminders() async => (await db.query('service_reminders', orderBy: 'targetDateMillis ASC'))
      .map((e) => ServiceReminder.fromMap(e))
      .toList();
  Future<int> insertReminder(ServiceReminder item) async => (await db).insert('service_reminders', item.toMap()..remove('id'));
  Future<void> updateReminder(ServiceReminder item) async => (await db).update('service_reminders', item.toMap()..remove('id'), where: 'id=?', whereArgs: [item.id]);
  Future<void> deleteReminder(int id) async => (await db).delete('service_reminders', where: 'id=?', whereArgs: [id]);

  Future<List<ServiceHistory>> serviceHistory() async => (await db.query('service_history', orderBy: 'dateMillis DESC'))
      .map((e) => ServiceHistory.fromMap(e))
      .toList();
  Future<int> insertServiceHistory(ServiceHistory item) async => (await db).insert('service_history', item.toMap()..remove('id'));
  Future<void> deleteServiceHistory(int id) async => (await db).delete('service_history', where: 'id=?', whereArgs: [id]);

  Future<List<ServiceRequest>> requests() async => (await db.query('service_requests', orderBy: 'submissionDateMillis DESC'))
      .map((e) => ServiceRequest.fromMap(e))
      .toList();
  Future<int> insertRequest(ServiceRequest item) async => (await db).insert('service_requests', item.toMap()..remove('id'));
  Future<void> updateRequest(ServiceRequest item) async => (await db).update('service_requests', item.toMap()..remove('id'), where: 'id=?', whereArgs: [item.id]);
  Future<void> deleteRequest(int id) async => (await db).delete('service_requests', where: 'id=?', whereArgs: [id]);

  Future<List<InquiryRecord>> inquiries() async => (await db.query('inquiry_records', orderBy: 'dateMillis DESC'))
      .map((e) => InquiryRecord.fromMap(e))
      .toList();
  Future<int> insertInquiry(InquiryRecord item) async => (await db).insert('inquiry_records', item.toMap()..remove('id'));
  Future<void> updateInquiry(InquiryRecord item) async => (await db).update('inquiry_records', item.toMap()..remove('id'), where: 'id=?', whereArgs: [item.id]);
  Future<void> deleteInquiry(int id) async => (await db).delete('inquiry_records', where: 'id=?', whereArgs: [id]);

  Future<int> enqueue(QueueItem item) async => (await db).insert('sync_queue', item.toMap()..remove('id'));
  Future<List<QueueItem>> queue() async => (await db.query('sync_queue', orderBy: 'createdAt ASC'))
      .map((e) => QueueItem.fromMap(e))
      .toList();
  Future<void> deleteQueueItem(int id) async => (await db).delete('sync_queue', where: 'id=?', whereArgs: [id]);
  Future<void> bumpQueueItem(int id, int attempts) async => (await db).update('sync_queue', {'attempts': attempts}, where: 'id=?', whereArgs: [id]);

  Future<Map<String, dynamic>> snapshot() async {
    final v = await vehicles();
    final f = await fuelLogs();
    final r = await reminders();
    final h = await serviceHistory();
    final s = await requests();
    final i = await inquiries();
    return {
      'app': 'com.angelgirlbrand.sokhtandmodiriat',
      'version': '2.0.0',
      'timestamp': DateTime.now().millisecondsSinceEpoch,
      'vehicles': v.map((e) => e.toMap()).toList(),
      'fuelLogs': f.map((e) => e.toMap()).toList(),
      'serviceReminders': r.map((e) => e.toMap()).toList(),
      'serviceHistory': h.map((e) => e.toMap()).toList(),
      'serviceRequests': s.map((e) => e.toMap()).toList(),
      'inquiries': i.map((e) => e.toMap()).toList(),
    };
  }
}
