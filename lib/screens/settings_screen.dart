import 'dart:io';

import 'package:excel/excel.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';
import 'package:path_provider/path_provider.dart';
import 'package:pdf/widgets.dart' as pw;
import 'package:printing/printing.dart';
import 'package:share_plus/share_plus.dart';
import 'package:url_launcher/url_launcher.dart';

import '../app_controller.dart';
import '../core/theme.dart';
import '../core/utils.dart';
import '../widgets/glass.dart';
import '../widgets/logo.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key, required this.controller});

  final AppController controller;

  @override
  Widget build(BuildContext context) {
    final security = controller.security;

    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 14, 16, 30),
      children: [
        const GradientHeader(
          title: 'تنظیمات',
          subtitle: 'امنیت، ظاهر، همگام‌سازی و ابزارهای گزارش‌گیری برنامه',
          trailing: AppLogo(size: 52),
        ),
        const SizedBox(height: 16),
        GlassCard(
          child: Row(
            children: [
              const AppLogo(size: 56),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'مدیریت سوخت و استعلام',
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.w900,
                          ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'نسخه 2.0 • Flutter',
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(
                            color: Theme.of(context).colorScheme.onSurfaceVariant,
                          ),
                    ),
                  ],
                ),
              ),
              AnimatedContainer(
                duration: AppMotion.fast,
                width: 12,
                height: 12,
                decoration: BoxDecoration(
                  color: controller.isRailwayOnline ? AppTheme.turquoise : Colors.orange,
                  shape: BoxShape.circle,
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 12),
        _section(context, 'ظاهر برنامه', [
          _tile(
            context,
            Icons.palette_outlined,
            'رنگ اصلی',
            _themeTitle(security.themeColor),
            onTap: () => _themeDialog(context),
          ),
          _tile(
            context,
            Icons.dark_mode_outlined,
            'حالت نمایش',
            _darkTitle(security.darkMode),
            onTap: () => _darkDialog(context),
          ),
        ]),
        const SizedBox(height: 12),
        _section(context, 'امنیت', [
          _tile(
            context,
            Icons.lock_outline_rounded,
            security.isPinEnabled ? 'تغییر / حذف رمز' : 'فعال‌سازی قفل برنامه',
            security.isPinEnabled ? 'PIN فعال است' : 'PIN + اثر انگشت اختیاری',
            onTap: () => _pinDialog(context),
          ),
          if (security.isPinEnabled)
            _switchTile(
              context,
              Icons.fingerprint_rounded,
              'ورود با اثر انگشت',
              security.isBiometricEnabled,
              (value) async {
                await security.setBiometricEnabled(value);
                controller.refresh();
              },
            ),
        ]),
        const SizedBox(height: 12),
        _section(context, 'ابری و Railway', [
          _tile(
            context,
            Icons.cloud_sync_rounded,
            'همگام‌سازی کامل',
            controller.isRailwayOnline ? 'اتصال فعال' : 'اتصال در دسترس نیست',
            onTap: controller.busy ? null : controller.sync,
          ),
          _tile(
            context,
            Icons.link_rounded,
            'آدرس Railway',
            'sokhtvamodiriat-production.up.railway.app',
            onTap: () => launchUrl(
              Uri.parse('https://sokhtvamodiriat-production.up.railway.app/'),
              mode: LaunchMode.externalApplication,
            ),
          ),
        ]),
        const SizedBox(height: 12),
        _section(context, 'گزارش و خروجی', [
          _tile(
            context,
            Icons.picture_as_pdf_rounded,
            'گزارش PDF',
            'سوخت، هزینه و سوابق سرویس',
            onTap: () => _pdf(context),
          ),
          _tile(
            context,
            Icons.table_chart_rounded,
            'گزارش Excel',
            'خروجی XLSX قابل باز شدن در Excel',
            onTap: () => _excel(context),
          ),
          _tile(
            context,
            Icons.copy_rounded,
            'کپی متن گزارش',
            'کپی مستقیم خلاصه گزارش در حافظه دستگاه',
            onTap: () => _copyReport(context),
          ),
          _tile(
            context,
            Icons.share_rounded,
            'اشتراک متن گزارش',
            'ارسال گزارش خلاصه از طریق Share',
            onTap: () => _share(context),
          ),
        ]),
        const SizedBox(height: 12),
        _section(context, 'حمایت و ارتباط', [
          _tile(
            context,
            Icons.favorite_border_rounded,
            'حمایت از توسعه‌دهنده',
            'ارسال فیش به Railway و سپس بله',
            onTap: () => _donation(context),
          ),
          _tile(
            context,
            Icons.credit_card_rounded,
            'شماره کارت حمایت',
            '6219-8619-2069-6209 • میلاد قنواتی',
            onTap: () async {
              await Clipboard.setData(
                const ClipboardData(text: '6219861920696209'),
              );
              if (!context.mounted) return;
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('شماره کارت کپی شد.')),
              );
            },
          ),
          _tile(
            context,
            Icons.send_rounded,
            'کانال بله',
            'angelgirlbrand',
            onTap: () => launchUrl(
              Uri.parse('https://ble.ir/angelgirlbrand'),
              mode: LaunchMode.externalApplication,
            ),
          ),
          _tile(
            context,
            Icons.chat_rounded,
            'روبیکا',
            '@angelgirlbrand',
            onTap: () => launchUrl(
              Uri.parse('https://rubika.ir/angelgirlbrand'),
              mode: LaunchMode.externalApplication,
            ),
          ),
          _tile(
            context,
            Icons.alternate_email_rounded,
            'تلگرام',
            '@angelgirlbrand',
            onTap: () => launchUrl(
              Uri.parse('https://t.me/angelgirlbrand'),
              mode: LaunchMode.externalApplication,
            ),
          ),
        ]),
        if (controller.statusMessage.isNotEmpty) ...[
          const SizedBox(height: 12),
          GlassCard(
            padding: const EdgeInsets.all(14),
            child: Text(
              controller.statusMessage,
              style: const TextStyle(fontWeight: FontWeight.w800),
            ),
          ),
        ],
      ],
    );
  }

  Widget _section(BuildContext context, String title, List<Widget> tiles) {
    return GlassCard(
      padding: const EdgeInsets.fromLTRB(10, 12, 10, 10),
      borderRadius: 24,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(8, 0, 8, 8),
            child: Text(
              title,
              style: Theme.of(context).textTheme.titleSmall?.copyWith(
                    fontWeight: FontWeight.w900,
                  ),
            ),
          ),
          ...tiles,
        ],
      ),
    );
  }

  Widget _tile(
    BuildContext context,
    IconData icon,
    String title,
    String subtitle, {
    VoidCallback? onTap,
  }) {
    return ListTile(
      onTap: onTap,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(18)),
      leading: Container(
        width: 42,
        height: 42,
        decoration: BoxDecoration(
          color: Theme.of(context).colorScheme.primary.withValues(alpha: .10),
          borderRadius: BorderRadius.circular(14),
        ),
        child: Icon(icon, color: Theme.of(context).colorScheme.primary),
      ),
      title: Text(title, style: const TextStyle(fontWeight: FontWeight.w800)),
      subtitle: Text(subtitle, style: const TextStyle(fontSize: 10.5)),
      trailing: const Icon(Icons.chevron_left_rounded),
    );
  }

  Widget _switchTile(
    BuildContext context,
    IconData icon,
    String title,
    bool value,
    ValueChanged<bool> onChanged,
  ) {
    return SwitchListTile.adaptive(
      value: value,
      onChanged: onChanged,
      secondary: Container(
        width: 42,
        height: 42,
        decoration: BoxDecoration(
          color: AppTheme.turquoise.withValues(alpha: .10),
          borderRadius: BorderRadius.circular(14),
        ),
        child: Icon(icon, color: AppTheme.turquoise),
      ),
      title: Text(title, style: const TextStyle(fontWeight: FontWeight.w800)),
      contentPadding: const EdgeInsets.symmetric(horizontal: 8),
    );
  }

  String _themeTitle(String value) {
    return {
          'skyBlue': 'آبی آسمانی',
          'red': 'قرمز',
          'purple': 'بنفش',
          'emerald': 'سبز زمردی',
        }[value] ??
        'آبی آسمانی';
  }

  String _darkTitle(String value) {
    return {
          'system': 'خودکار (سیستم)',
          'light': 'روشن',
          'dark': 'تاریک',
        }[value] ??
        'خودکار (سیستم)';
  }

  Future<void> _themeDialog(BuildContext context) async {
    final current = controller.security.themeColor;
    final result = await showDialog<String>(
      context: context,
      builder: (ctx) => SimpleDialog(
        title: const Text('رنگ برنامه'),
        children: [
          for (final item in ['skyBlue', 'red', 'purple', 'emerald'])
            SimpleDialogOption(
              onPressed: () => Navigator.pop(ctx, item),
              child: Row(
                children: [
                  Container(
                    width: 18,
                    height: 18,
                    decoration: BoxDecoration(
                      color: {
                        'skyBlue': AppTheme.skyBlue,
                        'red': AppTheme.red,
                        'purple': AppTheme.purple,
                        'emerald': AppTheme.turquoise,
                      }[item],
                      shape: BoxShape.circle,
                    ),
                  ),
                  const SizedBox(width: 10),
                  Expanded(child: Text(_themeTitle(item))),
                  if (current == item) const Icon(Icons.check_rounded),
                ],
              ),
            ),
        ],
      ),
    );

    if (result == null) return;
    await controller.security.setThemeColor(result);
    controller.refresh();
  }

  Future<void> _darkDialog(BuildContext context) async {
    final current = controller.security.darkMode;
    final result = await showDialog<String>(
      context: context,
      builder: (ctx) => SimpleDialog(
        title: const Text('حالت نمایش'),
        children: [
          for (final item in ['system', 'light', 'dark'])
            SimpleDialogOption(
              onPressed: () => Navigator.pop(ctx, item),
              child: Row(
                children: [
                  Expanded(child: Text(_darkTitle(item))),
                  if (item == current) const Icon(Icons.check_rounded),
                ],
              ),
            ),
        ],
      ),
    );

    if (result == null) return;
    await controller.security.setDarkMode(result);
    controller.refresh();
  }

  Future<void> _pinDialog(BuildContext context) async {
    if (controller.security.isPinEnabled) {
      final action = await showDialog<String>(
        context: context,
        builder: (ctx) => AlertDialog(
          title: const Text('امنیت برنامه'),
          content: const Text('قفل برنامه فعال است.'),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(ctx, 'remove'),
              child: const Text('حذف PIN'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(ctx, 'change'),
              child: const Text('تغییر PIN'),
            ),
          ],
        ),
      );

      if (action == 'remove') {
        await controller.security.removePin();
        controller.refresh();
        return;
      }
      if (action != 'change') return;
    }

    final first = TextEditingController();
    final second = TextEditingController();
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('تنظیم رمز قفل'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: first,
              obscureText: true,
              keyboardType: TextInputType.number,
              maxLength: 6,
              decoration: fieldDecoration(ctx, 'رمز حداقل ۴ رقم'),
            ),
            const SizedBox(height: 9),
            TextField(
              controller: second,
              obscureText: true,
              keyboardType: TextInputType.number,
              maxLength: 6,
              decoration: fieldDecoration(ctx, 'تکرار رمز'),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('انصراف'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(
              ctx,
              first.text.length >= 4 && first.text == second.text,
            ),
            child: const Text('ذخیره'),
          ),
        ],
      ),
    );

    if (ok != true) return;
    await controller.security.setPin(first.text);
    controller.unlocked = true;
    controller.refresh();
  }

  Future<void> _donation(BuildContext context) async {
    final amount = TextEditingController();
    final custom = TextEditingController();
    final name = TextEditingController();
    final phone = TextEditingController();
    final note = TextEditingController();
    Uint8List? photo;

    await showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setState) {
          return Container(
            padding: EdgeInsets.fromLTRB(
              18,
              14,
              18,
              24 + MediaQuery.of(ctx).viewInsets.bottom,
            ),
            decoration: BoxDecoration(
              color: Theme.of(ctx).colorScheme.surface,
              borderRadius: const BorderRadius.vertical(
                top: Radius.circular(30),
              ),
            ),
            child: SingleChildScrollView(
              child: Column(
                children: [
                  Text(
                    'حمایت از توسعه‌دهنده',
                    style: Theme.of(ctx).textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.w900,
                        ),
                  ),
                  const SizedBox(height: 12),
                  Text(
                    'رسید و اطلاعات پشتیبانی ابتدا در Railway ثبت می‌شود و سپس برای بله ارسال می‌گردد.',
                    style: Theme.of(ctx).textTheme.bodySmall,
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: amount,
                    keyboardType: TextInputType.number,
                    decoration: fieldDecoration(ctx, 'مبلغ حمایت (تومان)'),
                  ),
                  const SizedBox(height: 9),
                  TextField(
                    controller: custom,
                    decoration: fieldDecoration(ctx, 'یا مبلغ دلخواه'),
                  ),
                  const SizedBox(height: 9),
                  TextField(
                    controller: name,
                    decoration: fieldDecoration(ctx, 'نام پرداخت‌کننده'),
                  ),
                  const SizedBox(height: 9),
                  TextField(
                    controller: phone,
                    keyboardType: TextInputType.phone,
                    decoration: fieldDecoration(ctx, 'شماره تماس'),
                  ),
                  const SizedBox(height: 9),
                  TextField(
                    controller: note,
                    maxLines: 3,
                    decoration: fieldDecoration(ctx, 'یادداشت'),
                  ),
                  const SizedBox(height: 9),
                  Row(
                    children: [
                      Expanded(
                        child: OutlinedButton.icon(
                          onPressed: () async {
                            final picked = await ImagePicker().pickImage(
                              source: ImageSource.gallery,
                              imageQuality: 88,
                            );
                            if (picked == null) return;
                            photo = await picked.readAsBytes();
                            setState(() {});
                          },
                          icon: const Icon(Icons.image_outlined),
                          label: Text(
                            photo == null ? 'افزودن فیش' : 'فیش انتخاب شد',
                          ),
                        ),
                      ),
                      const SizedBox(width: 9),
                      Expanded(
                        child: PrimaryButton(
                          label: 'ارسال',
                          icon: Icons.send_rounded,
                          onPressed: () {
                            Navigator.pop(ctx);
                            controller.donation(
                              amount: parseInt(amount.text),
                              customAmountText: custom.text.trim(),
                              payerName: name.text.trim(),
                              payerPhone: phone.text.trim(),
                              note: note.text.trim(),
                              photoBytes: photo,
                            );
                          },
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  Future<void> _pdf(BuildContext context) async {
    final document = pw.Document();
    final vehicles = controller.vehicles;
    final fuelLogs = controller.fuelLogs;
    final history = controller.serviceHistory;

    document.addPage(
      pw.MultiPage(
        build: (ctx) => [
          pw.Header(level: 0, text: 'SookhtMan - Vehicle Report'),
          pw.Paragraph(
            text:
                'Vehicles: ${vehicles.length} | Fuel logs: ${fuelLogs.length} | Service records: ${history.length}',
          ),
          pw.TableHelper.fromTextArray(
            headers: const ['Vehicle', 'Plate', 'Fuel', 'Odometer'],
            data: vehicles
                .map(
                  (vehicle) => [
                    vehicle.title,
                    vehicle.formattedPlate,
                    vehicle.fuelType,
                    vehicle.currentOdometer.toString(),
                  ],
                )
                .toList(),
          ),
          pw.SizedBox(height: 18),
          pw.TableHelper.fromTextArray(
            headers: const ['Date', 'Liters', 'Cost', 'Odometer', 'Station'],
            data: fuelLogs
                .map(
                  (log) => [
                    dateFa(log.dateMillis),
                    log.liters.toStringAsFixed(1),
                    log.totalCost.toString(),
                    log.odometer.toString(),
                    log.stationName,
                  ],
                )
                .toList(),
          ),
          pw.SizedBox(height: 18),
          pw.TableHelper.fromTextArray(
            headers: const ['Date', 'Service', 'Cost', 'Odometer'],
            data: history
                .map(
                  (item) => [
                    dateFa(item.dateMillis),
                    item.serviceType,
                    item.cost.toString(),
                    item.odometer.toString(),
                  ],
                )
                .toList(),
          ),
        ],
      ),
    );

    await Printing.layoutPdf(onLayout: (format) async => document.save());
  }

  Future<void> _excel(BuildContext context) async {
    final excel = Excel.createExcel();
    final sheet = excel['گزارش'];

    sheet.appendRow([
      TextCellValue('عنوان'),
      TextCellValue('مقدار'),
      TextCellValue('جزئیات'),
    ]);

    for (final vehicle in controller.vehicles) {
      sheet.appendRow([
        TextCellValue('خودرو'),
        TextCellValue(vehicle.title),
        TextCellValue(vehicle.formattedPlate),
      ]);
    }

    for (final log in controller.fuelLogs) {
      sheet.appendRow([
        TextCellValue('سوخت'),
        TextCellValue(log.liters.toStringAsFixed(1)),
        TextCellValue('${log.totalCost} تومان'),
      ]);
    }

    for (final history in controller.serviceHistory) {
      sheet.appendRow([
        TextCellValue('سرویس'),
        TextCellValue(history.serviceType),
        TextCellValue('${history.cost} تومان'),
      ]);
    }

    final bytes = excel.save();
    if (bytes == null) return;

    final dir = await getTemporaryDirectory();
    final file = File('${dir.path}/sookhtman_report.xlsx');
    await file.writeAsBytes(bytes, flush: true);

    await SharePlus.instance.share(
      ShareParams(
        files: [XFile(file.path)],
        text: 'گزارش Excel مدیریت سوخت و استعلام',
      ),
    );
  }

  String _reportText() {
    return 'گزارش مدیریت سوخت و استعلام\n'
        'خودروها: ${controller.vehicles.length}\n'
        'سوخت: ${controller.fuelLogs.length} رکورد\n'
        'هزینه سوخت: ${money(controller.totalFuelCost)} تومان\n'
        'حجم سوخت: ${controller.totalLiters.toStringAsFixed(1)} لیتر\n'
        'سرویس‌ها: ${controller.serviceHistory.length} رکورد\n'
        'درخواست‌ها: ${controller.requests.length} مورد';
  }

  Future<void> _copyReport(BuildContext context) async {
    await Clipboard.setData(ClipboardData(text: _reportText()));
    if (!context.mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('متن گزارش کپی شد.')),
    );
  }

  Future<void> _share(BuildContext context) async {
    await SharePlus.instance.share(
      ShareParams(
        text: _reportText(),
        subject: 'گزارش مدیریت سوخت و استعلام',
      ),
    );
  }
}
