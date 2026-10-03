import 'package:flutter/material.dart';

import '../app_controller.dart';
import '../core/theme.dart';
import '../core/utils.dart';
import '../data/models.dart';
import '../widgets/glass.dart';

class ServicesScreen extends StatefulWidget {
  const ServicesScreen({super.key, required this.controller});

  final AppController controller;

  @override
  State<ServicesScreen> createState() => _ServicesScreenState();
}

class _ServicesScreenState extends State<ServicesScreen> {
  String filter = 'همه';

  AppController get controller => widget.controller;

  final filters = const [
    'همه',
    'بیمه',
    'استعلام و عوارض',
    'کارت سوخت',
    'در انتظار تایید',
    'تایید شده',
  ];

  List<ServiceRequest> get filtered {
    return controller.requests.where((request) {
      switch (filter) {
        case 'بیمه':
          return request.requestType.contains('بیمه');
        case 'استعلام و عوارض':
          return request.requestType.contains('استعلام') ||
              request.requestType.contains('عوارض') ||
              request.title.contains('مالیات');
        case 'کارت سوخت':
          return request.requestType.contains('کارت سوخت');
        case 'در انتظار تایید':
          return request.status.contains('در انتظار') || request.status.contains('بررسی');
        case 'تایید شده':
          return request.status.contains('تایید شد');
        default:
          return true;
      }
    }).toList();
  }

