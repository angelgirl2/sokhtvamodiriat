import 'package:fl_chart/fl_chart.dart';
import 'package:flutter/material.dart';

import '../app_controller.dart';
import '../core/theme.dart';
import '../core/utils.dart';
import '../data/models.dart';
import '../widgets/glass.dart';
import '../widgets/plate.dart';

class FuelScreen extends StatelessWidget {
  const FuelScreen({super.key, required this.controller});

  final AppController controller;

  @override
  Widget build(BuildContext context) {
    final vehicle = controller.activeVehicle;
    final logs = controller.activeFuelLogs;

    return RefreshIndicator(
      onRefresh: controller.afterChange,
      child: ListView(
        physics: const AlwaysScrollableScrollPhysics(parent: BouncingScrollPhysics()),
        padding: const EdgeInsets.fromLTRB(16, 14, 16, 30),
        children: [
          GradientHeader(
            title: 'مدیریت خودرو و سوخت',
            subtitle: 'ثبت، پایش هوشمند و تحلیل هزینه‌های بنزین',
            trailing: IconButton.filledTonal(
              onPressed: () => _addVehicle(context),
              icon: const Icon(Icons.add_rounded),
            ),
          ),
          const SizedBox(height: 18),
          if (vehicle == null)
            _empty(context)
          else ...[
            SizedBox(
              height: 52,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                itemCount: controller.vehicles.length,
                separatorBuilder: (_, _) => const SizedBox(width: 8),
                itemBuilder: (context, index) {
                  final item = controller.vehicles[index];
                  final selected = item.id == vehicle.id;
                  return ChoiceChip(
                    selected: selected,
                    onSelected: (_) => controller.selectVehicle(item.id),
                    avatar: const Icon(Icons.directions_car_rounded, size: 17),
                    label: Text(item.title),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(17)),
                  );
                },
              ),
            ),
            const SizedBox(height: 12),
            _vehicleCard(context, vehicle),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: StatTile(
                    icon: Icons.payments_rounded,
                    title: 'هزینه سوخت',
                    value: '${money(controller.totalFuelCost)} تومان',
                    color: AppTheme.purple,
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: StatTile(
                    icon: Icons.local_gas_station_rounded,
                    title: 'مصرف ثبت‌شده',
                    value: '${controller.totalLiters.toStringAsFixed(1)} لیتر',
                    color: AppTheme.turquoise,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            _quickActions(context, vehicle),
            const SizedBox(height: 16),
            SectionTitle(
              title: 'نمودار مصرف اخیر',
              action: TextButton(
                onPressed: () => _report(context),
                child: const Text('گزارش‌ها'),
              ),
            ),
            const SizedBox(height: 10),
            GlassCard(
              padding: const EdgeInsets.fromLTRB(8, 18, 18, 12),
              child: SizedBox(height: 220, child: _FuelChart(logs: logs)),
            ),
            const SizedBox(height: 16),
            SectionTitle(
              title: 'سوخت‌گیری‌های اخیر',
              action: TextButton.icon(
                onPressed: () => _addFuelLog(context, vehicle),
                icon: const Icon(Icons.add, size: 17),
                label: const Text('ثبت سوخت'),
              ),
            ),
            const SizedBox(height: 8),
            if (logs.isEmpty)
              const GlassCard(
                child: Center(
                  child: Padding(
                    padding: EdgeInsets.all(24),
                    child: Text('هنوز رکورد سوختی ثبت نشده است'),
                  ),
                ),
              )
            else
              ...logs.take(12).map((log) => _fuelLogCard(context, log)),
            if (logs.length > 12)
              const Padding(
                padding: EdgeInsets.all(12),
                child: Text('۱۲ مورد آخر نمایش داده می‌شود.'),
              ),
          ],
        ],
      ),
    );
  }

