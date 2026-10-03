import 'package:flutter/material.dart';

import '../app_controller.dart';
import '../core/theme.dart';
import '../core/utils.dart';
import '../data/models.dart';
import '../widgets/glass.dart';

class InquiryScreen extends StatelessWidget {
  const InquiryScreen({super.key, required this.controller});

  final AppController controller;

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 14, 16, 30),
      children: [
        GradientHeader(
          title: 'استعلام و پرداخت',
          subtitle: 'خلافی، عوارض، مالیات و سرویس‌های پرداختی خودرو را یک‌جا مدیریت کنید',
          trailing: Icon(
            Icons.search_rounded,
            color: Theme.of(context).colorScheme.primary,
            size: 29,
          ),
        ),
        const SizedBox(height: 18),
        GridView.count(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          crossAxisCount: 2,
          childAspectRatio: 1.2,
          mainAxisSpacing: 10,
          crossAxisSpacing: 10,
          children: [
            _InquiryTile(
              title: 'خلافی راهور',
              subtitle: 'استعلام و تسویه',
              icon: Icons.receipt_long_rounded,
              color: AppTheme.red,
              onTap: () => _open(context, 'استعلام خلافی راهور'),
            ),
            _InquiryTile(
              title: 'عوارض آزادراهی',
              subtitle: 'تردد و پرداخت',
              icon: Icons.alt_route_rounded,
              color: Colors.orange,
              onTap: () => _open(context, 'استعلام عوارض آزادراهی'),
            ),
            _InquiryTile(
              title: 'عوارض سالیانه',
              subtitle: 'شهرداری و نوسازی',
              icon: Icons.location_city_rounded,
              color: AppTheme.turquoise,
              onTap: () => _open(context, 'استعلام عوارض سالیانه خودرو'),
            ),
            _InquiryTile(
              title: 'مالیات نقل‌وانتقال',
              subtitle: 'تعویض پلاک و etax',
              icon: Icons.payments_rounded,
              color: AppTheme.purple,
              onTap: () => _open(context, 'استعلام مالیات نقل و انتقال خودرو'),
            ),
          ],
        ),
        const SizedBox(height: 18),
        SectionTitle(
          title: 'استعلام‌های ثبت‌شده',
          action: Text('${controller.inquiries.length} مورد'),
        ),
        const SizedBox(height: 9),
        if (controller.inquiries.isEmpty)
          GlassCard(
            child: Column(
              children: [
                Icon(
                  Icons.search_off_rounded,
                  size: 40,
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                ),
                const SizedBox(height: 10),
                const Text(
                  'هنوز استعلامی ثبت نشده است',
                  style: TextStyle(fontWeight: FontWeight.w900),
                ),
                const SizedBox(height: 6),
                const Text('یک نوع استعلام را بالا انتخاب کنید.'),
              ],
            ),
          )
        else
          ...controller.inquiries.map((item) => _card(context, item)),
      ],
    );
  }

  Widget _card(BuildContext context, InquiryRecord item) {
    final awaiting = item.status.contains('بررسی');

    return GlassCard(
      margin: const EdgeInsets.only(bottom: 9),
      padding: const EdgeInsets.all(15),
      borderRadius: 21,
      child: Column(
        children: [
          Row(
            children: [
              Container(
                width: 42,
                height: 42,
                decoration: BoxDecoration(
                  color: Theme.of(context).colorScheme.primary.withOpacity(.1),
                  borderRadius: BorderRadius.circular(14),
                ),
                child: Icon(
                  Icons.receipt_long_rounded,
                  color: Theme.of(context).colorScheme.primary,
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(item.title, style: const TextStyle(fontWeight: FontWeight.w900)),
                    const SizedBox(height: 4),
                    Text(
                      '${item.plateNumber} • ${dateFa(item.dateMillis)}',
                      style: const TextStyle(fontSize: 11),
                    ),
                  ],
                ),
              ),
              _status(item.status),
            ],
          ),
          const SizedBox(height: 9),
          Align(
            alignment: Alignment.centerRight,
            child: Text(
              'مبلغ: ${money(item.amount)} تومان',
              style: const TextStyle(fontWeight: FontWeight.w900),
            ),
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              Expanded(
                child: Text(
                  item.transactionRef.isEmpty
                      ? 'کد پیگیری در انتظار ثبت است'
                      : 'پیگیری: ${item.transactionRef}',
                  style: const TextStyle(fontSize: 10.5),
                ),
              ),
              if (awaiting)
                IconButton(
                  onPressed: controller.busy ? null : () => controller.approveInquiry(item),
                  icon: const Icon(Icons.check_circle_outline_rounded),
                  tooltip: 'تایید و تسویه',
                ),
              if (item.workflowMethod == 'DIRECT_PAYMENT' && item.status == 'در انتظار پرداخت')
                OutlinedButton(
                  onPressed: controller.busy ? null : () => controller.directPay(item),
                  child: const Text('پرداخت'),
                ),
              IconButton(
                onPressed: controller.busy ? null : () => controller.deleteInquiry(item),
                icon: const Icon(Icons.delete_outline_rounded),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _status(String text) {
    final color = text.contains('پرداخت') || text.contains('تسویه')
        ? AppTheme.turquoise
        : text.contains('بررسی')
            ? Colors.orange
            : AppTheme.softBlue;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 6),
      decoration: BoxDecoration(
        color: color.withOpacity(.1),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text(
        text,
        style: TextStyle(fontSize: 9, color: color, fontWeight: FontWeight.w800),
      ),
    );
  }

  Future<void> _open(BuildContext context, String type) async {
    final result = await showModalBottomSheet<Map<String, String>>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (ctx) => _InquirySheet(type: type, vehicle: controller.activeVehicle),
    );
    if (result == null) return;

    final direct = result['method'] == 'DIRECT_PAYMENT';
    final inquiry = InquiryRecord(
      inquiryType: type,
      title: type,
      plateNumber: result['plate'] ?? '',
      barcodeOrVin: result['identifier'] ?? '',
      nationalId: result['national'] ?? '',
      fullName: result['name'] ?? '',
      phoneNumber: result['phone'] ?? '',
      vinCode: result['vin'] ?? '',
      barcode: result['barcode'] ?? '',
      engineNumber: result['engine'] ?? '',
      chassisNumber: result['chassis'] ?? '',
      postalCode: result['postal'] ?? '',
      address: result['address'] ?? '',
      amount: parseInt(result['amount'] ?? '0'),
      workflowMethod: direct ? 'DIRECT_PAYMENT' : 'ADMIN_REVIEW',
      status: direct ? 'در انتظار پرداخت' : 'در حال بررسی توسط کارشناس',
    );
    await controller.submitInquiry(inquiry);
  }
}

class _InquiryTile extends StatelessWidget {
  const _InquiryTile({
    required this.title,
    required this.subtitle,
    required this.icon,
    required this.color,
    required this.onTap,
  });

  final String title;
  final String subtitle;
  final IconData icon;
  final Color color;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return GlassCard(
      padding: EdgeInsets.zero,
      borderRadius: 22,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(22),
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: color.withOpacity(.12),
                  borderRadius: BorderRadius.circular(14),
                ),
                child: Icon(icon, color: color),
              ),
              const Spacer(),
              Text(title, style: const TextStyle(fontWeight: FontWeight.w900)),
              const SizedBox(height: 4),
              Text(subtitle, style: const TextStyle(fontSize: 10.5)),
            ],
          ),
        ),
      ),
    );
  }
}