  @override
  Widget build(BuildContext context) {
    final items = filtered;
    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 14, 16, 30),
      children: [
        GradientHeader(
          title: 'درخواست خدمات',
          subtitle: 'خدمات بیمه، استعلام، عوارض و کارت سوخت را از همین‌جا ثبت و پیگیری کنید',
          trailing: Stack(
            clipBehavior: Clip.none,
            children: [
              const Icon(Icons.notifications_none_rounded, size: 26),
              if (controller.pendingRequests > 0)
                Positioned(
                  right: -2,
                  top: -2,
                  child: Container(
                    width: 9,
                    height: 9,
                    decoration: const BoxDecoration(color: AppTheme.red, shape: BoxShape.circle),
                  ),
                ),
            ],
          ),
        ),
        const SizedBox(height: 18),
        _hero(context),
        const SizedBox(height: 16),
        SectionTitle(title: 'خدمات سریع'),
        const SizedBox(height: 10),
        GridView.count(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          crossAxisCount: 2,
          childAspectRatio: 1.28,
          mainAxisSpacing: 10,
          crossAxisSpacing: 10,
          children: [
            _ServiceButton(
              title: 'بیمه شخص ثالث',
              subtitle: 'صدور یا تمدید بیمه',
              icon: Icons.shield_outlined,
              color: AppTheme.purple,
              onTap: () => _serviceForm(context, type: 'بیمه', insurance: true),
            ),
            _ServiceButton(
              title: 'استعلام خلافی',
              subtitle: 'راهبری و پرداخت',
              icon: Icons.receipt_long_rounded,
              color: AppTheme.red,
              onTap: () => _serviceForm(context, type: 'استعلام خلافی خودرو'),
            ),
            _ServiceButton(
              title: 'عوارض آزادراهی',
              subtitle: 'تسویه تردد',
              icon: Icons.alt_route_rounded,
              color: Colors.orange,
              onTap: () => _serviceForm(context, type: 'استعلام عوارض آزادراهی'),
            ),
            _ServiceButton(
              title: 'عوارض سالیانه',
              subtitle: 'شهرداری و نوسازی',
              icon: Icons.location_city_rounded,
              color: AppTheme.turquoise,
              onTap: () => _serviceForm(context, type: 'استعلام عوارض سالیانه خودرو'),
            ),
          ],
        ),
        const SizedBox(height: 10),
        _wide(
          context,
          title: 'درخواست کارت سوخت',
          subtitle: 'صدور کارت نو یا المثنی با اطلاعات مالک و نشانی',
          icon: Icons.local_gas_station_rounded,
          color: AppTheme.skyBlue,
          onTap: () => _serviceForm(context, type: 'کارت سوخت', fuelCard: true),
        ),
        const SizedBox(height: 20),
        SectionTitle(
          title: 'پیگیری درخواست‌های ثبت‌شده',
          action: Text('${items.length} مورد', style: Theme.of(context).textTheme.bodySmall),
        ),
        const SizedBox(height: 9),
        SizedBox(
          height: 45,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            itemCount: filters.length,
            separatorBuilder: (_, __) => const SizedBox(width: 7),
            itemBuilder: (context, index) {
              final item = filters[index];
              return ChoiceChip(
                selected: filter == item,
                onSelected: (_) => setState(() => filter = item),
                label: Text(item),
              );
            },
          ),
        ),
        const SizedBox(height: 12),
        if (items.isEmpty)
          GlassCard(
            child: Column(
              children: [
                Icon(
                  Icons.hourglass_empty_rounded,
                  size: 42,
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                ),
                const SizedBox(height: 10),
                const Text('موردی در این دسته‌بندی یافت نشد', style: TextStyle(fontWeight: FontWeight.w900)),
                const SizedBox(height: 5),
                Text(
                  'یکی از خدمات بالا را ثبت کنید تا پیگیری آن در همین صفحه نمایش داده شود.',
                  style: Theme.of(context).textTheme.bodySmall,
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          )
        else
          ...items.map((request) => _requestCard(context, request)),
        if (controller.statusMessage.isNotEmpty) ...[
          const SizedBox(height: 12),
          GlassCard(
            padding: const EdgeInsets.all(14),
            child: Row(
              children: [
                const Icon(Icons.cloud_done_rounded, color: AppTheme.turquoise),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(
                    controller.statusMessage,
                    style: const TextStyle(fontWeight: FontWeight.w700),
                  ),
                ),
              ],
            ),
          ),
        ],
      ],
    );
  }

  Widget _hero(BuildContext context) {
    return GlassCard(
      padding: const EdgeInsets.all(20),
      child: Container(
        padding: const EdgeInsets.all(18),
        decoration: BoxDecoration(
          gradient: LinearGradient(
            colors: [
              Theme.of(context).colorScheme.primary.withOpacity(.14),
              AppTheme.purple.withOpacity(.10),
            ],
          ),
          borderRadius: BorderRadius.circular(22),
        ),
        child: Row(
          children: [
            Container(
              width: 58,
              height: 58,
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.primary.withOpacity(.14),
                shape: BoxShape.circle,
              ),
              child: Icon(Icons.support_agent_rounded, color: Theme.of(context).colorScheme.primary, size: 30),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'همه درخواست‌ها از Railway عبور می‌کنند',
                    style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w900),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    'قبل از ارسال به ربات بله، رویداد درخواست در سرور ذخیره می‌شود و در صورت قطعی شبکه در صف محلی می‌ماند.',
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          height: 1.45,
                          color: Theme.of(context).colorScheme.onSurfaceVariant,
                        ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _wide(
    BuildContext context, {
    required String title,
    required String subtitle,
    required IconData icon,
    required Color color,
    required VoidCallback onTap,
  }) {
    return GlassCard(
      padding: EdgeInsets.zero,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(26),
        child: Padding(
          padding: const EdgeInsets.all(17),
          child: Row(
            children: [
              Container(
                width: 48,
                height: 48,
                decoration: BoxDecoration(
                  color: color.withOpacity(.12),
                  borderRadius: BorderRadius.circular(15),
                ),
                child: Icon(icon, color: color),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(title, style: const TextStyle(fontWeight: FontWeight.w900)),
                    const SizedBox(height: 4),
                    Text(subtitle, style: const TextStyle(fontSize: 11)),
                  ],
                ),
              ),
              const Icon(Icons.arrow_back_ios_new_rounded, size: 16),
            ],
          ),
        ),
      ),
    );
  }

  Widget _requestCard(BuildContext context, ServiceRequest request) {
    final pending = request.status.contains('در انتظار') || request.status.contains('بررسی');
    return GlassCard(
      margin: const EdgeInsets.only(bottom: 9),
      padding: const EdgeInsets.all(15),
      borderRadius: 21,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(request.title, style: const TextStyle(fontWeight: FontWeight.w900)),
              ),
              _statusChip(context, request.status),
            ],
          ),
          const SizedBox(height: 8),
          Text('${request.fullName} • ${request.phoneNumber}', style: Theme.of(context).textTheme.bodySmall),
          const SizedBox(height: 5),
          Text(
            '${request.vehiclePlate} • ${dateFa(request.submissionDateMillis)}',
            style: Theme.of(context).textTheme.bodySmall,
          ),
          const SizedBox(height: 10),
          Row(
            children: [
              Expanded(
                child: Text(
                  'کد پیگیری: ${request.baleMessageId.isEmpty ? '—' : request.baleMessageId}',
                  style: const TextStyle(fontSize: 10.5, fontWeight: FontWeight.w800),
                ),
              ),
              if (pending)
                IconButton(
                  onPressed: controller.busy ? null : () => controller.approveRequest(request),
                  icon: const Icon(Icons.check_circle_outline_rounded),
                  tooltip: 'تایید',
                ),
              IconButton(
                onPressed: controller.busy ? null : () => controller.checkRequest(request),
                icon: const Icon(Icons.refresh_rounded),
                tooltip: 'بررسی وضعیت',
              ),
              IconButton(
                onPressed: controller.busy ? null : () => controller.deleteRequest(request),
                icon: const Icon(Icons.delete_outline_rounded),
                tooltip: 'حذف',
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _statusChip(BuildContext context, String text) {
    final good = text.contains('تایید') || text.contains('انجام');
    final color = good
        ? AppTheme.turquoise
        : text.contains('رد')
            ? AppTheme.red
            : Colors.orange;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 6),
      decoration: BoxDecoration(
        color: color.withOpacity(.10),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text(
        text,
        style: TextStyle(fontSize: 9.5, color: color, fontWeight: FontWeight.w800),
      ),
    );
  }

  Future<void> _serviceForm(
    BuildContext context, {
    required String type,
    bool insurance = false,
    bool fuelCard = false,
  }) async {
    final result = await showModalBottomSheet<Map<String, String>>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => _ServiceFormSheet(
        type: type,
        controller: controller,
        insurance: insurance,
        fuelCard: fuelCard,
      ),
    );

    if (result == null) return;

    final request = ServiceRequest(
      requestType: type,
      title: type,
      fullName: result['name'] ?? '',
      nationalCode: result['national'] ?? '',
      phoneNumber: result['phone'] ?? '',
      vehiclePlate: result['plate'] ?? '',
      vinCode: result['vin'] ?? '',
      barcodeNumber: result['barcode'] ?? '',
      engineNumber: result['engine'] ?? '',
      chassisNumber: result['chassis'] ?? '',
      postalCode: result['postal'] ?? '',
      address: result['address'] ?? '',
      insuranceCategory: result['category'] ?? '',
      insuranceCompany: result['company'] ?? '',
      durationMonths: int.tryParse(result['duration'] ?? '12') ?? 12,
      discountPercent: int.tryParse(result['discount'] ?? '0') ?? 0,
      additionalDetails: result['details'] ?? '',
    );

    await controller.submitServiceRequest(request);
  }
}