  Widget _empty(BuildContext context) {
    return GlassCard(
      child: Column(
        children: [
          Container(
            width: 68,
            height: 68,
            decoration: BoxDecoration(
              color: Theme.of(context).colorScheme.primary.withValues(alpha: .12),
              shape: BoxShape.circle,
            ),
            child: Icon(
              Icons.directions_car_filled_rounded,
              size: 34,
              color: Theme.of(context).colorScheme.primary,
            ),
          ),
          const SizedBox(height: 16),
          Text(
            'هنوز هیچ خودرویی اضافه نشده است',
            style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w900),
          ),
          const SizedBox(height: 8),
          Text(
            'برنامه با صفر داده شروع می‌شود. برای شروع مدیریت سوخت و سرویس‌ها اولین خودرو را ثبت کنید.',
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.bodySmall?.copyWith(
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                  height: 1.5,
                ),
          ),
          const SizedBox(height: 16),
          PrimaryButton(
            label: 'ثبت اولین خودرو',
            icon: Icons.add_rounded,
            onPressed: () => _addVehicle(context),
          ),
        ],
      ),
    );
  }

  Widget _vehicleCard(BuildContext context, Vehicle vehicle) {
    return GlassCard(
      padding: const EdgeInsets.all(18),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      vehicle.title,
                      style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900),
                    ),
                    const SizedBox(height: 5),
                    Text(
                      '${vehicle.fuelType} • ${vehicle.tankCapacity.toStringAsFixed(0)} لیتر',
                      style: Theme.of(context).textTheme.bodySmall?.copyWith(
                            color: Theme.of(context).colorScheme.onSurfaceVariant,
                          ),
                    ),
                  ],
                ),
              ),
              PopupMenuButton<String>(
                onSelected: (value) async {
                  if (value == 'insurance') await _insurance(context, vehicle);
                  if (value == 'inspection') await _inspection(context, vehicle);
                  if (value == 'delete') await _deleteVehicle(context, vehicle);
                },
                itemBuilder: (_) => const [
                  PopupMenuItem(value: 'insurance', child: Text('مدیریت بیمه')),
                  PopupMenuItem(value: 'inspection', child: Text('معاینه فنی')),
                  PopupMenuItem(value: 'delete', child: Text('حذف خودرو')),
                ],
              ),
            ],
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: _infoPill(
                  Icons.speed_rounded,
                  'کیلومتر فعلی',
                  '${money(vehicle.currentOdometer)} km',
                  AppTheme.softBlue,
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: _infoPill(
                  Icons.shield_rounded,
                  'بیمه',
                  dateFa(vehicle.effectiveInsuranceExpiry),
                  AppTheme.turquoise,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Flexible(
                child: Text(
                  vehicle.formattedPlate,
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.w800),
                ),
              ),
              IranianPlate(
                first2: vehicle.plateFirst2,
                letter: vehicle.plateLetter,
                last3: vehicle.plateLast3,
                city: vehicle.plateCityCode,
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _infoPill(IconData icon, String title, String value, Color color) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: color.withValues(alpha: .09),
        borderRadius: BorderRadius.circular(18),
      ),
      child: Row(
        children: [
          Icon(icon, color: color, size: 20),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: const TextStyle(fontSize: 10, fontWeight: FontWeight.w700)),
                const SizedBox(height: 3),
                Text(value, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w900)),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _quickActions(BuildContext context, Vehicle vehicle) {
    return Row(
      children: [
        Expanded(
          child: _Action(
            color: AppTheme.skyBlue,
            icon: Icons.local_gas_station_rounded,
            text: 'ثبت سوخت',
            onTap: () => _addFuelLog(context, vehicle),
          ),
        ),
        const SizedBox(width: 8),
        Expanded(
          child: _Action(
            color: AppTheme.turquoise,
            icon: Icons.notifications_active_rounded,
            text: 'یادآور سرویس',
            onTap: () {
              controller.tab = 3;
              controller.refresh();
            },
          ),
        ),
        const SizedBox(width: 8),
        Expanded(
          child: _Action(
            color: AppTheme.purple,
            icon: Icons.shield_rounded,
            text: 'درخواست بیمه',
            onTap: () {
              controller.tab = 1;
              controller.refresh();
            },
          ),
        ),
      ],
    );
  }

  Widget _fuelLogCard(BuildContext context, FuelLog log) {
    return Dismissible(
      key: ValueKey(log.id),
      direction: DismissDirection.endToStart,
      background: Container(
        margin: const EdgeInsets.only(bottom: 8),
        decoration: BoxDecoration(
          color: Theme.of(context).colorScheme.errorContainer,
          borderRadius: BorderRadius.circular(20),
        ),
        alignment: Alignment.centerLeft,
        padding: const EdgeInsets.only(left: 20),
        child: Icon(Icons.delete_outline, color: Theme.of(context).colorScheme.error),
      ),
      confirmDismiss: (_) async {
        return await showDialog<bool>(
              context: context,
              builder: (_) => AlertDialog(
                title: const Text('حذف سوخت‌گیری'),
                content: const Text('این رکورد حذف شود؟'),
                actions: [
                  TextButton(
                    onPressed: () => Navigator.pop(context, false),
                    child: const Text('انصراف'),
                  ),
                  FilledButton(
                    onPressed: () => Navigator.pop(context, true),
                    child: const Text('حذف'),
                  ),
                ],
              ),
            ) ??
            false;
      },
      onDismissed: (_) => controller.deleteFuelLog(log),
      child: GlassCard(
        margin: const EdgeInsets.only(bottom: 8),
        padding: const EdgeInsets.all(15),
        borderRadius: 20,
        child: Row(
          children: [
            Container(
              width: 42,
              height: 42,
              decoration: BoxDecoration(
                color: AppTheme.skyBlue.withValues(alpha: .12),
                borderRadius: BorderRadius.circular(14),
              ),
              child: const Icon(Icons.local_gas_station_rounded, color: AppTheme.skyBlue),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    log.stationName.isEmpty ? 'سوخت‌گیری' : log.stationName,
                    style: const TextStyle(fontWeight: FontWeight.w800),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '${log.liters.toStringAsFixed(1)} لیتر • ${money(log.totalCost)} تومان • ${dateFa(log.dateMillis)}',
                    style: const TextStyle(fontSize: 11),
                  ),
                ],
              ),
            ),
            Text('${money(log.odometer)} km', style: const TextStyle(fontWeight: FontWeight.w800)),
          ],
        ),
      ),
    );
  }

  Future<void> _addVehicle(BuildContext context) async {
    final result = await _vehicleForm(context);
    if (result != null) await controller.addVehicle(result);
  }

  Future<void> _addFuelLog(BuildContext context, Vehicle vehicle) async {
    final result = await _fuelForm(context, vehicle);
    if (result != null) await controller.addFuelLog(result);
  }

  Future<void> _insurance(BuildContext context, Vehicle vehicle) async {
    final result = await _dateInfo(
      context,
      title: 'اطلاعات بیمه',
      currentDate: vehicle.effectiveInsuranceExpiry,
      company: vehicle.insuranceCompany,
      type: vehicle.insuranceType,
    );
    if (result != null) {
      await controller.updateVehicle(
        vehicle.copyWith(
          insuranceExpiryMillis: result.$1,
          insuranceCompany: result.$2,
          insuranceType: result.$3,
        ),
      );
    }
  }

  Future<void> _inspection(BuildContext context, Vehicle vehicle) async {
    final result = await _inspectionForm(context, vehicle);
    if (result != null) {
      await controller.updateVehicle(
        vehicle.copyWith(
          inspectionExpiryMillis: result.$1,
          inspectionCenter: result.$2,
        ),
      );
    }
  }

  Future<void> _deleteVehicle(BuildContext context, Vehicle vehicle) async {
    final yes = await showDialog<bool>(
      context: context,
      builder: (_) => AlertDialog(
        title: const Text('حذف خودرو'),
        content: Text('«${vehicle.title}» حذف شود؟ همه رکوردهای وابسته هم حذف می‌شوند.'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context, false), child: const Text('انصراف')),
          FilledButton(onPressed: () => Navigator.pop(context, true), child: const Text('حذف')),
        ],
      ),
    );
    if (yes == true) await controller.deleteVehicle(vehicle);
  }

  Future<void> _report(BuildContext context) async {
    await showDialog<void>(
      context: context,
      builder: (_) => const _ReportDialog(),
    );
  }
}

