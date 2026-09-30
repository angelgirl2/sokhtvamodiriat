import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';

import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;

import '../core/persian_utils.dart';
import '../data/models.dart';

/// Report generation: comprehensive PDF dossier, Excel-compatible CSV and a
/// shareable plain-text report. Ported from `ReportExporter.kt`.
class ReportExporter {
  ReportExporter._();

  // ---------------------------------------------------------------------------
  // Text dossier
  // ---------------------------------------------------------------------------
  static String generateFullDossierTextReport({
    Vehicle? vehicle,
    required List<FuelLog> logs,
    required List<ServiceHistory> serviceHistory,
    required List<ServiceReminder> reminders,
  }) {
    final sb = StringBuffer()
      ..writeln('📋 گزارش پرونده هوشمند خودرو و مصرف سوخت')
      ..writeln('═══════════════════════════')
      ..writeln('📅 تاریخ ایجاد: ${PersianUtils.dateTime(DateTime.now())}');

    if (vehicle != null) {
      final expiryDays = PersianUtils.daysUntil(vehicle.effectiveInspectionExpiryMillis);
      final inspStatus = expiryDays < 0
          ? 'منقضی شده 🔴'
          : expiryDays <= 15
              ? 'نزدیک به انقضا (${PersianUtils.daysLabel(expiryDays)} روز مانده) 🟡'
              : 'معتبر (${PersianUtils.daysLabel(expiryDays)} روز مانده) 🟢';

      sb
        ..writeln()
        ..writeln('🚗 مشخصات خودرو:')
        ..writeln('• عنوان: ${vehicle.title}')
        ..writeln('• پلاک: ${vehicle.formattedPlate}')
        ..writeln('• نوع سوخت: ${vehicle.fuelType}')
        ..writeln('• کارکرد فعلی: ${PersianUtils.number(vehicle.currentOdometer)} کیلومتر')
        ..writeln('• ظرفیت باک: ${PersianUtils.liters(vehicle.tankCapacity)} لیتر')
        ..writeln('• وضعیت معاینه فنی: $inspStatus')
        ..writeln(
            '• مرکز معاینه: ${vehicle.inspectionCenter.isNotEmpty ? vehicle.inspectionCenter : "مرکز بیهقی"}');
    }

    final totalLiters = logs.fold<double>(0, (sum, l) => sum + l.liters);
    final totalFuelCost = logs.fold<int>(0, (sum, l) => sum + l.totalCost);
    final totalServiceCost = serviceHistory.fold<int>(0, (sum, s) => sum + s.cost);

    sb
      ..writeln()
      ..writeln('📊 خلاصه آمار و هزینه‌ها:')
      ..writeln('• کل بنزین مصرفی: ${PersianUtils.liters(totalLiters)} لیتر')
      ..writeln('• کل هزینه سوخت‌گیری: ${PersianUtils.money(totalFuelCost)} تومان')
      ..writeln('• کل هزینه سرویس و نگهداری: ${PersianUtils.money(totalServiceCost)} تومان')
      ..writeln('• مجموع کل مخارج خودرو: ${PersianUtils.money(totalFuelCost + totalServiceCost)} تومان');

    if (serviceHistory.isNotEmpty) {
      sb
        ..writeln()
        ..writeln('🔧 سوابق سرویس‌های دوره‌ای اخیر (${PersianUtils.number(serviceHistory.length)} مورد):');
      for (var i = 0; i < serviceHistory.take(5).length; i++) {
        final s = serviceHistory[i];
        sb
          ..writeln('${PersianUtils.number(i + 1)}. [${PersianUtils.date(DateTime.fromMillisecondsSinceEpoch(s.dateMillis))}] ${s.serviceType}')
          ..writeln(
              '   - کیلومتر: ${PersianUtils.number(s.odometer)} km | بعدی: ${PersianUtils.number(s.nextDueOdometer)} km');
        if (s.itemsChanged.isNotEmpty) sb.writeln('   - اقلام: ${s.itemsChanged}');
        sb.writeln(
            '   - هزینه: ${PersianUtils.money(s.cost)} تومان | تعمیرگاه: ${s.mechanicOrShop.isNotEmpty ? s.mechanicOrShop : "ثبت نشده"}');
      }
    }

    if (logs.isNotEmpty) {
      sb
        ..writeln()
        ..writeln('⛽ تاریخچه سوخت‌گیری‌های اخیر (${PersianUtils.number(logs.length)} نوبت):');
      for (var i = 0; i < logs.take(5).length; i++) {
        final l = logs[i];
        sb
          ..writeln(
              '${PersianUtils.number(i + 1)}. [${PersianUtils.date(DateTime.fromMillisecondsSinceEpoch(l.dateMillis))}] ${PersianUtils.liters(l.liters)} لیتر (${PersianUtils.money(l.totalCost)} تومان)')
          ..writeln(
              '   - کارکرد: ${PersianUtils.number(l.odometer)} km | جایگاه: ${l.stationName.isNotEmpty ? l.stationName : "نامشخص"}');
      }
    }

    if (reminders.isNotEmpty) {
      final active = reminders.where((r) => !r.isCompleted).toList();
      if (active.isNotEmpty) {
        sb
          ..writeln()
          ..writeln('🔔 یادآورهای فعال (${PersianUtils.number(active.length)} مورد):');
        for (final r in active) {
          sb.writeln(
              '• ${r.serviceType} — کیلومتر ${PersianUtils.number(r.targetOdometer)} | ${PersianUtils.date(DateTime.fromMillisecondsSinceEpoch(r.targetDateMillis))}');
        }
      }
    }

    sb
      ..writeln()
      ..writeln('═══════════════════════════')
      ..write('📲 صادر شده از اپلیکیشن مدیریت سوخت و استعلام خودرو');
    return sb.toString();
  }