class _ServiceButton extends StatelessWidget {
  const _ServiceButton({
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
      borderRadius: 23,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(23),
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                width: 42,
                height: 42,
                decoration: BoxDecoration(
                  color: color.withOpacity(.12),
                  borderRadius: BorderRadius.circular(13),
                ),
                child: Icon(icon, color: color),
              ),
              const Spacer(),
              Text(title, style: const TextStyle(fontWeight: FontWeight.w900)),
              const SizedBox(height: 3),
              Text(subtitle, style: const TextStyle(fontSize: 10.5, height: 1.2)),
            ],
          ),
        ),
      ),
    );
  }
}

class _ServiceFormSheet extends StatefulWidget {
  const _ServiceFormSheet({
    required this.type,
    required this.controller,
    this.insurance = false,
    this.fuelCard = false,
  });

  final String type;
  final AppController controller;
  final bool insurance;
  final bool fuelCard;

  @override
  State<_ServiceFormSheet> createState() => _ServiceFormSheetState();
}

class _ServiceFormSheetState extends State<_ServiceFormSheet> {
  final fields = <String, TextEditingController>{};
  final scrollController = ScrollController();
  String category = 'شخص ثالث';
  String company = 'بیمه ایران';
  String cardType = 'المثنی';

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
      'details',
      'duration',
      'discount',
    ]) {
      fields[key] = TextEditingController();
    }
    fields['duration']!.text = '12';
    fields['discount']!.text = '0';

    final vehicle = widget.controller.activeVehicle;
    if (vehicle != null) fields['plate']!.text = vehicle.formattedPlate;
  }

  @override
  void dispose() {
    for (final controller in fields.values) {
      controller.dispose();
    }
    scrollController.dispose();
    super.dispose();
  }

  Widget field(String key, String hint, {int maxLines = 1, TextInputType? keyboard}) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: TextField(
        controller: fields[key],
        keyboardType: keyboard,
        maxLines: maxLines,
        decoration: fieldDecoration(context, hint),
      ),
    );
  }

  Map<String, String> result() {
    return {
      for (final entry in fields.entries) entry.key: entry.value.text.trim(),
      'category': category,
      'company': company,
      'cardType': cardType,
    };
  }

  @override
  Widget build(BuildContext context) {
    return Directionality(
      textDirection: TextDirection.rtl,
      child: Container(
        padding: EdgeInsets.only(
          top: 14,
          left: 18,
          right: 18,
          bottom: 24 + MediaQuery.of(context).viewInsets.bottom,
        ),
        decoration: BoxDecoration(
          color: Theme.of(context).colorScheme.surface,
          borderRadius: const BorderRadius.vertical(top: Radius.circular(30)),
        ),
        child: SafeArea(
          top: false,
          child: SingleChildScrollView(
            controller: scrollController,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Center(
                  child: Container(
                    width: 42,
                    height: 4,
                    decoration: BoxDecoration(
                      color: Colors.grey.withOpacity(.3),
                      borderRadius: BorderRadius.circular(4),
                    ),
                  ),
                ),
                const SizedBox(height: 14),
                Text(
                  widget.type,
                  style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900),
                ),
                const SizedBox(height: 14),
                field('name', 'نام و نام خانوادگی'),
                field('national', 'کد ملی', keyboard: TextInputType.number),
                field('phone', 'شماره تماس', keyboard: TextInputType.phone),
                field('plate', 'پلاک خودرو'),
                field('vin', 'کد VIN'),
                field('barcode', 'بارکد کارت خودرو'),
                field('engine', 'شماره موتور'),
                field('chassis', 'شماره شاسی'),
                field('postal', 'کد پستی', keyboard: TextInputType.number),
                field('address', 'آدرس محل سکونت', maxLines: 3),
                if (widget.insurance) ...[
                  DropdownButtonFormField<String>(
                    initialValue: category,
                    decoration: fieldDecoration(context, 'دسته‌بندی بیمه'),
                    items: ['شخص ثالث', 'بدنه', 'حوادث راننده', 'موتورسیکلت']
                        .map((value) => DropdownMenuItem(value: value, child: Text(value)))
                        .toList(),
                    onChanged: (value) => setState(() => category = value ?? category),
                  ),
                  const SizedBox(height: 10),
                  DropdownButtonFormField<String>(
                    initialValue: company,
                    decoration: fieldDecoration(context, 'شرکت بیمه'),
                    items: ['بیمه ایران', 'آسیا', 'دانا', 'پارسیان', 'البرز', 'سایر']
                        .map((value) => DropdownMenuItem(value: value, child: Text(value)))
                        .toList(),
                    onChanged: (value) => setState(() => company = value ?? company),
                  ),
                  const SizedBox(height: 10),
                  Row(
                    children: [
                      Expanded(child: field('duration', 'مدت (ماه)', keyboard: TextInputType.number)),
                      const SizedBox(width: 10),
                      Expanded(child: field('discount', 'تخفیف عدم خسارت (%)', keyboard: TextInputType.number)),
                    ],
                  ),
                ],
                if (widget.fuelCard) ...[
                  DropdownButtonFormField<String>(
                    initialValue: cardType,
                    decoration: fieldDecoration(context, 'نوع کارت'),
                    items: ['نو', 'المثنی']
                        .map((value) => DropdownMenuItem(value: value, child: Text(value)))
                        .toList(),
                    onChanged: (value) => setState(() => cardType = value ?? cardType),
                  ),
                  const SizedBox(height: 10),
                ],
                field('details', 'توضیحات تکمیلی', maxLines: 4),
                const SizedBox(height: 6),
                PrimaryButton(
                  label: 'ثبت و ارسال درخواست',
                  icon: Icons.send_rounded,
                  onPressed: () => Navigator.pop(context, result()),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
