import 'package:flutter/material.dart';

import '../app_controller.dart';
import '../core/theme.dart';
import '../core/utils.dart';
import '../data/models.dart';
import '../widgets/glass.dart';

class RemindersScreen extends StatefulWidget {
  const RemindersScreen({super.key, required this.controller});

  final AppController controller;

  @override
  State<RemindersScreen> createState() => _RemindersScreenState();
}

class _RemindersScreenState extends State<RemindersScreen> {
  int tab = 0;

  @override
  Widget build(BuildContext context) {
    final vehicle = widget.controller.activeVehicle;

    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 14, 16, 30),
      children: [
        const GradientHeader(
          title: 'سرویس و نگهداری',
          subtitle:
              'یادآور سرویس، سوابق تعمیرات، محاسبه مصرف و معاینه فنی را یک‌جا مدیریت کنید',
        ),
        const SizedBox(height: 16),
        GlassCard(
          padding: const EdgeInsets.all(6),
          borderRadius: 22,
          child: Row(
            children: [
              Expanded(child: _tab(context, 0, 'سوابق', Icons.history_rounded)),
              Expanded(
                child: _tab(
                  context,
                  1,
                  'یادآورها',
                  Icons.notifications_active_rounded,
                ),
              ),
              Expanded(
                child: _tab(context, 2, 'محاسبه‌گر', Icons.calculate_rounded),
              ),
              Expanded(
                child: _tab(context, 3, 'معاینه فنی', Icons.fact_check_rounded),
              ),
            ],
          ),
        ),
        const SizedBox(height: 14),
        if (vehicle == null)
          const GlassCard(
            child: Center(child: Text('ابتدا یک خودرو ثبت کنید.')),
          )
        else
          AnimatedSwitcher(
            duration: AppMotion.normal,
            switchInCurve: AppMotion.curve,
            switchOutCurve: AppMotion.curve,
            child: KeyedSubtree(
              key: ValueKey(tab),
              child: switch (tab) {
                0 => _history(context, vehicle),
                1 => _reminders(context, vehicle),
                2 => _calculator(context),
                _ => _inspection(context, vehicle),
              },
            ),
          ),
      ],
    );
  }

  Widget _tab(
    BuildContext context,
    int index,
    String label,
    IconData icon,
  ) {
    final active = tab == index;
    final colors = Theme.of(context).colorScheme;

    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      onTap: () => setState(() => tab = index),
      child: AnimatedContainer(
        duration: AppMotion.fast,
        curve: AppMotion.curve,
        margin: const EdgeInsets.symmetric(horizontal: 2),
        padding: const EdgeInsets.symmetric(vertical: 8),
        decoration: BoxDecoration(
          color: active ? colors.primary.withOpacity(.11) : Colors.transparent,
          borderRadius: BorderRadius.circular(17),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            AnimatedScale(
              scale: active ? 1.0 : .92,
              duration: AppMotion.fast,
              curve: AppMotion.curve,
              child: Icon(
                icon,
                size: 19,
                color: active ? colors.primary : colors.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 3),
            Text(
              label,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: TextStyle(
                fontSize: 10,
                fontWeight: active ? FontWeight.w900 : FontWeight.w600,
                color: active ? colors.primary : colors.onSurface,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _history(BuildContext context, Vehicle vehicle) {
    final items = widget.controller.serviceHistory
        .where((item) => item.vehicleId == vehicle.id)
        .toList()
      ..sort((a, b) => b.dateMillis.compareTo(a.dateMillis));

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SectionTitle(
          title: 'سوابق سرویس و تعمیرات',
          action: IconButton(
            tooltip: 'ثبت سابقه',
            onPressed: () => _addHistory(context, vehicle),
            icon: const Icon(Icons.add_rounded),
          ),
        ),
        const SizedBox(height: 7),
        if (items.isEmpty)
          const GlassCard(child: Text('هنوز سرویس یا تعمیراتی ثبت نشده است.'))
        else
          ...items.map(
            (history) => Padding(
              padding: const EdgeInsets.only(bottom: 9),
              child: GlassCard(
                padding: const EdgeInsets.all(15),
                borderRadius: 21,
                child: Row(
                  children: [
                    Container(
                      width: 46,
                      height: 46,
                      decoration: BoxDecoration(
                        color: AppTheme.purple.withOpacity(.10),
                        borderRadius: BorderRadius.circular(15),
                      ),
                      child: const Icon(
                        Icons.build_circle_rounded,
                        color: AppTheme.purple,
                      ),
                    ),
                    const SizedBox(width: 11),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            history.serviceType.isEmpty
                                ? 'سرویس خودرو'
                                : history.serviceType,
                            style: const TextStyle(fontWeight: FontWeight.w900),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            '${money(history.cost)} تومان • ${money(history.odometer)} km • ${dateFa(history.dateMillis)}',
                            style: const TextStyle(fontSize: 10.5),
                          ),
                          if (history.mechanicOrShop.isNotEmpty)
                            Text(
                              history.mechanicOrShop,
                              style: const TextStyle(fontSize: 10.5),
                            ),
                          if (history.itemsChanged.isNotEmpty)
                            Text(
                              history.itemsChanged,
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis,
                              style: const TextStyle(fontSize: 10.5),
                            ),
                        ],
                      ),
                    ),
                    IconButton(
                      tooltip: 'حذف',
                      onPressed: () => _confirmDeleteHistory(context, history),
                      icon: const Icon(Icons.delete_outline_rounded),
                    ),
                  ],
                ),
              ),
            ),
          ),
      ],
    );
  }

  Widget _reminders(BuildContext context, Vehicle vehicle) {
    final items = widget.controller.reminders
        .where((item) => item.vehicleId == vehicle.id)
        .toList()
      ..sort((a, b) => a.targetDateMillis.compareTo(b.targetDateMillis));

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SectionTitle(
          title: 'یادآورهای فعال',
          action: IconButton(
            tooltip: 'افزودن یادآور',
            onPressed: () => _addReminder(context, vehicle),
            icon: const Icon(Icons.add_rounded),
          ),
        ),
        const SizedBox(height: 7),
        if (items.isEmpty)
          const GlassCard(child: Text('یادآوری برای سرویس بعدی ثبت نشده است.'))
        else
          ...items.map(
            (reminder) => Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: Dismissible(
                key: ValueKey(reminder.id),
                direction: DismissDirection.endToStart,
                background: Builder(
                  builder: (context) => Container(
                    decoration: BoxDecoration(
                      color: Theme.of(context).colorScheme.errorContainer,
                      borderRadius: BorderRadius.circular(20),
                    ),
                    alignment: Alignment.centerLeft,
                    padding: const EdgeInsets.only(left: 18),
                    child: Icon(
                      Icons.delete_outline,
                      color: Theme.of(context).colorScheme.error,
                    ),
                  ),
                ),
                onDismissed: (_) => widget.controller.deleteReminder(reminder),
                child: GlassCard(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 10),
                  borderRadius: 20,
                  child: Row(
                    children: [
                      Checkbox(
                        value: reminder.isCompleted,
                        onChanged: (_) => widget.controller.toggleReminder(reminder),
                      ),
                      Container(
                        width: 40,
                        height: 40,
                        decoration: BoxDecoration(
                          color: Theme.of(context).colorScheme.primary.withOpacity(.10),
                          borderRadius: BorderRadius.circular(13),
                        ),
                        child: Icon(
                          reminder.isCompleted
                              ? Icons.check_circle_rounded
                              : Icons.notifications_active_rounded,
                          color: Theme.of(context).colorScheme.primary,
                          size: 20,
                        ),
                      ),
                      const SizedBox(width: 10),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              reminder.serviceType,
                              style: TextStyle(
                                fontWeight: FontWeight.w900,
                                decoration: reminder.isCompleted
                                    ? TextDecoration.lineThrough
                                    : null,
                              ),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              '${dateFa(reminder.targetDateMillis)} • ${money(reminder.targetOdometer)} km',
                              style: const TextStyle(fontSize: 10.5),
                            ),
                            if (reminder.notes.isNotEmpty)
                              Text(
                                reminder.notes,
                                maxLines: 2,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(fontSize: 10.5),
                              ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
      ],
    );
  }

  Widget _calculator(BuildContext context) {
    final distance = TextEditingController();
    final liters = TextEditingController();
    final price = TextEditingController();
    String result = '—';

    return StatefulBuilder(
      builder: (context, setState) {
        return GlassCard(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'محاسبه‌گر هزینه و مصرف',
                style: Theme.of(context).textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w900,
                    ),
              ),
              const SizedBox(height: 6),
              const Text(
                'با وارد کردن مسافت، حجم سوخت و قیمت هر لیتر، مصرف میانگین و هزینه محاسبه می‌شود.',
                style: TextStyle(fontSize: 11),
              ),
              const SizedBox(height: 12),
              TextField(
                controller: distance,
                keyboardType: TextInputType.number,
                decoration: fieldDecoration(context, 'مسافت طی‌شده (کیلومتر)'),
              ),
              const SizedBox(height: 9),
              TextField(
                controller: liters,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                decoration: fieldDecoration(context, 'سوخت مصرف‌شده (لیتر)'),
              ),
              const SizedBox(height: 9),
              TextField(
                controller: price,
                keyboardType: TextInputType.number,
                decoration: fieldDecoration(context, 'قیمت هر لیتر (تومان)'),
              ),
              const SizedBox(height: 12),
              PrimaryButton(
                expand: false,
                label: 'محاسبه',
                icon: Icons.calculate_outlined,
                onPressed: () {
                  final d = parseDouble(distance.text);
                  final l = parseDouble(liters.text);
                  final p = parseInt(price.text);
                  final consumption = d > 0 ? l / d * 100 : 0;
                  final cost = (l * p).round();

                  setState(
                    () => result =
                        'مصرف: ${consumption.toStringAsFixed(2)} لیتر در ۱۰۰ km • هزینه: ${money(cost)} تومان',
                  );
                },
              ),
              const SizedBox(height: 12),
              AnimatedSwitcher(
                duration: AppMotion.fast,
                child: Text(
                  result,
                  key: ValueKey(result),
                  style: const TextStyle(fontWeight: FontWeight.w900),
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _inspection(BuildContext context, Vehicle vehicle) {
    final expiry = vehicle.effectiveInspectionExpiry;
    final remainingDays = DateTime.fromMillisecondsSinceEpoch(expiry)
        .difference(DateTime.now())
        .inDays;
    final expired = remainingDays < 0;

    return GlassCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 48,
                height: 48,
                decoration: BoxDecoration(
                  color: AppTheme.turquoise.withOpacity(.11),
                  borderRadius: BorderRadius.circular(15),
                ),
                child: const Icon(Icons.fact_check_rounded, color: AppTheme.turquoise),
              ),
              const SizedBox(width: 11),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'معاینه فنی',
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.w900,
                          ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      vehicle.inspectionCenter.isEmpty
                          ? 'مرکز ثبت نشده'
                          : 'مرکز: ${vehicle.inspectionCenter}',
                      style: const TextStyle(fontSize: 11),
                    ),
                  ],
                ),
              ),
              IconButton(
                tooltip: 'ویرایش',
                onPressed: () => _editInspection(context, vehicle),
                icon: const Icon(Icons.edit_outlined),
              ),
            ],
          ),
          const SizedBox(height: 14),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: (expired ? AppTheme.red : AppTheme.turquoise).withOpacity(.08),
              borderRadius: BorderRadius.circular(18),
            ),
            child: Row(
              children: [
                Icon(
                  expired ? Icons.warning_amber_rounded : Icons.verified_rounded,
                  color: expired ? AppTheme.red : AppTheme.turquoise,
                ),
                const SizedBox(width: 9),
                Expanded(
                  child: Text(
                    expired
                        ? 'معاینه فنی منقضی شده است.'
                        : 'اعتبار باقی‌مانده: $remainingDays روز',
                    style: TextStyle(
                      fontWeight: FontWeight.w900,
                      color: expired ? AppTheme.red : AppTheme.turquoise,
                    ),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 10),
          Text(
            'تاریخ اعتبار: ${dateFa(expiry)}',
            style: const TextStyle(fontWeight: FontWeight.w800),
          ),
        ],
      ),
    );
  }

  Future<void> _addHistory(BuildContext context, Vehicle vehicle) async {
    final item = await _historyForm(context, vehicle);
    if (item == null) return;
    await widget.controller.addHistory(item);
    if (mounted) setState(() {});
  }

  Future<void> _addReminder(BuildContext context, Vehicle vehicle) async {
    final item = await _reminderForm(context, vehicle);
    if (item == null) return;
    await widget.controller.addReminder(item);
    if (mounted) setState(() {});
  }

  Future<void> _editInspection(BuildContext context, Vehicle vehicle) async {
    final result = await _inspectionInfo(context, vehicle);
    if (result == null) return;
    await widget.controller.updateVehicle(
      vehicle.copyWith(
        inspectionExpiryMillis: result.$1,
        inspectionCenter: result.$2,
      ),
    );
    if (mounted) setState(() {});
  }

  Future<void> _confirmDeleteHistory(
    BuildContext context,
    ServiceHistory history,
  ) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('حذف سابقه'),
        content: const Text('این سابقه سرویس حذف شود؟'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('انصراف'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('حذف'),
          ),
        ],
      ),
    );
    if (ok == true) await widget.controller.deleteHistory(history);
  }
}