  // ---------------------------------------------------------------------------
  // CSV (Excel compatible, UTF-8 BOM)
  // ---------------------------------------------------------------------------
  static Uint8List buildFuelCsvBytes({
    required List<Vehicle> vehicles,
    required List<FuelLog> logs,
  }) {
    final vehicleMap = {for (final v in vehicles) v.id: v};
    final sb = StringBuffer()
      ..writeln(
          'ردیف,نام خودرو,پلاک,تاریخ,کیلومتر کارکرد,حجم سوخت (لیتر),قیمت هر لیتر (تومان),مبلغ پرداختی (تومان),نام جایگاه,باک کامل,توضیحات');

    for (var i = 0; i < logs.length; i++) {
      final log = logs[i];
      final v = vehicleMap[log.vehicleId];
      final line = <String>[
        '${i + 1}',
        v?.title ?? 'نامشخص',
        '"${v?.formattedPlate ?? ""}"',
        PersianUtils.dateTime(DateTime.fromMillisecondsSinceEpoch(log.dateMillis)),
        '${log.odometer}',
        '${log.liters}',
        '${log.pricePerLiter}',
        '${log.totalCost}',
        '"${log.stationName}"',
        log.isFullTank ? 'کامل' : 'خیر',
        '"${log.notes.replaceAll('"', '""')}"',
      ].join(',');
      sb.writeln(line);
    }

    // UTF-8 BOM so Excel renders Persian text correctly.
    return Uint8List.fromList([0xEF, 0xBB, 0xBF, ...utf8.encode(sb.toString())]);
  }