class _InquirySheet extends StatefulWidget {
  const _InquirySheet({required this.type, required this.vehicle});

  final String type;
  final Vehicle? vehicle;

  @override
  State<_InquirySheet> createState() => _InquirySheetState();
}

class _InquirySheetState extends State<_InquirySheet> {
  final Map<String, TextEditingController> fields = {};
  String method = 'ADMIN_REVIEW';

  @override
  void initState() {
    super.initState();
    for (final key in [
      'name',
      'national',
      'phone',
      'plate',
      'vin',
      'barcode',
      'engine',
      'chassis',
      'postal',
      'address',
      'amount',
      'identifier',
    ]) {
      fields[key] = TextEditingController();
    }

    final vehicle = widget.vehicle;
    if (vehicle != null) {
      fields['plate']!.text = vehicle.formattedPlate;
    }
  }

  @override
  void dispose() {
    for (final controller in fields.values) {
      controller.dispose();
    }
    super.dispose();
  }

  Widget field(
    String key,
    String hint, {
    TextInputType? keyboard,
    int lines = 1,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 9),
      child: TextField(
        controller: fields[key],
        keyboardType: keyboard,
        maxLines: lines,
        decoration: fieldDecoration(context, hint),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Directionality(
      textDirection: TextDirection.rtl,
      child: Container(
        padding: EdgeInsets.only(
          left: 18,
          right: 18,
          top: 14,
          bottom: 24 + MediaQuery.of(context).viewInsets.bottom,
        ),
        decoration: BoxDecoration(
          color: Theme.of(context).colorScheme.surface,
          borderRadius: const BorderRadius.vertical(top: Radius.circular(30)),
        ),
        child: SafeArea(
          top: false,
          child: SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Center(
                  child: Container(
                    width: 42,
                    height: 4,
                    decoration: BoxDecoration(
                      color: Colors.grey.withOpacity(.28),
                      borderRadius: BorderRadius.circular(4),
                    ),
                  ),
                ),
                const SizedBox(height: 13),
                Text(
                  widget.type,
                  style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900),
                ),
                const SizedBox(height: 12),
                field('name', 'نام مالک'),
                field('national', 'کد ملی', keyboard: TextInputType.number),
                field('phone', 'شماره تماس', keyboard: TextInputType.phone),
                field('plate', 'پلاک خودرو'),
                Row(
                  children: [
                    Expanded(child: field('vin', 'VIN')),
                    const SizedBox(width: 8),
                    Expanded(child: field('barcode', 'بارکد')),
                  ],
                ),
                Row(
                  children: [
                    Expanded(child: field('engine', 'موتور')),
                    const SizedBox(width: 8),
                    Expanded(child: field('chassis', 'شاسی')),
                  ],
                ),
                field('postal', 'کد پستی'),
                field('address', 'آدرس', lines: 3),
                field('amount', 'مبلغ برآوردی (تومان)', keyboard: TextInputType.number),
                const Text('روش رسیدگی', style: TextStyle(fontWeight: FontWeight.w800)),
                RadioListTile<String>(
                  value: 'ADMIN_REVIEW',
                  groupValue: method,
                  onChanged: (value) => setState(() => method = value ?? method),
                  title: const Text('بررسی و تسویه توسط کارشناس'),
                  contentPadding: EdgeInsets.zero,
                ),
                RadioListTile<String>(
                  value: 'DIRECT_PAYMENT',
                  groupValue: method,
                  onChanged: (value) => setState(() => method = value ?? method),
                  title: const Text('پرداخت مستقیم (شبیه‌سازی)'),
                  contentPadding: EdgeInsets.zero,
                ),
                const SizedBox(height: 8),
                PrimaryButton(
                  label: 'ثبت استعلام',
                  icon: Icons.send_rounded,
                  onPressed: () {
                    Navigator.pop(
                      context,
                      <String, String>{
                        for (final entry in fields.entries) entry.key: entry.value.text.trim(),
                        'method': method,
                      },
                    );
                  },
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