class _Action extends StatelessWidget {
  const _Action({required this.icon, required this.text, required this.color, required this.onTap});

  final IconData icon;
  final String text;
  final Color color;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(20),
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 10),
        decoration: BoxDecoration(
          color: color.withValues(alpha: .09),
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: color.withValues(alpha: .12)),
        ),
        child: Column(
          children: [
            Icon(icon, color: color, size: 22),
            const SizedBox(height: 6),
            Text(
              text,
              textAlign: TextAlign.center,
              style: const TextStyle(fontSize: 10.5, fontWeight: FontWeight.w800),
            ),
          ],
        ),
      ),
    );
  }
}

class _FuelChart extends StatelessWidget {
  const _FuelChart({required this.logs});

  final List<FuelLog> logs;

  @override
  Widget build(BuildContext context) {
    final data = logs.take(7).toList().reversed.toList();
    if (data.isEmpty) return const Center(child: Text('داده‌ای برای نمودار نداریم'));

    final maxValue = data.map((e) => e.liters).fold<double>(1, (p, e) => e > p ? e : p) + 3;
    final interval = maxValue / 4;

    return LineChart(
      LineChartData(
        minY: 0,
        maxY: maxValue,
        gridData: FlGridData(
          show: true,
          drawVerticalLine: false,
          horizontalInterval: interval <= 0 ? 1 : interval,
        ),
        titlesData: FlTitlesData(
          leftTitles: AxisTitles(
            sideTitles: SideTitles(
              showTitles: true,
              reservedSize: 38,
              getTitlesWidget: (value, meta) => Text(
                value.toStringAsFixed(0),
                style: const TextStyle(fontSize: 9),
              ),
            ),
          ),
          bottomTitles: AxisTitles(
            sideTitles: SideTitles(
              showTitles: true,
              getTitlesWidget: (value, meta) {
                final index = value.round();
                if (index < 0 || index >= data.length) return const SizedBox.shrink();
                return Padding(
                  padding: const EdgeInsets.only(top: 6),
                  child: Text('${index + 1}', style: const TextStyle(fontSize: 9)),
                );
              },
            ),
          ),
          topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
          rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
        ),
        borderData: FlBorderData(show: false),
        lineBarsData: [
          LineChartBarData(
            isCurved: true,
            barWidth: 3,
            color: Theme.of(context).colorScheme.primary,
            dotData: const FlDotData(show: true),
            belowBarData: BarAreaData(
              show: true,
              color: Theme.of(context).colorScheme.primary.withValues(alpha: .10),
            ),
            spots: [
              for (int index = 0; index < data.length; index++)
                FlSpot(index.toDouble(), data[index].liters),
            ],
          ),
        ],
      ),
    );
  }
}