Future<ServiceHistory?> _historyForm(
  BuildContext context,
  Vehicle vehicle,
) async {
  final type = TextEditingController();
  final items = TextEditingController();
  final odometer = TextEditingController(text: vehicle.currentOdometer.toString());
  final nextOdometer = TextEditingController();
  final cost = TextEditingController();
  final shop = TextEditingController();
  final notes = TextEditingController();

  return showDialog<ServiceHistory>(
    context: context,
    builder: (ctx) => AlertDialog(
      title: const Text('ثبت سابقه سرویس'),
      content: SingleChildScrollView(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: type,
              decoration: fieldDecoration(ctx, 'نوع سرویس'),
            ),
            const SizedBox(height: 9),
            TextField(
              controller: items,
              maxLines: 2,
              decoration: fieldDecoration(ctx, 'قطعات / اقلام تعویض‌شده'),
            ),
            const SizedBox(height: 9),
            TextField(
              controller: odometer,
              keyboardType: TextInputType.number,
              decoration: fieldDecoration(ctx, 'کیلومتر فعلی'),
            ),
            const SizedBox(height: 9),
            TextField(
              controller: nextOdometer,
              keyboardType: TextInputType.number,
              decoration: fieldDecoration(ctx, 'کیلومتر سرویس بعدی'),
            ),
            const SizedBox(height: 9),
            TextField(
              controller: cost,
              keyboardType: TextInputType.number,
              decoration: fieldDecoration(ctx, 'هزینه (تومان)'),
            ),
            const SizedBox(height: 9),
            TextField(
              controller: shop,
              decoration: fieldDecoration(ctx, 'تعمیرگاه / تعویض روغنی'),
            ),
            const SizedBox(height: 9),
            TextField(
              controller: notes,
              maxLines: 2,
              decoration: fieldDecoration(ctx, 'یادداشت'),
            ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(ctx),
          child: const Text('انصراف'),
        ),
        FilledButton(
          onPressed: () {
            Navigator.pop(
              ctx,
              ServiceHistory(
                vehicleId: vehicle.id!,
                serviceType: type.text.trim().isEmpty ? 'سرویس خودرو' : type.text.trim(),
                itemsChanged: items.text.trim(),
                odometer: parseInt(odometer.text),
                nextDueOdometer: parseInt(nextOdometer.text),
                dateMillis: DateTime.now().millisecondsSinceEpoch,
                cost: parseInt(cost.text),
                mechanicOrShop: shop.text.trim(),
                notes: notes.text.trim(),
              ),
            );
          },
          child: const Text('ثبت'),
        ),
      ],
    ),
  );
}