  // ---------------------------------------------------------------------------
  // PDF dossier (A4 portrait, RTL)
  // ---------------------------------------------------------------------------
  static Future<Uint8List> buildPdfBytes({
    required List<Vehicle> vehicles,
    required List<FuelLog> logs,
    required List<ServiceHistory> serviceHistory,
    required List<ServiceReminder> reminders,
    pw.Font? font,
  }) async {
    final doc = pw.Document(
      theme: font != null ? pw.ThemeData.withFont(base: font, bold: font) : null,
    );

    final totalLiters = logs.fold<double>(0, (s, l) => s + l.liters);
    final totalFuelCost = logs.fold<int>(0, (s, l) => s + l.totalCost);
    final totalServiceCost = serviceHistory.fold<int>(0, (s, l) => s + l.cost);
    final vehicleMap = {for (final v in vehicles) v.id: v};

    pw.Widget header(String text, PdfColor color) => pw.Container(
          width: double.infinity,
          padding: const pw.EdgeInsets.symmetric(vertical: 5, horizontal: 6),
          decoration: pw.BoxDecoration(
            color: color,
            borderRadius: pw.BorderRadius.circular(4),
          ),
          child: pw.Text(text,
              style: pw.TextStyle(fontSize: 9, fontWeight: pw.FontWeight.bold)),
        );

    doc.addPage(
      pw.MultiPage(
        pageFormat: PdfPageFormat.a4,
        textDirection: pw.TextDirection.rtl,
        margin: const pw.EdgeInsets.all(24),
        build: (context) => [
          pw.Container(
            width: double.infinity,
            padding: const pw.EdgeInsets.all(12),
            decoration: pw.BoxDecoration(
              color: PdfColor.fromHex('#F1F5F9'),
              borderRadius: pw.BorderRadius.circular(8),
            ),
            child: pw.Column(
              crossAxisAlignment: pw.CrossAxisAlignment.center,
              children: [
                pw.Text('گزارش جامع مدیریت خودرو، سوخت و دفترچه سرویس دوره‌ای',
                    style: pw.TextStyle(
                        fontSize: 14,
                        fontWeight: pw.FontWeight.bold,
                        color: PdfColor.fromHex('#0284C7'))),
                pw.SizedBox(height: 4),
                pw.Text(
                  'تاریخ ایجاد گزارش: ${PersianUtils.dateTime(DateTime.now())} | پایش هوشمند خودرو',
                  style: pw.TextStyle(fontSize: 8, color: PdfColor.fromHex('#64748B')),
                ),
              ],
            ),
          ),
          pw.SizedBox(height: 10),
          pw.Container(
            width: double.infinity,
            padding: const pw.EdgeInsets.all(10),
            decoration: pw.BoxDecoration(
              color: PdfColor.fromHex('#E0F2FE'),
              borderRadius: pw.BorderRadius.circular(8),
            ),
            child: pw.Column(
              crossAxisAlignment: pw.CrossAxisAlignment.start,
              children: [
                pw.Text(
                  'خودروها: ${PersianUtils.number(vehicles.length)} دستگاه    |    '
                  'مجموع بنزین: ${PersianUtils.liters(totalLiters)} لیتر    |    '
                  'هزینه سوخت: ${PersianUtils.money(totalFuelCost)} تومان',
                  style: pw.TextStyle(fontSize: 9, fontWeight: pw.FontWeight.bold),
                ),
                pw.SizedBox(height: 4),
                pw.Text(
                  'سوابق سوخت‌گیری: ${PersianUtils.number(logs.length)} نوبت    |    '
                  'سوابق سرویس: ${PersianUtils.number(serviceHistory.length)} مورد    |    '
                  'هزینه سرویس‌ها: ${PersianUtils.money(totalServiceCost)} تومان',
                  style: pw.TextStyle(fontSize: 9, fontWeight: pw.FontWeight.bold),
                ),
              ],
            ),
          ),
          pw.SizedBox(height: 14),
          pw.Text('۱. آخرین سوابق سرویس دوره‌ای و تعویض قطعات',
              style: pw.TextStyle(fontSize: 10.5, fontWeight: pw.FontWeight.bold)),
          pw.SizedBox(height: 6),
          if (serviceHistory.isEmpty)
            pw.Text('هیچ سابقه سرویس دوره‌ای ثبت نشده است.',
                style: const pw.TextStyle(fontSize: 8.5))
          else ...[
            header('ردیف | سرویس | اقلام تعویضی | کیلومتر | سرویس بعدی | هزینه (تومان)',
                PdfColor.fromHex('#F1F5F9')),
            pw.SizedBox(height: 4),
            ...serviceHistory.take(8).toList().asMap().entries.map((e) {
              final s = e.value;
              return pw.Padding(
                padding: const pw.EdgeInsets.only(bottom: 3),
                child: pw.Text(
                  '${PersianUtils.number(e.key + 1)} | ${s.serviceType} | '
                  '${s.itemsChanged.isEmpty ? "سرویس عمومی" : s.itemsChanged} | '
                  '${PersianUtils.number(s.odometer)} | '
                  '${s.nextDueOdometer > 0 ? PersianUtils.number(s.nextDueOdometer) : "---"} | '
                  '${PersianUtils.money(s.cost)}',
                  style: const pw.TextStyle(fontSize: 8),
                ),
              );
            }),
          ],
          pw.SizedBox(height: 14),
          pw.Text('۲. تاریخچه سوخت‌گیری‌های اخیر خودرو',
              style: pw.TextStyle(fontSize: 10.5, fontWeight: pw.FontWeight.bold)),
          pw.SizedBox(height: 6),
          if (logs.isEmpty)
            pw.Text('هیچ سابقه سوخت‌گیری ثبت نشده است.', style: const pw.TextStyle(fontSize: 8.5))
          else ...[
            header('ردیف | خودرو | تاریخ | کیلومتر | لیتر | فی (تومان) | مبلغ پرداختی',
                PdfColor.fromHex('#E0F2FE')),
            pw.SizedBox(height: 4),
            ...logs.take(16).toList().asMap().entries.map((e) {
              final l = e.value;
              final vTitle = vehicleMap[l.vehicleId]?.title ?? 'خودرو';
              return pw.Padding(
                padding: const pw.EdgeInsets.only(bottom: 3),
                child: pw.Text(
                  '${PersianUtils.number(e.key + 1)} | $vTitle | '
                  '${PersianUtils.date(DateTime.fromMillisecondsSinceEpoch(l.dateMillis))} | '
                  '${PersianUtils.number(l.odometer)} | ${PersianUtils.liters(l.liters)} | '
                  '${PersianUtils.money(l.pricePerLiter)} | ${PersianUtils.money(l.totalCost)}',
                  style: const pw.TextStyle(fontSize: 8),
                ),
              );
            }),
          ],
          if (reminders.isNotEmpty) ...[
            pw.SizedBox(height: 14),
            pw.Text('۳. یادآورهای سرویس و نگهداری',
                style: pw.TextStyle(fontSize: 10.5, fontWeight: pw.FontWeight.bold)),
            pw.SizedBox(height: 6),
            ...reminders.take(10).map((r) => pw.Padding(
                  padding: const pw.EdgeInsets.only(bottom: 3),
                  child: pw.Text(
                    '• ${r.serviceType} — کیلومتر ${PersianUtils.number(r.targetOdometer)} | '
                    '${PersianUtils.date(DateTime.fromMillisecondsSinceEpoch(r.targetDateMillis))} | '
                    '${r.isCompleted ? "انجام شد" : "در انتظار"}',
                    style: const pw.TextStyle(fontSize: 8),
                  ),
                )),
          ],
          pw.SizedBox(height: 18),
          pw.Divider(color: PdfColor.fromHex('#E2E8F0')),
          pw.Center(
            child: pw.Text(
              'صادر شده از سامانه مدیریت سوخت و خدمات خودرو | نسخه اختصاصی',
              style: pw.TextStyle(fontSize: 8, color: PdfColor.fromHex('#64748B')),
            ),
          ),
        ],
      ),
    );

    return doc.save();
  }

  static Future<File> writeTemp(String fileName, Uint8List bytes) async {
    final dir = Directory.systemTemp;
    final file = File('${dir.path}/$fileName');
    await file.writeAsBytes(bytes);
    return file;
  }
}
