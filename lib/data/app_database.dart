import 'package:path/path.dart' as p;
import 'package:sqflite/sqflite.dart';

/// SQLite schema mirroring the original Room database (`fuel_and_inquiry_db`, v5).
class AppDatabase {
  AppDatabase._();

  static const _dbName = 'fuel_and_inquiry_db';
  static const _dbVersion = 5;

  static Database? _instance;

  static Future<Database> instance() async {
    if (_instance != null) return _instance!;
    final dir = await getDatabasesPath();
    _instance = await openDatabase(
      p.join(dir, _dbName),
      version: _dbVersion,
      onConfigure: (db) async {
        await db.execute('PRAGMA foreign_keys = ON');
      },
      onCreate: (db, version) async {
        await _createSchema(db);
      },
      onUpgrade: (db, oldVersion, newVersion) async {
        // Mirrors `fallbackToDestructiveMigration()`.
        for (final table in const [
          'fuel_logs',
          'service_reminders',
          'service_history',
          'service_requests',
          'inquiry_records',
          'vehicles',
        ]) {
          await db.execute('DROP TABLE IF EXISTS $table');
        }
        await _createSchema(db);
      },
      onOpen: (db) async {
        await db.execute('''
          CREATE TABLE IF NOT EXISTS pending_bale_outbox (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            kind TEXT NOT NULL,
            payloadJson TEXT NOT NULL,
            photoBase64 TEXT NOT NULL DEFAULT '',
            createdAt INTEGER NOT NULL
          )
        ''');
      },
    );
    return _instance!;
  }

  static Future<void> _createSchema(Database db) async {
    await db.execute('''
      CREATE TABLE IF NOT EXISTS pending_bale_outbox (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        kind TEXT NOT NULL,
        payloadJson TEXT NOT NULL,
        photoBase64 TEXT NOT NULL DEFAULT '',
        createdAt INTEGER NOT NULL
      )
    ''');
    await db.execute('''
      CREATE TABLE vehicles (
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
      )
    ''');

    await db.execute('''
      CREATE TABLE fuel_logs (
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
        FOREIGN KEY (vehicleId) REFERENCES vehicles (id) ON DELETE CASCADE
      )
    ''');
    await db.execute('CREATE INDEX idx_fuel_logs_vehicleId ON fuel_logs (vehicleId)');

    await db.execute('''
      CREATE TABLE service_reminders (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        vehicleId INTEGER NOT NULL,
        serviceType TEXT NOT NULL,
        targetDateMillis INTEGER NOT NULL,
        targetOdometer INTEGER NOT NULL,
        notes TEXT NOT NULL DEFAULT '',
        isCompleted INTEGER NOT NULL DEFAULT 0,
        notified INTEGER NOT NULL DEFAULT 0,
        FOREIGN KEY (vehicleId) REFERENCES vehicles (id) ON DELETE CASCADE
      )
    ''');
    await db.execute('CREATE INDEX idx_reminders_vehicleId ON service_reminders (vehicleId)');

    await db.execute('''
      CREATE TABLE service_history (
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
        FOREIGN KEY (vehicleId) REFERENCES vehicles (id) ON DELETE CASCADE
      )
    ''');
    await db.execute('CREATE INDEX idx_history_vehicleId ON service_history (vehicleId)');

    await db.execute('''
      CREATE TABLE service_requests (
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
      )
    ''');

    await db.execute('''
      CREATE TABLE inquiry_records (
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
      )
    ''');
  }
}