Future<ServiceReminder?> _reminderForm(
  BuildContext context,
  Vehicle vehicle,
) async {
  final type = TextEditingController();
  final odometer = TextEditingController();
  final notes = TextEditingController();
  DateTime selected = DateTime.now().add(const Duration(days: 30));

  return showDialog<ServiceReminder>(
    context: context,
    builder: (ctx) => StatefulBuilder(
      builder: (ctx, setState) => AlertDialog(
        title: const Text('یادآور سرویس'),
        content: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: type,
                decoration: fieldDecoration(ctx, 'نوع سرویس'),
              ),
              const SizedBox(height: 9),
              ListTile(
                contentPadding: EdgeInsets.zero,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                  side: BorderSide(
                    color: Theme.of(ctx).colorScheme.outline.withOpacity(.35),
                  ),
                ),
                leading: const Icon(Icons.event_outlined),
                title: const Text('تاریخ سرویس'),
                subtitle: Text(dateFa(selected.millisecondsSinceEpoch)),
                trailing: const Icon(Icons.edit_calendar_outlined),
                onTap: () async {
                  final picked = await showDatePicker(
                    context: ctx,
                    initialDate: selected,
                    firstDate: DateTime.now().subtract(const Duration(days: 1)),
                    lastDate: DateTime.now().add(const Duration(days: 3650)),
                    locale: const Locale('fa'),
                  );
                  if (picked == null) return;
                  setState(() => selected = DateTime(
                        picked.year,
                        picked.month,
                        picked.day,
                        12,
                      ));
                },
              ),
              const SizedBox(height: 9),
              TextField(
                controller: odometer,
                keyboardType: TextInputType.number,
                decoration: fieldDecoration(ctx, 'کیلومتر هدف'),
              ),
              const SizedBox(height: 9),
              TextField(
                controller: notes,
                maxLines: 2,
                decoration: fieldDecoration(ctx, 'یادداشت'),
              ),
            ],
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('انصراف'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.pop(
                ctx,
                ServiceReminder(
                  vehicleId: vehicle.id!,
                  serviceType: type.text.trim().isEmpty ? 'سرویس دوره‌ای' : type.text.trim(),
                  targetDateMillis: selected.millisecondsSinceEpoch,
                  targetOdometer: parseInt(odometer.text),
                  notes: notes.text.trim(),
                ),
              );
            },
            child: const Text('ثبت یادآور'),
          ),
        ],
      ),
    ),
  );
}