Future<Vehicle?> _vehicleForm(BuildContext context) async {
  final title = TextEditingController();
  final first2 = TextEditingController();
  final letter = TextEditingController();
  final last3 = TextEditingController();
  final city = TextEditingController(text: '11');
  final tank = TextEditingController(text: '50');
  final odo = TextEditingController();
  var fuel = 'بنزین معمولی';

  return showDialog<Vehicle>(
    context: context,
    builder: (context) => StatefulBuilder(
      builder: (context, setState) => AlertDialog(
        scrollable: true,
        title: const Text('ثبت خودرو'),
        content: Column(
          children: [
            TextField(
              controller: title,
              decoration: fieldDecoration(
                context,
                'نام خودرو',
                prefixIcon: const Icon(Icons.drive_file_rename_outline),
              ),
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: first2,
                    maxLength: 2,
                    keyboardType: TextInputType.number,
                    decoration: fieldDecoration(context, 'دو رقم'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: TextField(
                    controller: letter,
                    decoration: fieldDecoration(context, 'حرف'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: TextField(
                    controller: last3,
                    maxLength: 3,
                    keyboardType: TextInputType.number,
                    decoration: fieldDecoration(context, 'سه رقم'),
                  ),
                ),
                const SizedBox(width: 8),
                SizedBox(
                  width: 70,
                  child: TextField(
                    controller: city,
                    keyboardType: TextInputType.number,
                    decoration: fieldDecoration(context, 'ایران'),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            DropdownButtonFormField<String>(
              initialValue: fuel,
              decoration: fieldDecoration(context, 'نوع سوخت'),
              items: ['بنزین معمولی', 'بنزین سوپر', 'گاز سوز CNG', 'گازوئیل']
                  .map((value) => DropdownMenuItem(value: value, child: Text(value)))
                  .toList(),
              onChanged: (value) => setState(() => fuel = value ?? fuel),
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: tank,
                    keyboardType: TextInputType.number,
                    decoration: fieldDecoration(context, 'ظرفیت باک'),
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: TextField(
                    controller: odo,
                    keyboardType: TextInputType.number,
                    decoration: fieldDecoration(context, 'کیلومتر فعلی'),
                  ),
                ),
              ],
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('انصراف'),
          ),
          FilledButton(
            onPressed: () {
              if (title.text.trim().isEmpty) return;
              Navigator.pop(
                context,
                Vehicle(
                  title: title.text.trim(),
                  plateFirst2: first2.text.trim(),
                  plateLetter: letter.text.trim(),
                  plateLast3: last3.text.trim(),
                  plateCityCode: city.text.trim(),
                  fuelType: fuel,
                  tankCapacity: parseDouble(tank.text),
                  currentOdometer: parseInt(odo.text),
                ),
              );
            },
            child: const Text('ثبت'),
          ),
        ],
      ),
    ),
  );
}

Future<FuelLog?> _fuelForm(BuildContext context, Vehicle vehicle) async {
  final liters = TextEditingController();
  final price = TextEditingController();
  final station = TextEditingController();
  final odo = TextEditingController(text: vehicle.currentOdometer.toString());
  final notes = TextEditingController();
  var full = true;

  return showDialog<FuelLog>(
    context: context,
    builder: (context) => StatefulBuilder(
      builder: (context, setState) => AlertDialog(
        scrollable: true,
        title: const Text('ثبت سوخت‌گیری'),
        content: Column(
          children: [
            TextField(
              controller: liters,
              keyboardType: TextInputType.number,
              decoration: fieldDecoration(
                context,
                'مقدار لیتر',
                prefixIcon: const Icon(Icons.local_gas_station),
              ),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: price,
              keyboardType: TextInputType.number,
              decoration: fieldDecoration(context, 'قیمت هر لیتر (تومان)'),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: station,
              decoration: fieldDecoration(context, 'جایگاه / پمپ'),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: odo,
              keyboardType: TextInputType.number,
              decoration: fieldDecoration(context, 'کیلومتر خودرو'),
            ),
            const SizedBox(height: 8),
            SwitchListTile(
              value: full,
              onChanged: (value) => setState(() => full = value),
              title: const Text('باک کامل'),
              contentPadding: EdgeInsets.zero,
            ),
            TextField(
              controller: notes,
              maxLines: 2,
              decoration: fieldDecoration(context, 'یادداشت'),
            ),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('انصراف')),
          FilledButton(
            onPressed: () {
              final litersValue = parseDouble(liters.text);
              final priceValue = parseInt(price.text);
              if (litersValue <= 0) return;
              Navigator.pop(
                context,
                FuelLog(
                  vehicleId: vehicle.id!,
                  dateMillis: DateTime.now().millisecondsSinceEpoch,
                  odometer: parseInt(odo.text),
                  liters: litersValue,
                  pricePerLiter: priceValue,
                  totalCost: (litersValue * priceValue).round(),
                  stationName: station.text.trim(),
                  isFullTank: full,
                  notes: notes.text.trim(),
                ),
              );
            },
            child: const Text('ثبت'),
          ),
        ],
      ),
    ),
  );
}

Future<(int, String, String)?> _dateInfo(
  BuildContext context, {
  required String title,
  required int currentDate,
  required String company,
  required String type,
}) async {
  var selected = DateTime.fromMillisecondsSinceEpoch(currentDate);
  final companyController = TextEditingController(text: company);
  var selectedType = type;

  return showDialog<(int, String, String)>(
    context: context,
    builder: (context) => StatefulBuilder(
      builder: (context, setState) => AlertDialog(
        title: Text(title),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextButton.icon(
              onPressed: () async {
                final date = await showDatePicker(
                  context: context,
                  initialDate: selected,
                  firstDate: DateTime(2020),
                  lastDate: DateTime(2100),
                );
                if (date != null) setState(() => selected = date);
              },
              icon: const Icon(Icons.calendar_month),
              label: Text('تاریخ انقضا: ${dateFa(selected.millisecondsSinceEpoch)}'),
            ),
            TextField(
              controller: companyController,
              decoration: fieldDecoration(context, 'شرکت بیمه'),
            ),
            const SizedBox(height: 10),
            DropdownButtonFormField<String>(
              initialValue: selectedType,
              items: ['بیمه شخص ثالث', 'بیمه بدنه', 'حوادث راننده']
                  .map((value) => DropdownMenuItem(value: value, child: Text(value)))
                  .toList(),
              onChanged: (value) => setState(() => selectedType = value ?? selectedType),
              decoration: fieldDecoration(context, 'نوع بیمه'),
            ),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('انصراف')),
          FilledButton(
            onPressed: () => Navigator.pop(
              context,
              (selected.millisecondsSinceEpoch, companyController.text.trim(), selectedType),
            ),
            child: const Text('ذخیره'),
          ),
        ],
      ),
    ),
  );
}

Future<(int, String)?> _inspectionForm(BuildContext context, Vehicle vehicle) async {
  var selected = DateTime.fromMillisecondsSinceEpoch(vehicle.effectiveInspectionExpiry);
  final center = TextEditingController(text: vehicle.inspectionCenter);

  return showDialog<(int, String)>(
    context: context,
    builder: (context) => StatefulBuilder(
      builder: (context, setState) => AlertDialog(
        title: const Text('معاینه فنی'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextButton.icon(
              onPressed: () async {
                final date = await showDatePicker(
                  context: context,
                  initialDate: selected,
                  firstDate: DateTime(2020),
                  lastDate: DateTime(2100),
                );
                if (date != null) setState(() => selected = date);
              },
              icon: const Icon(Icons.calendar_month),
              label: Text('تاریخ اعتبار: ${dateFa(selected.millisecondsSinceEpoch)}'),
            ),
            TextField(
              controller: center,
              decoration: fieldDecoration(context, 'مرکز معاینه فنی'),
            ),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('انصراف')),
          FilledButton(
            onPressed: () => Navigator.pop(
              context,
              (selected.millisecondsSinceEpoch, center.text.trim()),
            ),
            child: const Text('ذخیره'),
          ),
        ],
      ),
    ),
  );
}

class _ReportDialog extends StatelessWidget {
  const _ReportDialog();

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('گزارش‌ها'),
      content: const Text(
        'از بخش تنظیمات می‌توانید گزارش PDF، Excel و متن اشتراکی را اجرا کنید.',
      ),
      actions: [
        FilledButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('متوجه شدم'),
        ),
      ],
    );
  }
}