Future<(int, String)?> _inspectionInfo(
  BuildContext context,
  Vehicle vehicle,
) async {
  final center = TextEditingController(text: vehicle.inspectionCenter);
  DateTime selected = DateTime.fromMillisecondsSinceEpoch(
    vehicle.effectiveInspectionExpiry,
  );

  return showDialog<(int, String)>(
    context: context,
    builder: (ctx) => StatefulBuilder(
      builder: (ctx, setState) => AlertDialog(
        title: const Text('تنظیم معاینه فنی'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              contentPadding: EdgeInsets.zero,
              leading: const Icon(Icons.event_available_rounded),
              title: const Text('تاریخ انقضا'),
              subtitle: Text(dateFa(selected.millisecondsSinceEpoch)),
              onTap: () async {
                final picked = await showDatePicker(
                  context: ctx,
                  initialDate: selected.isBefore(DateTime.now())
                      ? DateTime.now()
                      : selected,
                  firstDate: DateTime.now(),
                  lastDate: DateTime.now().add(const Duration(days: 3650)),
                  locale: const Locale('fa'),
                );
                if (picked == null) return;
                setState(() => selected = DateTime(
                      picked.year,
                      picked.month,
                      picked.day,
                      12,
                    ));
              },
            ),
            const SizedBox(height: 9),
            TextField(
              controller: center,
              decoration: fieldDecoration(ctx, 'نام مرکز معاینه فنی'),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('انصراف'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(
              ctx,
              (
                selected.millisecondsSinceEpoch,
                center.text.trim(),
              ),
            ),
            child: const Text('ذخیره'),
          ),
        ],
      ),
    ),
  );
}
