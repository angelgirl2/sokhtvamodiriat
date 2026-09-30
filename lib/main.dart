import 'dart:math' as math;
import 'dart:typed_data';

import 'package:animations/animations.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';
import 'package:intl/intl.dart';
import 'package:local_auth/local_auth.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'app_state.dart';
import 'core/app_colors.dart';
import 'core/app_theme.dart';
import 'core/persian_utils.dart';
import 'data/app_repository.dart';
import 'data/bale_bot_service.dart';
import 'data/models.dart';
import 'data/notification_helper.dart';
import 'data/railway_api_client.dart';
import 'data/railway_sync_service.dart';
import 'data/security_manager.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  Intl.defaultLocale = 'fa';
  final prefs = await SharedPreferences.getInstance();
  final securityManager = SecurityManager(prefs);
  final api = RailwayApiClient(baseUrls: securityManager.getRailwayUrls());
  final repository = AppRepository(
    baleBotService: BaleBotService(api: api),
    railwaySyncService: RailwaySyncService(api: api),
    securityManager: securityManager,
  );
  final notificationHelper = NotificationHelper();
  await notificationHelper.initialize();

  final appState = AppState(
    repository: repository,
    securityManager: securityManager,
    notificationHelper: notificationHelper,
  );
  await appState.load();

  runApp(ChangeNotifierProvider.value(value: appState, child: const FuelApp()));
}

class FuelApp extends StatelessWidget {
  const FuelApp({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<AppState>(
      builder: (context, appState, _) {
        final theme = buildAppTheme(
          appState.currentThemeColor,
          appState.darkModePref,
        );
        return MaterialApp(
          debugShowCheckedModeBanner: false,
          title: 'مدیریت سوخت و استعلام',
          locale: const Locale('fa'),
          supportedLocales: const [Locale('fa')],
          theme: theme.light,
          darkTheme: theme.dark,
          themeMode: theme.mode,
          builder: (context, child) {
            return Directionality(
              textDirection: TextDirection.rtl,
              child: MediaQuery(
                data: MediaQuery.of(
                  context,
                ).copyWith(textScaler: const TextScaler.linear(1.0)),
                child: child ?? const SizedBox.shrink(),
              ),
            );
          },
          home: const AppShell(),
        );
      },
    );
  }
}

enum AppDestination { splash, lock, main }

class AppShell extends StatefulWidget {
  const AppShell({super.key});

  @override
  State<AppShell> createState() => _AppShellState();
}

class _AppShellState extends State<AppShell> {
  AppDestination destination = AppDestination.splash;
  bool onboardingVisible = false;
  bool onboardingSeen = true;

  @override
  void initState() {
    super.initState();
    _bootstrap();
  }

  Future<void> _bootstrap() async {
    final security = context.read<AppState>().securityManager;
    onboardingSeen = security.isTutorialSeen();
    await Future<void>.delayed(const Duration(milliseconds: 2100));
    if (!mounted) return;
    setState(() {
      destination = security.isPinSet()
          ? AppDestination.lock
          : AppDestination.main;
      onboardingVisible = !onboardingSeen;
    });
  }

  Future<void> _completeOnboarding() async {
    await context.read<AppState>().securityManager.setTutorialSeen(true);
    if (!mounted) return;
    setState(() => onboardingVisible = false);
  }

  @override
  Widget build(BuildContext context) {
    final page = switch (destination) {
      AppDestination.splash => SplashScreen(onContinue: () {}),
      AppDestination.lock => LockScreen(
        onUnlocked: () {
          setState(() => destination = AppDestination.main);
        },
      ),
      AppDestination.main => MainScreen(
        onboardingVisible: onboardingVisible,
        onOnboardingDismissed: _completeOnboarding,
      ),
    };

    return PageTransitionSwitcher(
      duration: const Duration(milliseconds: 650),
      reverse: destination == AppDestination.lock,
      transitionBuilder: (child, animation, secondaryAnimation) {
        return SharedAxisTransition(
          animation: animation,
          secondaryAnimation: secondaryAnimation,
          transitionType: SharedAxisTransitionType.horizontal,
          fillColor: Theme.of(context).colorScheme.surface,
          child: child,
        );
      },
      child: KeyedSubtree(key: ValueKey(destination), child: page),
    );
  }
}

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key, required this.onContinue});

  final VoidCallback onContinue;

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1800),
    )..repeat(reverse: true);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return Scaffold(
      body: DecoratedBox(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topRight,
            end: Alignment.bottomLeft,
            colors: [colors.primary, colors.secondary, colors.tertiary],
          ),
        ),
        child: SafeArea(
          child: Center(
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  ScaleTransition(
                    scale: Tween<double>(begin: 0.94, end: 1.04).animate(
                      CurvedAnimation(
                        parent: _controller,
                        curve: Curves.easeInOut,
                      ),
                    ),
                    child: Hero(
                      tag: 'app-logo',
                      child: Container(
                        width: 132,
                        height: 132,
                        decoration: BoxDecoration(
                          color: Colors.white.withValues(alpha: .14),
                          borderRadius: BorderRadius.circular(34),
                          boxShadow: [
                            BoxShadow(
                              color: Colors.black.withValues(alpha: .15),
                              blurRadius: 30,
                              offset: const Offset(0, 12),
                            ),
                          ],
                        ),
                        child: Padding(
                          padding: const EdgeInsets.all(18),
                          child: Image.asset(
                            'assets/app_logo.png',
                            fit: BoxFit.contain,
                          ),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 28),
                  Text(
                    'مدیریت سوخت و استعلام',
                    style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                      color: Colors.white,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  const SizedBox(height: 12),
                  Text(
                    'سامانه جامع مدیریت هوشمند سوخت خودرو، استعلام و پرداخت خلافی و عوارض، خدمات بیمه و یادآور سرویس',
                    textAlign: TextAlign.center,
                    style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                      color: Colors.white.withValues(alpha: .9),
                      height: 1.9,
                    ),
                  ),
                  const SizedBox(height: 30),
                  ClipRRect(
                    borderRadius: BorderRadius.circular(999),
                    child: TweenAnimationBuilder<double>(
                      duration: const Duration(milliseconds: 2200),
                      tween: Tween(begin: 0, end: 1),
                      builder: (context, value, _) {
                        return LinearProgressIndicator(
                          value: value,
                          minHeight: 10,
                          backgroundColor: Colors.white.withValues(alpha: .18),
                          valueColor: const AlwaysStoppedAnimation<Color>(
                            Colors.white,
                          ),
                        );
                      },
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class LockScreen extends StatefulWidget {
  const LockScreen({super.key, required this.onUnlocked});

  final VoidCallback onUnlocked;

  @override
  State<LockScreen> createState() => _LockScreenState();
}

class _LockScreenState extends State<LockScreen> {
  final LocalAuthentication auth = LocalAuthentication();
  String pin = '';
  String? error;
  bool working = false;

  Future<void> _tryUnlock() async {
    final security = context.read<AppState>().securityManager;
    if (security.verifyPin(pin)) {
      widget.onUnlocked();
      return;
    }
    setState(() {
      error = 'رمز واردشده صحیح نیست.';
      pin = '';
    });
  }

  Future<void> _biometric() async {
    setState(() {
      working = true;
      error = null;
    });
    try {
      final ok = await auth.authenticate(
        localizedReason: 'برای ورود به سامانه هویت خود را تایید کنید',
        biometricOnly: true,
        authMessages: const [],
      );
      if (!mounted) return;
      if (ok) {
        widget.onUnlocked();
      } else {
        setState(() => error = 'احراز هویت بیومتریک انجام نشد.');
      }
    } catch (_) {
      if (!mounted) return;
      setState(() => error = 'این دستگاه از ورود بیومتریک پشتیبانی نمی‌کند.');
    } finally {
      if (mounted) setState(() => working = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return Scaffold(
      body: SafeArea(
        child: Center(
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 460),
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Hero(
                    tag: 'app-logo',
                    child: CircleAvatar(
                      radius: 52,
                      backgroundColor: colors.primaryContainer,
                      child: Image.asset('assets/app_logo.png', width: 56),
                    ),
                  ),
                  const SizedBox(height: 18),
                  Text(
                    'ورود ایمن',
                    style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'برای دسترسی به اطلاعات خودرو، رمز یا اثرانگشت را وارد کنید.',
                    textAlign: TextAlign.center,
                    style: Theme.of(
                      context,
                    ).textTheme.bodyMedium?.copyWith(height: 1.8),
                  ),
                  const SizedBox(height: 28),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: List.generate(
                      4,
                      (index) => AnimatedContainer(
                        duration: const Duration(milliseconds: 220),
                        margin: const EdgeInsets.symmetric(horizontal: 8),
                        width: 18,
                        height: 18,
                        decoration: BoxDecoration(
                          color: index < pin.length
                              ? colors.primary
                              : colors.surfaceContainerHighest,
                          shape: BoxShape.circle,
                          border: Border.all(
                            color: colors.primary.withValues(alpha: .35),
                          ),
                        ),
                      ),
                    ),
                  ),
                  if (error != null) ...[
                    const SizedBox(height: 12),
                    Text(error!, style: TextStyle(color: colors.error)),
                  ],
                  const SizedBox(height: 24),
                  Wrap(
                    spacing: 10,
                    runSpacing: 10,
                    children: [
                      for (final key in [
                        '1',
                        '2',
                        '3',
                        '4',
                        '5',
                        '6',
                        '7',
                        '8',
                        '9',
                      ])
                        _PinKey(
                          label: key,
                          onTap: () {
                            if (pin.length >= 4) return;
                            setState(() => pin += key);
                            if (pin.length == 4) {
                              _tryUnlock();
                            }
                          },
                        ),
                      _PinKey(
                        label: 'بیومتریک',
                        wide: true,
                        onTap: working ? null : _biometric,
                      ),
                      _PinKey(
                        label: '0',
                        onTap: () {
                          if (pin.length >= 4) return;
                          setState(() => pin += '0');
                          if (pin.length == 4) _tryUnlock();
                        },
                      ),
                      _PinKey(
                        label: 'حذف',
                        wide: true,
                        onTap: () => setState(
                          () => pin = pin.isEmpty
                              ? ''
                              : pin.substring(0, pin.length - 1),
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _PinKey extends StatelessWidget {
  const _PinKey({required this.label, required this.onTap, this.wide = false});

  final String label;
  final VoidCallback? onTap;
  final bool wide;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: wide ? 126 : 84,
      height: 64,
      child: FilledButton.tonal(
        onPressed: onTap,
        style: FilledButton.styleFrom(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(20),
          ),
        ),
        child: Text(label, style: const TextStyle(fontWeight: FontWeight.w700)),
      ),
    );
  }
}

enum MainTab { fuel, services, inquiry, reminders, settings }

class MainScreen extends StatefulWidget {
  const MainScreen({
    super.key,
    required this.onboardingVisible,
    required this.onOnboardingDismissed,
  });

  final bool onboardingVisible;
  final Future<void> Function() onOnboardingDismissed;

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  MainTab tab = MainTab.fuel;

  Future<bool> _handleBack() async {
    final shouldExit =
        await showDialog<bool>(
          context: context,
          builder: (context) => AlertDialog(
            title: const Text('خروج از برنامه'),
            content: const Text('آیا مایل هستید از سامانه خارج شوید؟'),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context, false),
                child: const Text('خیر'),
              ),
              FilledButton(
                onPressed: () => Navigator.pop(context, true),
                child: const Text('بله'),
              ),
            ],
          ),
        ) ??
        false;
    return shouldExit;
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (widget.onboardingVisible) {
      WidgetsBinding.instance.addPostFrameCallback((_) async {
        if (!mounted) return;
        await showDialog<void>(
          context: context,
          barrierDismissible: false,
          builder: (context) => AlertDialog(
            icon: const Icon(Icons.info_rounded, size: 36),
            title: const Text('راهنمای شروع سریع'),
            content: const Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _GuideItem(
                  index: 1,
                  text: 'افزودن خودرو و ثبت سوخت‌گیری برای پایش هزینه و مصرف',
                ),
                SizedBox(height: 10),
                _GuideItem(
                  index: 2,
                  text: 'ثبت درخواست خدمات بیمه، المثنی کارت یا اسناد خودرو',
                ),
                SizedBox(height: 10),
                _GuideItem(
                  index: 3,
                  text: 'استعلام و پرداخت خلافی، عوارض و تسویه مستقیم',
                ),
                SizedBox(height: 10),
                _GuideItem(
                  index: 4,
                  text: 'گزارش PDF/Excel، سرویس‌ها و یادآورهای نگهداری',
                ),
              ],
            ),
            actions: [
              FilledButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('شروع استفاده'),
              ),
            ],
          ),
        );
        await widget.onOnboardingDismissed();
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final pages = {
      MainTab.fuel: const FuelManagementScreen(),
      MainTab.services: const ServiceRequestScreen(),
      MainTab.inquiry: const InquiryAndPaymentScreen(),
      MainTab.reminders: const ServiceRemindersScreen(),
      MainTab.settings: const SettingsScreen(),
    };

    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, result) async {
        if (didPop) return;
        final exit = await _handleBack();
        if (exit && mounted) {
          SystemNavigator.pop();
        }
      },
      child: Scaffold(
        body: SafeArea(
          child: Column(
            children: [
              Expanded(
                child: PageTransitionSwitcher(
                  duration: const Duration(milliseconds: 450),
                  transitionBuilder: (child, animation, secondaryAnimation) {
                    return FadeThroughTransition(
                      animation: animation,
                      secondaryAnimation: secondaryAnimation,
                      child: child,
                    );
                  },
                  child: KeyedSubtree(key: ValueKey(tab), child: pages[tab]),
                ),
              ),
              ModernFloatingNavigationBar(
                current: tab,
                onChanged: (value) => setState(() => tab = value),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _GuideItem extends StatelessWidget {
  const _GuideItem({required this.index, required this.text});

  final int index;
  final String text;

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        CircleAvatar(radius: 14, child: Text('$index')),
        const SizedBox(width: 10),
        Expanded(child: Text(text, style: const TextStyle(height: 1.8))),
      ],
    );
  }
}

class ModernFloatingNavigationBar extends StatelessWidget {
  const ModernFloatingNavigationBar({
    super.key,
    required this.current,
    required this.onChanged,
  });

  final MainTab current;
  final ValueChanged<MainTab> onChanged;

  @override
  Widget build(BuildContext context) {
    final items = const [
      (MainTab.fuel, 'سوخت', Icons.local_gas_station_rounded),
      (MainTab.services, 'خدمات', Icons.grid_view_rounded),
      (MainTab.inquiry, 'استعلام', Icons.receipt_long_rounded),
      (MainTab.reminders, 'یادآورها', Icons.alarm_rounded),
      (MainTab.settings, 'تنظیمات', Icons.settings_rounded),
    ];
    final colors = Theme.of(context).colorScheme;
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
      child: Material(
        color: colors.surface,
        elevation: 16,
        borderRadius: BorderRadius.circular(28),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 10),
          child: Row(
            children: [
              for (final item in items)
                Expanded(
                  child: GestureDetector(
                    behavior: HitTestBehavior.opaque,
                    onTap: () => onChanged(item.$1),
                    child: AnimatedContainer(
                      duration: const Duration(milliseconds: 260),
                      padding: const EdgeInsets.symmetric(
                        vertical: 10,
                        horizontal: 8,
                      ),
                      decoration: BoxDecoration(
                        color: current == item.$1
                            ? colors.primaryContainer
                            : Colors.transparent,
                        borderRadius: BorderRadius.circular(20),
                      ),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          TweenAnimationBuilder<double>(
                            tween: Tween(
                              begin: 1,
                              end: current == item.$1 ? 1.12 : 1,
                            ),
                            duration: const Duration(milliseconds: 240),
                            builder: (context, value, child) =>
                                Transform.scale(scale: value, child: child),
                            child: Icon(
                              item.$3,
                              color: current == item.$1
                                  ? colors.primary
                                  : colors.onSurfaceVariant,
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            item.$2,
                            style: TextStyle(
                              fontSize: 12,
                              color: current == item.$1
                                  ? colors.primary
                                  : colors.onSurfaceVariant,
                              fontWeight: current == item.$1
                                  ? FontWeight.w800
                                  : FontWeight.w600,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}

class FuelManagementScreen extends StatelessWidget {
  const FuelManagementScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<AppState>(
      builder: (context, app, _) {
        final vehicle = app.activeVehicle;
        final vehicleLogs = vehicle == null
            ? app.fuelLogs
            : app.fuelLogs.where((e) => e.vehicleId == vehicle.id).toList();
        final vehicleReminders = vehicle == null
            ? app.reminders
            : app.reminders.where((e) => e.vehicleId == vehicle.id).toList();
        return CustomScrollView(
          slivers: [
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(20, 18, 20, 8),
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
                                'مدیریت سوخت خودرو',
                                style: Theme.of(context).textTheme.headlineSmall
                                    ?.copyWith(fontWeight: FontWeight.w900),
                              ),
                              const SizedBox(height: 6),
                              Text(
                                'ثبت سوخت‌گیری، هزینه‌ها، گزارش و پایش ماهانه',
                                style: Theme.of(context).textTheme.bodyMedium,
                              ),
                            ],
                          ),
                        ),
                        FilledButton.icon(
                          onPressed: () => showDialog(
                            context: context,
                            builder: (_) => const AddVehicleDialog(),
                          ),
                          icon: const Icon(Icons.add_rounded),
                          label: const Text('افزودن خودرو'),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    if (app.vehicles.isEmpty)
                      _EmptyCard(
                        title: 'هنوز خودرویی ثبت نشده است',
                        subtitle:
                            'برای شروع، اطلاعات خودرو و پلاک را ثبت کنید تا داشبورد سوخت و سرویس فعال شود.',
                        actionLabel: 'ثبت اولین خودرو',
                        onAction: () => showDialog(
                          context: context,
                          builder: (_) => const AddVehicleDialog(),
                        ),
                      )
                    else ...[
                      SizedBox(
                        height: 58,
                        child: ListView.separated(
                          scrollDirection: Axis.horizontal,
                          itemCount: app.vehicles.length,
                          separatorBuilder: (_, _) => const SizedBox(width: 10),
                          itemBuilder: (context, index) {
                            final item = app.vehicles[index];
                            final selected = app.selectedVehicleId == item.id;
                            return FilterChip(
                              selected: selected,
                              showCheckmark: false,
                              avatar: const Icon(
                                Icons.directions_car_rounded,
                                size: 20,
                              ),
                              label: Text(item.title),
                              onSelected: (_) => app.selectVehicle(item.id),
                            );
                          },
                        ),
                      ),
                      const SizedBox(height: 16),
                      if (vehicle != null)
                        VehicleOverviewCard(
                          vehicle: vehicle,
                          reminders: vehicleReminders,
                        ),
                      const SizedBox(height: 16),
                      Row(
                        children: [
                          Expanded(
                            child: StatCard(
                              title: 'کل هزینه سوخت',
                              value:
                                  '${PersianUtils.withPrice(app.totalCost)} تومان',
                              icon: Icons.payments_rounded,
                              color: AppColors.fuelGold,
                            ),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: StatCard(
                              title: 'کل لیتر ثبت‌شده',
                              value: PersianUtils.decimal(app.totalLiters),
                              icon: Icons.water_drop_rounded,
                              color: AppColors.skyBluePrimary,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      FuelConsumptionChart(logs: vehicleLogs),
                      const SizedBox(height: 16),
                      Row(
                        children: [
                          Expanded(
                            child: FilledButton.icon(
                              onPressed: vehicle == null
                                  ? null
                                  : () => showDialog(
                                      context: context,
                                      builder: (_) =>
                                          AddFuelLogDialog(vehicle: vehicle),
                                    ),
                              icon: const Icon(Icons.local_gas_station_rounded),
                              label: const Text('ثبت سوخت‌گیری'),
                            ),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: OutlinedButton.icon(
                              onPressed: vehicle == null
                                  ? null
                                  : () => showDialog(
                                      context: context,
                                      builder: (_) => QuickAddReminderDialog(
                                        vehicle: vehicle,
                                      ),
                                    ),
                              icon: const Icon(Icons.alarm_add_rounded),
                              label: const Text('یادآور سرویس'),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      Row(
                        children: [
                          Expanded(
                            child: _ExportButton(
                              title: 'خروجی PDF',
                              icon: Icons.picture_as_pdf_rounded,
                              onTap: () => app.exportPdf(),
                            ),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: _ExportButton(
                              title: 'اشتراک متنی',
                              icon: Icons.share_rounded,
                              onTap: () => app.shareReportText(),
                            ),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: _ExportButton(
                              title: 'کپی گزارش',
                              icon: Icons.copy_all_rounded,
                              onTap: () async {
                                await app.copyReportText();
                                if (context.mounted) {
                                  ScaffoldMessenger.of(context).showSnackBar(
                                    const SnackBar(
                                      content: Text(
                                        'گزارش در کلیپ‌بورد کپی شد.',
                                      ),
                                    ),
                                  );
                                }
                              },
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      OutlinedButton.icon(
                        onPressed: () => app.exportCsv(),
                        icon: const Icon(Icons.grid_on_rounded),
                        label: const Text('خروجی Excel / CSV'),
                      ),
                    ],
                  ],
                ),
              ),
            ),
            SliverPadding(
              padding: const EdgeInsets.fromLTRB(20, 8, 20, 120),
              sliver: vehicleLogs.isEmpty
                  ? SliverToBoxAdapter(
                      child: _EmptyCard(
                        title: 'سوخت‌گیری ثبت نشده است',
                        subtitle:
                            'با ثبت اولین سوخت‌گیری، نمودار و گزارش‌های مصرف برای شما فعال می‌شود.',
                        actionLabel: 'ثبت سوخت‌گیری',
                        onAction: vehicle == null
                            ? null
                            : () => showDialog(
                                context: context,
                                builder: (_) =>
                                    AddFuelLogDialog(vehicle: vehicle),
                              ),
                      ),
                    )
                  : SliverList.separated(
                      itemCount: vehicleLogs.length,
                      separatorBuilder: (_, _) => const SizedBox(height: 12),
                      itemBuilder: (context, index) =>
                          FuelLogItem(log: vehicleLogs[index]),
                    ),
            ),
          ],
        );
      },
    );
  }
}

class StatCard extends StatelessWidget {
  const StatCard({
    super.key,
    required this.title,
    required this.value,
    required this.icon,
    required this.color,
  });
  final String title;
  final String value;
  final IconData icon;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(24),
        color: color.withValues(alpha: .10),
        border: Border.all(color: color.withValues(alpha: .16)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, color: color),
          const SizedBox(height: 12),
          Text(title, style: Theme.of(context).textTheme.labelLarge),
          const SizedBox(height: 8),
          Text(
            value,
            style: Theme.of(
              context,
            ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900),
          ),
        ],
      ),
    );
  }
}

class _ExportButton extends StatelessWidget {
  const _ExportButton({
    required this.title,
    required this.icon,
    required this.onTap,
  });
  final String title;
  final IconData icon;
  final VoidCallback onTap;
  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(18),
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 14),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(18),
          color: Theme.of(
            context,
          ).colorScheme.surfaceContainerHighest.withValues(alpha: .55),
        ),
        child: Column(
          children: [
            Icon(icon),
            const SizedBox(height: 6),
            Text(
              title,
              textAlign: TextAlign.center,
              style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w700),
            ),
          ],
        ),
      ),
    );
  }
}

class VehicleOverviewCard extends StatelessWidget {
  const VehicleOverviewCard({
    super.key,
    required this.vehicle,
    required this.reminders,
  });
  final Vehicle vehicle;
  final List<ServiceReminder> reminders;
  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final due = reminders.where((e) => !e.isCompleted).length;
    return Hero(
      tag: 'vehicle-${vehicle.id ?? vehicle.title}',
      child: Material(
        color: Colors.transparent,
        child: Container(
          padding: const EdgeInsets.all(20),
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(28),
            gradient: LinearGradient(
              colors: [colors.primaryContainer, colors.secondaryContainer],
            ),
          ),
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
                          style: Theme.of(context).textTheme.titleLarge
                              ?.copyWith(fontWeight: FontWeight.w900),
                        ),
                        const SizedBox(height: 6),
                        Text(
                          'سوخت: ${vehicle.fuelType}  •  ظرفیت باک: ${PersianUtils.decimal(vehicle.tankCapacity)} لیتر',
                        ),
                      ],
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.all(10),
                    decoration: BoxDecoration(
                      color: Colors.white.withValues(alpha: .55),
                      borderRadius: BorderRadius.circular(18),
                    ),
                    child: const Icon(Icons.directions_car_filled_rounded),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              IranianPlateView(vehicle: vehicle),
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(
                    child: _MetricChip(
                      label: 'کارکرد',
                      value:
                          '${PersianUtils.withPrice(vehicle.currentOdometer)} km',
                    ),
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: _MetricChip(label: 'یادآور باز', value: '$due مورد'),
                  ),
                ],
              ),
              if (vehicle.insuranceExpiryMillis > 0 ||
                  vehicle.inspectionExpiryMillis > 0) ...[
                const SizedBox(height: 16),
                Wrap(
                  spacing: 10,
                  runSpacing: 10,
                  children: [
                    if (vehicle.insuranceExpiryMillis > 0)
                      Chip(
                        label: Text(
                          'بیمه تا ${PersianUtils.dateFromMillis(vehicle.insuranceExpiryMillis)}',
                        ),
                      ),
                    if (vehicle.inspectionExpiryMillis > 0)
                      Chip(
                        label: Text(
                          'معاینه فنی تا ${PersianUtils.dateFromMillis(vehicle.inspectionExpiryMillis)}',
                        ),
                      ),
                  ],
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

class _MetricChip extends StatelessWidget {
  const _MetricChip({required this.label, required this.value});
  final String label;
  final String value;
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: .55),
        borderRadius: BorderRadius.circular(18),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label),
          const SizedBox(height: 4),
          Text(value, style: const TextStyle(fontWeight: FontWeight.w900)),
        ],
      ),
    );
  }
}

class IranianPlateView extends StatelessWidget {
  const IranianPlateView({super.key, required this.vehicle});
  final Vehicle vehicle;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: Colors.black12),
      ),
      child: Row(
        children: [
          Expanded(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                Text(
                  vehicle.plateCityCode,
                  style: const TextStyle(
                    fontSize: 22,
                    fontWeight: FontWeight.w900,
                  ),
                ),
                Text(
                  vehicle.plateLast3,
                  style: const TextStyle(
                    fontSize: 22,
                    fontWeight: FontWeight.w900,
                  ),
                ),
                Text(
                  vehicle.plateLetter,
                  style: const TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.w900,
                  ),
                ),
                Text(
                  vehicle.plateFirst2,
                  style: const TextStyle(
                    fontSize: 22,
                    fontWeight: FontWeight.w900,
                  ),
                ),
              ],
            ),
          ),
          Container(
            width: 52,
            height: 42,
            decoration: BoxDecoration(
              color: Colors.blue.shade700,
              borderRadius: BorderRadius.circular(8),
            ),
            child: const Center(
              child: Text(
                'IR',
                style: TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class FuelConsumptionChart extends StatefulWidget {
  const FuelConsumptionChart({super.key, required this.logs});
  final List<FuelLog> logs;
  @override
  State<FuelConsumptionChart> createState() => _FuelConsumptionChartState();
}

class _FuelConsumptionChartState extends State<FuelConsumptionChart> {
  bool barMode = false;

  @override
  Widget build(BuildContext context) {
    final stats = <String, int>{};
    for (final log in widget.logs) {
      final date = DateTime.fromMillisecondsSinceEpoch(log.dateMillis);
      final key = '${date.month}/${date.year % 100}';
      stats[key] = (stats[key] ?? 0) + log.totalCost;
    }
    final entries = stats.entries.toList()
      ..sort((a, b) => a.key.compareTo(b.key));
    final colors = [
      AppColors.skyBluePrimary,
      AppColors.fuelGold,
      AppColors.emeraldGreen,
      AppColors.rosePink,
      AppColors.orangeAccent,
      AppColors.deepIndigo,
    ];

    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(28),
        color: Theme.of(context).colorScheme.surfaceContainerLowest,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  'نمودار مصرف سوخت و هزینه ماهانه',
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w900,
                  ),
                ),
              ),
              SegmentedButton<bool>(
                segments: const [
                  ButtonSegment(value: false, label: Text('دایره‌ای')),
                  ButtonSegment(value: true, label: Text('میله‌ای')),
                ],
                selected: {barMode},
                onSelectionChanged: (value) =>
                    setState(() => barMode = value.first),
              ),
            ],
          ),
          const SizedBox(height: 18),
          SizedBox(
            height: 220,
            child: entries.isEmpty
                ? const Center(
                    child: Text('داده‌ای برای نمایش نمودار ثبت نشده است.'),
                  )
                : AnimatedSwitcher(
                    duration: const Duration(milliseconds: 350),
                    child: barMode
                        ? CustomPaint(
                            key: const ValueKey('bar'),
                            painter: _BarChartPainter(entries, colors),
                            child: const SizedBox.expand(),
                          )
                        : CustomPaint(
                            key: const ValueKey('pie'),
                            painter: _DonutChartPainter(entries, colors),
                            child: const SizedBox.expand(),
                          ),
                  ),
          ),
          const SizedBox(height: 12),
          Wrap(
            spacing: 12,
            runSpacing: 10,
            children: [
              for (var i = 0; i < entries.length; i++)
                Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Container(
                      width: 12,
                      height: 12,
                      decoration: BoxDecoration(
                        color: colors[i % colors.length],
                        shape: BoxShape.circle,
                      ),
                    ),
                    const SizedBox(width: 6),
                    Text(
                      '${entries[i].key}: ${PersianUtils.withPrice(entries[i].value)} تومان',
                    ),
                  ],
                ),
            ],
          ),
        ],
      ),
    );
  }
}

class _DonutChartPainter extends CustomPainter {
  _DonutChartPainter(this.entries, this.colors);
  final List<MapEntry<String, int>> entries;
  final List<Color> colors;
  @override
  void paint(Canvas canvas, Size size) {
    final total = entries.fold<int>(0, (sum, e) => sum + e.value).toDouble();
    final rect = Rect.fromCircle(
      center: Offset(size.width / 2, size.height / 2),
      radius: math.min(size.width, size.height) * .32,
    );
    final bg = Paint()
      ..color = Colors.black12
      ..style = PaintingStyle.stroke
      ..strokeWidth = 26;
    canvas.drawArc(rect, 0, math.pi * 2, false, bg);
    var start = -math.pi / 2;
    for (var i = 0; i < entries.length; i++) {
      final sweep = (entries[i].value / total) * math.pi * 2;
      final paint = Paint()
        ..style = PaintingStyle.stroke
        ..strokeWidth = 26
        ..strokeCap = StrokeCap.round
        ..color = colors[i % colors.length];
      canvas.drawArc(rect, start, sweep, false, paint);
      start += sweep;
    }
  }

  @override
  bool shouldRepaint(covariant _DonutChartPainter oldDelegate) =>
      oldDelegate.entries != entries;
}

class _BarChartPainter extends CustomPainter {
  _BarChartPainter(this.entries, this.colors);
  final List<MapEntry<String, int>> entries;
  final List<Color> colors;
  @override
  void paint(Canvas canvas, Size size) {
    final maxValue = entries
        .map((e) => e.value)
        .fold<int>(0, math.max)
        .toDouble();
    final paint = Paint()..style = PaintingStyle.fill;
    final spacing = 12.0;
    final usable = size.width - (entries.length + 1) * spacing;
    final width = usable / entries.length;
    for (var i = 0; i < entries.length; i++) {
      final h = (entries[i].value / maxValue) * (size.height - 40);
      paint.color = colors[i % colors.length];
      final left = spacing + i * (width + spacing);
      final rect = RRect.fromRectAndRadius(
        Rect.fromLTWH(left, size.height - h - 24, width, h),
        const Radius.circular(14),
      );
      canvas.drawRRect(rect, paint);
      final tp = TextPainter(
        text: TextSpan(
          text: entries[i].key,
          style: const TextStyle(color: Colors.black54, fontSize: 11),
        ),
        textDirection: TextDirection.rtl,
      )..layout(maxWidth: width + 10);
      tp.paint(canvas, Offset(left - 2, size.height - 20));
    }
  }

  @override
  bool shouldRepaint(covariant _BarChartPainter oldDelegate) =>
      oldDelegate.entries != entries;
}

class FuelLogItem extends StatelessWidget {
  const FuelLogItem({super.key, required this.log});
  final FuelLog log;
  @override
  Widget build(BuildContext context) {
    final app = context.read<AppState>();
    final vehicle = app.vehicles
        .where((v) => v.id == log.vehicleId)
        .cast<Vehicle?>()
        .firstWhere((v) => v != null, orElse: () => null);
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceContainerLowest,
        borderRadius: BorderRadius.circular(22),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              color: Theme.of(context).colorScheme.primaryContainer,
              borderRadius: BorderRadius.circular(16),
            ),
            child: const Icon(Icons.local_gas_station_rounded),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  vehicle?.title ?? 'خودرو',
                  style: const TextStyle(fontWeight: FontWeight.w900),
                ),
                const SizedBox(height: 6),
                Text(
                  'تاریخ: ${PersianUtils.dateFromMillis(log.dateMillis)}  •  ${PersianUtils.decimal(log.liters)} لیتر',
                ),
                const SizedBox(height: 6),
                Text(
                  'هزینه: ${PersianUtils.withPrice(log.totalCost)} تومان  •  کارکرد: ${PersianUtils.withPrice(log.odometer)} km',
                ),
                if (log.stationName.isNotEmpty) ...[
                  const SizedBox(height: 6),
                  Text('جایگاه: ${log.stationName}'),
                ],
                if (log.notes.isNotEmpty) ...[
                  const SizedBox(height: 6),
                  Text('یادداشت: ${log.notes}'),
                ],
              ],
            ),
          ),
          IconButton(
            onPressed: () => showDialog(
              context: context,
              builder: (_) => AlertDialog(
                title: const Text('حذف سوخت‌گیری'),
                content: const Text('این مورد حذف شود؟'),
                actions: [
                  TextButton(
                    onPressed: () => Navigator.pop(context),
                    child: const Text('انصراف'),
                  ),
                  FilledButton(
                    onPressed: () async {
                      Navigator.pop(context);
                      await context.read<AppState>().deleteFuelLog(log);
                    },
                    child: const Text('حذف'),
                  ),
                ],
              ),
            ),
            icon: const Icon(Icons.delete_outline_rounded),
          ),
        ],
      ),
    );
  }
}

class ServiceRequestScreen extends StatelessWidget {
  const ServiceRequestScreen({super.key});

  static const services = [
    (
      'درخواست بیمه‌نامه',
      Icons.shield_rounded,
      'ثبت بیمه شخص ثالث و بدنه با تخفیف',
    ),
    (
      'المثنی کارت خودرو',
      Icons.credit_card_rounded,
      'ثبت درخواست صدور مجدد کارت خودرو',
    ),
    (
      'تعویض پلاک',
      Icons.pin_rounded,
      'پیگیری پرونده نقل و انتقال و تعویض پلاک',
    ),
    ('برگ سبز و سند', Icons.description_rounded, 'درخواست صدور اسناد خودرو'),
    (
      'تمدید معاینه فنی',
      Icons.fact_check_rounded,
      'ثبت هماهنگی و درخواست تمدید',
    ),
    (
      'مشاوره خسارت',
      Icons.support_agent_rounded,
      'راهنمایی در پرونده خسارت و بیمه',
    ),
  ];

  @override
  Widget build(BuildContext context) {
    final requests = context.watch<AppState>().serviceRequests;
    return CustomScrollView(
      slivers: [
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 20, 20, 8),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'درخواست خدمات',
                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                    fontWeight: FontWeight.w900,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  'همه گزینه‌ها با ابعاد یکسان و دسترسی سریع برای ثبت درخواست',
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
                const SizedBox(height: 16),
                Container(
                  padding: const EdgeInsets.all(18),
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      colors: [
                        Theme.of(context).colorScheme.secondaryContainer,
                        Theme.of(context).colorScheme.primaryContainer,
                      ],
                    ),
                    borderRadius: BorderRadius.circular(28),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'بازوی بله و ثبت متمرکز درخواست‌ها',
                        style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.w900,
                        ),
                      ),
                      const SizedBox(height: 8),
                      const Text(
                        'ثبت همه اطلاعات لازم برای خدمات خودرو، بیمه و مدارک در یک فرم یکپارچه و روان.',
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
        SliverPadding(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
          sliver: SliverGrid(
            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 2,
              mainAxisSpacing: 12,
              crossAxisSpacing: 12,
              childAspectRatio: 1.15,
            ),
            delegate: SliverChildBuilderDelegate((context, index) {
              final service = services[index];
              return _ServiceTile(
                title: service.$1,
                icon: service.$2,
                subtitle: service.$3,
                onTap: () => showDialog(
                  context: context,
                  builder: (_) =>
                      ServiceRequestDialog(serviceTitle: service.$1),
                ),
              );
            }, childCount: services.length),
          ),
        ),
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 16, 20, 10),
            child: Text(
              'سوابق درخواست‌ها',
              style: Theme.of(
                context,
              ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900),
            ),
          ),
        ),
        SliverPadding(
          padding: const EdgeInsets.fromLTRB(20, 0, 20, 120),
          sliver: requests.isEmpty
              ? SliverToBoxAdapter(
                  child: _EmptyCard(
                    title: 'درخواستی ثبت نشده است',
                    subtitle:
                        'از گزینه‌های بالا برای ثبت خدمت جدید استفاده کنید.',
                  ),
                )
              : SliverList.separated(
                  itemCount: requests.length,
                  separatorBuilder: (_, _) => const SizedBox(height: 12),
                  itemBuilder: (context, index) =>
                      ServiceRequestCard(request: requests[index]),
                ),
        ),
      ],
    );
  }
}

class _ServiceTile extends StatelessWidget {
  const _ServiceTile({
    required this.title,
    required this.icon,
    required this.subtitle,
    required this.onTap,
  });
  final String title;
  final IconData icon;
  final String subtitle;
  final VoidCallback onTap;
  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(26),
      child: Ink(
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(26),
          color: colors.surfaceContainerLowest,
          border: Border.all(
            color: colors.outlineVariant.withValues(alpha: .55),
          ),
          boxShadow: [
            BoxShadow(
              color: colors.shadow.withValues(alpha: .06),
              blurRadius: 16,
              offset: const Offset(0, 6),
            ),
          ],
        ),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                width: 52,
                height: 52,
                decoration: BoxDecoration(
                  color: colors.primaryContainer,
                  borderRadius: BorderRadius.circular(18),
                ),
                child: Icon(icon, color: colors.primary),
              ),
              const Spacer(),
              Text(
                title,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: Theme.of(
                  context,
                ).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w900),
              ),
              const SizedBox(height: 8),
              Text(
                subtitle,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: Theme.of(
                  context,
                ).textTheme.bodySmall?.copyWith(height: 1.7),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class ServiceRequestCard extends StatelessWidget {
  const ServiceRequestCard({super.key, required this.request});
  final ServiceRequest request;
  @override
  Widget build(BuildContext context) {
    final app = context.read<AppState>();
    final approved = request.status == ServiceRequest.statusApproved;
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceContainerLowest,
        borderRadius: BorderRadius.circular(22),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  request.title,
                  style: const TextStyle(fontWeight: FontWeight.w900),
                ),
              ),
              Chip(
                label: Text(request.status),
                backgroundColor: approved
                    ? Colors.green.withValues(alpha: .14)
                    : Colors.amber.withValues(alpha: .18),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text('نام: ${request.fullName}'),
          const SizedBox(height: 6),
          Text('شماره تماس: ${request.phoneNumber}'),
          const SizedBox(height: 6),
          Text('پلاک: ${request.vehiclePlate}'),
          if (request.details.isNotEmpty) ...[
            const SizedBox(height: 6),
            Text('توضیحات: ${request.details}'),
          ],
          const SizedBox(height: 12),
          Row(
            children: [
              if (!approved)
                FilledButton.tonalIcon(
                  onPressed: () => app.approveRequest(request.id),
                  icon: const Icon(Icons.verified_rounded),
                  label: const Text('تایید'),
                ),
              const Spacer(),
              TextButton.icon(
                onPressed: () => app.deleteRequest(request),
                icon: const Icon(Icons.delete_outline_rounded),
                label: const Text('حذف'),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class InquiryAndPaymentScreen extends StatefulWidget {
  const InquiryAndPaymentScreen({super.key});
  @override
  State<InquiryAndPaymentScreen> createState() =>
      _InquiryAndPaymentScreenState();
}

class _InquiryAndPaymentScreenState extends State<InquiryAndPaymentScreen> {
  String selectedFilter = 'همه';
  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final options = ['همه', 'خلافی', 'عوارض', 'پرداخت شد', 'در انتظار'];
    final items = app.inquiries.where((item) {
      if (selectedFilter == 'همه') return true;
      if (selectedFilter == 'پرداخت شد') return item.status.contains('پرداخت');
      if (selectedFilter == 'در انتظار')
        return item.status.contains('در انتظار');
      return item.inquiryType.contains(selectedFilter) ||
          item.title.contains(selectedFilter);
    }).toList();

    return CustomScrollView(
      slivers: [
        SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(20, 20, 20, 8),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  padding: const EdgeInsets.all(20),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(28),
                    gradient: LinearGradient(
                      colors: [
                        Theme.of(context).colorScheme.tertiaryContainer,
                        Theme.of(context).colorScheme.primaryContainer,
                      ],
                    ),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'استعلام و پرداخت',
                        style: Theme.of(context).textTheme.headlineSmall
                            ?.copyWith(fontWeight: FontWeight.w900),
                      ),
                      const SizedBox(height: 8),
                      const Text(
                        'استعلام خلافی خودرو، عوارض، تسویه مستقیم و رهگیری تراکنش‌ها در یک بخش واحد.',
                      ),
                      const SizedBox(height: 16),
                      FilledButton.icon(
                        onPressed: () => showDialog(
                          context: context,
                          builder: (_) => const InquiryFormDialog(),
                        ),
                        icon: const Icon(Icons.add_rounded),
                        label: const Text('استعلام جدید'),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
                SizedBox(
                  height: 44,
                  child: ListView.separated(
                    scrollDirection: Axis.horizontal,
                    itemCount: options.length,
                    separatorBuilder: (_, _) => const SizedBox(width: 10),
                    itemBuilder: (context, index) => ChoiceChip(
                      label: Text(options[index]),
                      selected: selectedFilter == options[index],
                      onSelected: (_) =>
                          setState(() => selectedFilter = options[index]),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
        SliverPadding(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 120),
          sliver: items.isEmpty
              ? SliverToBoxAdapter(
                  child: _EmptyCard(
                    title: 'استعلامی ثبت نشده است',
                    subtitle:
                        'از دکمه بالا برای ثبت خلافی یا عوارض استفاده کنید.',
                  ),
                )
              : SliverList.separated(
                  itemCount: items.length,
                  separatorBuilder: (_, _) => const SizedBox(height: 12),
                  itemBuilder: (context, index) =>
                      InquiryCardItem(record: items[index]),
                ),
        ),
      ],
    );
  }
}

class InquiryCardItem extends StatelessWidget {
  const InquiryCardItem({super.key, required this.record});
  final InquiryRecord record;
  @override
  Widget build(BuildContext context) {
    final app = context.read<AppState>();
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceContainerLowest,
        borderRadius: BorderRadius.circular(22),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  record.title,
                  style: const TextStyle(fontWeight: FontWeight.w900),
                ),
              ),
              Chip(label: Text(record.status)),
            ],
          ),
          const SizedBox(height: 8),
          Text('نوع: ${record.inquiryType}'),
          const SizedBox(height: 6),
          Text('مبلغ: ${PersianUtils.withPrice(record.amount)} تومان'),
          const SizedBox(height: 6),
          Text('پلاک/شناسه: ${record.plateNumber}'),
          if (record.transactionRef.isNotEmpty) ...[
            const SizedBox(height: 6),
            Text('شماره پیگیری: ${record.transactionRef}'),
          ],
          const SizedBox(height: 12),
          Wrap(
            spacing: 10,
            runSpacing: 10,
            children: [
              if (!record.status.contains('تایید مدیر'))
                FilledButton.tonalIcon(
                  onPressed: () => app.approveInquiry(record.id),
                  icon: const Icon(Icons.verified_user_rounded),
                  label: const Text('تایید مدیر'),
                ),
              TextButton.icon(
                onPressed: () => app.deleteInquiry(record),
                icon: const Icon(Icons.delete_outline_rounded),
                label: const Text('حذف'),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class ServiceRemindersScreen extends StatefulWidget {
  const ServiceRemindersScreen({super.key});
  @override
  State<ServiceRemindersScreen> createState() => _ServiceRemindersScreenState();
}

class _ServiceRemindersScreenState extends State<ServiceRemindersScreen>
    with SingleTickerProviderStateMixin {
  late TabController controller;
  @override
  void initState() {
    super.initState();
    controller = TabController(length: 3, vsync: this);
  }

  @override
  void dispose() {
    controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final vehicle = app.activeVehicle;
    final history = vehicle == null
        ? app.serviceHistory
        : app.serviceHistory.where((e) => e.vehicleId == vehicle.id).toList();
    final reminders = vehicle == null
        ? app.reminders
        : app.reminders.where((e) => e.vehicleId == vehicle.id).toList();
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 20, 20, 0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'سرویس‌ها و یادآورها',
            style: Theme.of(
              context,
            ).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w900),
          ),
          const SizedBox(height: 8),
          Text(
            'تاریخچه سرویس، یادآورهای آینده و محاسبه‌گر فنی خودرو',
            style: Theme.of(context).textTheme.bodyMedium,
          ),
          const SizedBox(height: 16),
          TabBar(
            controller: controller,
            tabs: const [
              Tab(text: 'تاریخچه'),
              Tab(text: 'یادآورها'),
              Tab(text: 'محاسبه‌گر'),
            ],
          ),
          const SizedBox(height: 12),
          Expanded(
            child: TabBarView(
              controller: controller,
              children: [
                history.isEmpty
                    ? _EmptyCard(
                        title: 'سابقه‌ای ثبت نشده است',
                        subtitle: 'هزینه‌ها و سرویس‌های انجام شده را ثبت کنید.',
                        actionLabel: 'ثبت سرویس',
                        onAction: vehicle == null
                            ? null
                            : () => showDialog(
                                context: context,
                                builder: (_) =>
                                    AddServiceHistoryDialog(vehicle: vehicle),
                              ),
                      )
                    : ListView.separated(
                        padding: const EdgeInsets.only(bottom: 120),
                        itemCount: history.length,
                        separatorBuilder: (_, _) => const SizedBox(height: 12),
                        itemBuilder: (context, index) =>
                            ServiceHistoryItem(history: history[index]),
                      ),
                reminders.isEmpty
                    ? _EmptyCard(
                        title: 'یادآوری فعال نیست',
                        subtitle:
                            'سرویس بعدی، تعویض روغن یا معاینه فنی را ثبت کنید.',
                        actionLabel: 'یادآور جدید',
                        onAction: vehicle == null
                            ? null
                            : () => showDialog(
                                context: context,
                                builder: (_) =>
                                    QuickAddReminderDialog(vehicle: vehicle),
                              ),
                      )
                    : ListView.separated(
                        padding: const EdgeInsets.only(bottom: 120),
                        itemCount: reminders.length,
                        separatorBuilder: (_, _) => const SizedBox(height: 12),
                        itemBuilder: (context, index) =>
                            ReminderItem(reminder: reminders[index]),
                      ),
                ServiceCalculator(vehicle: vehicle),
              ],
            ),
          ),
          const SizedBox(height: 10),
          if (vehicle != null)
            Padding(
              padding: const EdgeInsets.only(bottom: 18),
              child: Row(
                children: [
                  Expanded(
                    child: FilledButton.icon(
                      onPressed: () => showDialog(
                        context: context,
                        builder: (_) =>
                            AddServiceHistoryDialog(vehicle: vehicle),
                      ),
                      icon: const Icon(Icons.build_rounded),
                      label: const Text('ثبت سرویس'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: () => showDialog(
                        context: context,
                        builder: (_) =>
                            QuickAddReminderDialog(vehicle: vehicle),
                      ),
                      icon: const Icon(Icons.alarm_add_rounded),
                      label: const Text('یادآور جدید'),
                    ),
                  ),
                ],
              ),
            ),
        ],
      ),
    );
  }
}

class ServiceHistoryItem extends StatelessWidget {
  const ServiceHistoryItem({super.key, required this.history});
  final ServiceHistory history;
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceContainerLowest,
        borderRadius: BorderRadius.circular(22),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  history.serviceType,
                  style: const TextStyle(fontWeight: FontWeight.w900),
                ),
              ),
              Text(PersianUtils.dateFromMillis(history.dateMillis)),
            ],
          ),
          const SizedBox(height: 8),
          Text('اقلام: ${history.itemsChanged}'),
          const SizedBox(height: 6),
          Text(
            'کارکرد: ${PersianUtils.withPrice(history.odometer)} km  •  هزینه: ${PersianUtils.withPrice(history.cost)} تومان',
          ),
          const SizedBox(height: 6),
          Text(
            'نوبت بعد: ${PersianUtils.withPrice(history.nextDueOdometer)} km',
          ),
          if (history.mechanicOrShop.isNotEmpty) ...[
            const SizedBox(height: 6),
            Text('مرکز سرویس: ${history.mechanicOrShop}'),
          ],
        ],
      ),
    );
  }
}

class ReminderItem extends StatelessWidget {
  const ReminderItem({super.key, required this.reminder});
  final ServiceReminder reminder;
  @override
  Widget build(BuildContext context) {
    final app = context.read<AppState>();
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceContainerLowest,
        borderRadius: BorderRadius.circular(22),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  reminder.serviceType,
                  style: const TextStyle(fontWeight: FontWeight.w900),
                ),
              ),
              Checkbox(
                value: reminder.isCompleted,
                onChanged: (_) => app.toggleReminderCompleted(reminder),
              ),
            ],
          ),
          Text(
            'تاریخ هدف: ${PersianUtils.dateFromMillis(reminder.targetDateMillis)}',
          ),
          const SizedBox(height: 6),
          Text(
            'کارکرد هدف: ${PersianUtils.withPrice(reminder.targetOdometer)} km',
          ),
          if (reminder.notes.isNotEmpty) ...[
            const SizedBox(height: 6),
            Text(reminder.notes),
          ],
          TextButton.icon(
            onPressed: () => app.deleteReminder(reminder),
            icon: const Icon(Icons.delete_outline_rounded),
            label: const Text('حذف'),
          ),
        ],
      ),
    );
  }
}

class ServiceCalculator extends StatelessWidget {
  const ServiceCalculator({super.key, required this.vehicle});
  final Vehicle? vehicle;
  @override
  Widget build(BuildContext context) {
    if (vehicle == null)
      return const Center(child: Text('ابتدا یک خودرو انتخاب کنید.'));
    final oilChangeAt = vehicle!.currentOdometer + 5000;
    final tireAt = vehicle!.currentOdometer + 10000;
    return ListView(
      padding: const EdgeInsets.only(bottom: 120),
      children: [
        _CalcCard(
          title: 'پیشنهاد تعویض روغن',
          value: '${PersianUtils.withPrice(oilChangeAt)} km',
          subtitle: 'بر اساس الگوی رایج نگهداری',
        ),
        const SizedBox(height: 12),
        _CalcCard(
          title: 'پیشنهاد بررسی لاستیک و جلوبندی',
          value: '${PersianUtils.withPrice(tireAt)} km',
          subtitle: 'برای حفظ نرمی حرکت و ایمنی',
        ),
        const SizedBox(height: 12),
        _CalcCard(
          title: 'کارکرد فعلی خودرو',
          value: '${PersianUtils.withPrice(vehicle!.currentOdometer)} km',
          subtitle: vehicle!.formattedPlate,
        ),
      ],
    );
  }
}

class _CalcCard extends StatelessWidget {
  const _CalcCard({
    required this.title,
    required this.value,
    required this.subtitle,
  });
  final String title;
  final String value;
  final String subtitle;
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceContainerLowest,
        borderRadius: BorderRadius.circular(22),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: const TextStyle(fontWeight: FontWeight.w900)),
          const SizedBox(height: 8),
          Text(
            value,
            style: Theme.of(
              context,
            ).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w900),
          ),
          const SizedBox(height: 6),
          Text(subtitle),
        ],
      ),
    );
  }
}

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final app = context.watch<AppState>();
    final security = app.securityManager;
    final vehicle = app.activeVehicle;
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 20, 20, 120),
      children: [
        Text(
          'تنظیمات',
          style: Theme.of(
            context,
          ).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w900),
        ),
        const SizedBox(height: 8),
        Text(
          'ظاهر، امنیت، همگام‌سازی، بیمه، معاینه فنی و حمایت از توسعه',
          style: Theme.of(context).textTheme.bodyMedium,
        ),
        const SizedBox(height: 16),
        _SettingsSection(
          title: 'تم و ظاهر',
          child: Column(
            children: [
              DropdownButtonFormField<AppThemeColor>(
                initialValue: app.currentThemeColor,
                decoration: const InputDecoration(labelText: 'رنگ اصلی برنامه'),
                items: AppThemeColor.values
                    .map(
                      (e) => DropdownMenuItem(value: e, child: Text(e.label)),
                    )
                    .toList(),
                onChanged: (value) {
                  if (value != null) app.setThemeColor(value);
                },
              ),
              const SizedBox(height: 12),
              DropdownButtonFormField<DarkModePref>(
                initialValue: app.darkModePref,
                decoration: const InputDecoration(labelText: 'حالت نمایش'),
                items: DarkModePref.values
                    .map(
                      (e) => DropdownMenuItem(value: e, child: Text(e.label)),
                    )
                    .toList(),
                onChanged: (value) {
                  if (value != null) app.setDarkModePref(value);
                },
              ),
            ],
          ),
        ),
        const SizedBox(height: 14),
        _SettingsSection(
          title: 'امنیت و بیومتریک',
          child: Column(
            children: [
              ListTile(
                contentPadding: EdgeInsets.zero,
                title: const Text('رمز ورود چهاررقمی'),
                subtitle: Text(security.isPinSet() ? 'فعال است' : 'تنظیم نشده'),
                trailing: FilledButton.tonal(
                  onPressed: () => showDialog(
                    context: context,
                    builder: (_) => const SetPinDialog(),
                  ),
                  child: Text(security.isPinSet() ? 'تغییر' : 'تنظیم'),
                ),
              ),
              SwitchListTile(
                contentPadding: EdgeInsets.zero,
                title: const Text('ورود بیومتریک'),
                value: security.isBiometricEnabled(),
                onChanged: (value) => security.setBiometricEnabled(value),
              ),
            ],
          ),
        ),
        const SizedBox(height: 14),
        _SettingsSection(
          title: 'معاینه فنی و بیمه',
          child: vehicle == null
              ? const Text(
                  'برای تنظیم این بخش، ابتدا یک خودرو در داشبورد سوخت انتخاب کنید.',
                )
              : Column(
                  children: [
                    ListTile(
                      contentPadding: EdgeInsets.zero,
                      leading: const Icon(Icons.fact_check_rounded),
                      title: const Text('تنظیم معاینه فنی'),
                      subtitle: Text(
                        vehicle.inspectionExpiryMillis > 0
                            ? PersianUtils.dateFromMillis(
                                vehicle.inspectionExpiryMillis,
                              )
                            : 'ثبت نشده',
                      ),
                      onTap: () => showDialog(
                        context: context,
                        builder: (_) => InspectionDialog(vehicle: vehicle),
                      ),
                    ),
                    const Divider(),
                    ListTile(
                      contentPadding: EdgeInsets.zero,
                      leading: const Icon(Icons.shield_rounded),
                      title: const Text('ثبت اطلاعات بیمه'),
                      subtitle: Text(
                        vehicle.insuranceExpiryMillis > 0
                            ? PersianUtils.dateFromMillis(
                                vehicle.insuranceExpiryMillis,
                              )
                            : 'ثبت نشده',
                      ),
                      onTap: () => showDialog(
                        context: context,
                        builder: (_) => InsuranceDialog(vehicle: vehicle),
                      ),
                    ),
                  ],
                ),
        ),
        const SizedBox(height: 14),
        _SettingsSection(
          title: 'پشتیبانی و همگام‌سازی',
          child: Column(
            children: [
              ListTile(
                contentPadding: EdgeInsets.zero,
                leading: const Icon(Icons.cloud_sync_rounded),
                title: const Text('همگام‌سازی Railway'),
                subtitle: Text(
                  app.syncStatus ??
                      'برای ارسال داده‌های فعلی به بک‌اند ضربه بزنید',
                ),
                onTap: () => app.syncWithRailway(),
              ),
              const Divider(),
              ListTile(
                contentPadding: EdgeInsets.zero,
                leading: const Icon(Icons.favorite_rounded),
                title: const Text('حمایت مالی / ارسال فیش'),
                subtitle: const Text(
                  'ارسال تصویر فیش و جزئیات پرداخت به ربات بله',
                ),
                onTap: () => showDialog(
                  context: context,
                  builder: (_) => const DonationDialog(),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class _SettingsSection extends StatelessWidget {
  const _SettingsSection({required this.title, required this.child});
  final String title;
  final Widget child;
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceContainerLowest,
        borderRadius: BorderRadius.circular(24),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: Theme.of(
              context,
            ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900),
          ),
          const SizedBox(height: 12),
          child,
        ],
      ),
    );
  }
}

class _EmptyCard extends StatelessWidget {
  const _EmptyCard({
    required this.title,
    required this.subtitle,
    this.actionLabel,
    this.onAction,
  });
  final String title;
  final String subtitle;
  final String? actionLabel;
  final VoidCallback? onAction;
  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surfaceContainerLowest,
        borderRadius: BorderRadius.circular(28),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: Theme.of(
              context,
            ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900),
          ),
          const SizedBox(height: 8),
          Text(subtitle, style: const TextStyle(height: 1.8)),
          if (actionLabel != null) ...[
            const SizedBox(height: 14),
            FilledButton(onPressed: onAction, child: Text(actionLabel!)),
          ],
        ],
      ),
    );
  }
}

class AddVehicleDialog extends StatefulWidget {
  const AddVehicleDialog({super.key});
  @override
  State<AddVehicleDialog> createState() => _AddVehicleDialogState();
}

class _AddVehicleDialogState extends State<AddVehicleDialog> {
  final formKey = GlobalKey<FormState>();
  final title = TextEditingController();
  final plate1 = TextEditingController();
  final letter = TextEditingController(text: 'ب');
  final plate3 = TextEditingController();
  final city = TextEditingController();
  final tank = TextEditingController(text: '55');
  final odometer = TextEditingController(text: '0');
  String fuelType = 'بنزین';
  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('افزودن خودرو'),
      content: SizedBox(
        width: 480,
        child: Form(
          key: formKey,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextFormField(
                  controller: title,
                  decoration: const InputDecoration(labelText: 'نام خودرو'),
                  validator: _required,
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: plate1,
                        decoration: const InputDecoration(
                          labelText: 'دو رقم اول',
                        ),
                        validator: _required,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: TextFormField(
                        controller: letter,
                        decoration: const InputDecoration(
                          labelText: 'حرف پلاک',
                        ),
                        validator: _required,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: TextFormField(
                        controller: plate3,
                        decoration: const InputDecoration(
                          labelText: 'سه رقم دوم',
                        ),
                        validator: _required,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: TextFormField(
                        controller: city,
                        decoration: const InputDecoration(labelText: 'کد شهر'),
                        validator: _required,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                DropdownButtonFormField<String>(
                  initialValue: fuelType,
                  decoration: const InputDecoration(labelText: 'نوع سوخت'),
                  items: const ['بنزین', 'دوگانه سوز', 'گازوئیل', 'برقی']
                      .map((e) => DropdownMenuItem(value: e, child: Text(e)))
                      .toList(),
                  onChanged: (value) =>
                      setState(() => fuelType = value ?? fuelType),
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: tank,
                        keyboardType: TextInputType.number,
                        decoration: const InputDecoration(
                          labelText: 'ظرفیت باک',
                        ),
                        validator: _required,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: TextFormField(
                        controller: odometer,
                        keyboardType: TextInputType.number,
                        decoration: const InputDecoration(
                          labelText: 'کارکرد فعلی',
                        ),
                        validator: _required,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('انصراف'),
        ),
        FilledButton(
          onPressed: () async {
            if (!(formKey.currentState?.validate() ?? false)) return;
            await context.read<AppState>().addVehicle(
              title: title.text,
              plateFirst2: plate1.text,
              plateLetter: letter.text,
              plateLast3: plate3.text,
              plateCityCode: city.text,
              fuelType: fuelType,
              tankCapacity: double.tryParse(tank.text) ?? 0,
              odometer: int.tryParse(odometer.text) ?? 0,
            );
            if (context.mounted) Navigator.pop(context);
          },
          child: const Text('ثبت خودرو'),
        ),
      ],
    );
  }
}

class AddFuelLogDialog extends StatefulWidget {
  const AddFuelLogDialog({super.key, required this.vehicle});
  final Vehicle vehicle;
  @override
  State<AddFuelLogDialog> createState() => _AddFuelLogDialogState();
}

class _AddFuelLogDialogState extends State<AddFuelLogDialog> {
  final formKey = GlobalKey<FormState>();
  final odometer = TextEditingController();
  final liters = TextEditingController();
  final price = TextEditingController();
  final station = TextEditingController();
  final notes = TextEditingController();
  bool full = true;
  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('ثبت سوخت‌گیری'),
      content: SizedBox(
        width: 480,
        child: Form(
          key: formKey,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextFormField(
                  controller: odometer,
                  decoration: const InputDecoration(labelText: 'کارکرد خودرو'),
                  keyboardType: TextInputType.number,
                  validator: _required,
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: liters,
                        decoration: const InputDecoration(labelText: 'لیتر'),
                        keyboardType: TextInputType.number,
                        validator: _required,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: TextFormField(
                        controller: price,
                        decoration: const InputDecoration(
                          labelText: 'قیمت هر لیتر',
                        ),
                        keyboardType: TextInputType.number,
                        validator: _required,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: station,
                  decoration: const InputDecoration(labelText: 'نام جایگاه'),
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: notes,
                  decoration: const InputDecoration(labelText: 'توضیحات'),
                ),
                const SizedBox(height: 10),
                SwitchListTile(
                  contentPadding: EdgeInsets.zero,
                  title: const Text('باک کامل'),
                  value: full,
                  onChanged: (value) => setState(() => full = value),
                ),
              ],
            ),
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('انصراف'),
        ),
        FilledButton(
          onPressed: () async {
            if (!(formKey.currentState?.validate() ?? false)) return;
            await context.read<AppState>().addFuelLog(
              vehicleId: widget.vehicle.id,
              odometer: int.tryParse(odometer.text) ?? 0,
              liters: double.tryParse(liters.text) ?? 0,
              pricePerLiter: int.tryParse(price.text) ?? 0,
              stationName: station.text,
              isFullTank: full,
              notes: notes.text,
            );
            if (context.mounted) Navigator.pop(context);
          },
          child: const Text('ثبت'),
        ),
      ],
    );
  }
}

class QuickAddReminderDialog extends StatefulWidget {
  const QuickAddReminderDialog({super.key, required this.vehicle});
  final Vehicle vehicle;
  @override
  State<QuickAddReminderDialog> createState() => _QuickAddReminderDialogState();
}

class _QuickAddReminderDialogState extends State<QuickAddReminderDialog> {
  final formKey = GlobalKey<FormState>();
  final type = TextEditingController(text: 'تعویض روغن');
  final targetDate = TextEditingController(
    text: DateFormat(
      'yyyy-MM-dd',
    ).format(DateTime.now().add(const Duration(days: 45))),
  );
  final targetKm = TextEditingController();
  final notes = TextEditingController();
  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('یادآور سرویس'),
      content: SizedBox(
        width: 450,
        child: Form(
          key: formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextFormField(
                controller: type,
                decoration: const InputDecoration(labelText: 'نوع سرویس'),
                validator: _required,
              ),
              const SizedBox(height: 10),
              TextFormField(
                controller: targetDate,
                decoration: const InputDecoration(
                  labelText: 'تاریخ هدف (yyyy-mm-dd)',
                ),
                validator: _required,
              ),
              const SizedBox(height: 10),
              TextFormField(
                controller: targetKm,
                decoration: const InputDecoration(labelText: 'کارکرد هدف'),
                keyboardType: TextInputType.number,
                validator: _required,
              ),
              const SizedBox(height: 10),
              TextFormField(
                controller: notes,
                decoration: const InputDecoration(labelText: 'توضیحات'),
              ),
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('انصراف'),
        ),
        FilledButton(
          onPressed: () async {
            if (!(formKey.currentState?.validate() ?? false)) return;
            final date = DateTime.tryParse(targetDate.text) ?? DateTime.now();
            await context.read<AppState>().addServiceReminder(
              vehicleId: widget.vehicle.id,
              serviceType: type.text,
              targetDateMillis: date.millisecondsSinceEpoch,
              targetOdometer: int.tryParse(targetKm.text) ?? 0,
              notes: notes.text,
            );
            if (context.mounted) Navigator.pop(context);
          },
          child: const Text('ثبت'),
        ),
      ],
    );
  }
}

class AddServiceHistoryDialog extends StatefulWidget {
  const AddServiceHistoryDialog({super.key, required this.vehicle});
  final Vehicle vehicle;
  @override
  State<AddServiceHistoryDialog> createState() =>
      _AddServiceHistoryDialogState();
}

class _AddServiceHistoryDialogState extends State<AddServiceHistoryDialog> {
  final formKey = GlobalKey<FormState>();
  final type = TextEditingController(text: 'تعویض روغن');
  final items = TextEditingController();
  final odometer = TextEditingController();
  final nextDue = TextEditingController();
  final date = TextEditingController(
    text: DateFormat('yyyy-MM-dd').format(DateTime.now()),
  );
  final cost = TextEditingController();
  final shop = TextEditingController();
  final notes = TextEditingController();
  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('ثبت سابقه سرویس'),
      content: SizedBox(
        width: 480,
        child: Form(
          key: formKey,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextFormField(
                  controller: type,
                  decoration: const InputDecoration(labelText: 'نوع سرویس'),
                  validator: _required,
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: items,
                  decoration: const InputDecoration(
                    labelText: 'اقلام تعویض‌شده',
                  ),
                  validator: _required,
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: odometer,
                        decoration: const InputDecoration(labelText: 'کارکرد'),
                        keyboardType: TextInputType.number,
                        validator: _required,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: TextFormField(
                        controller: nextDue,
                        decoration: const InputDecoration(
                          labelText: 'کارکرد سرویس بعدی',
                        ),
                        keyboardType: TextInputType.number,
                        validator: _required,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: date,
                        decoration: const InputDecoration(labelText: 'تاریخ'),
                        validator: _required,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: TextFormField(
                        controller: cost,
                        decoration: const InputDecoration(labelText: 'هزینه'),
                        keyboardType: TextInputType.number,
                        validator: _required,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: shop,
                  decoration: const InputDecoration(
                    labelText: 'مکانیک / مرکز سرویس',
                  ),
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: notes,
                  decoration: const InputDecoration(labelText: 'یادداشت'),
                ),
              ],
            ),
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('انصراف'),
        ),
        FilledButton(
          onPressed: () async {
            if (!(formKey.currentState?.validate() ?? false)) return;
            await context.read<AppState>().addServiceHistory(
              vehicleId: widget.vehicle.id,
              serviceType: type.text,
              itemsChanged: items.text,
              odometer: int.tryParse(odometer.text) ?? 0,
              nextDueOdometer: int.tryParse(nextDue.text) ?? 0,
              dateMillis: (DateTime.tryParse(date.text) ?? DateTime.now())
                  .millisecondsSinceEpoch,
              cost: int.tryParse(cost.text) ?? 0,
              mechanicOrShop: shop.text,
              notes: notes.text,
            );
            if (context.mounted) Navigator.pop(context);
          },
          child: const Text('ثبت'),
        ),
      ],
    );
  }
}

class ServiceRequestDialog extends StatefulWidget {
  const ServiceRequestDialog({super.key, required this.serviceTitle});
  final String serviceTitle;
  @override
  State<ServiceRequestDialog> createState() => _ServiceRequestDialogState();
}

class _ServiceRequestDialogState extends State<ServiceRequestDialog> {
  final formKey = GlobalKey<FormState>();
  final fullName = TextEditingController();
  final nationalCode = TextEditingController();
  final phone = TextEditingController();
  final plate = TextEditingController();
  final vin = TextEditingController();
  final barcode = TextEditingController();
  final engine = TextEditingController();
  final chassis = TextEditingController();
  final postal = TextEditingController();
  final address = TextEditingController();
  final details = TextEditingController();
  String insuranceCategory = 'شخص ثالث';
  String insuranceCompany = 'ایران';
  int durationMonths = 12;
  int discount = 0;

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: Text(widget.serviceTitle),
      content: SizedBox(
        width: 540,
        child: Form(
          key: formKey,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextFormField(
                  controller: fullName,
                  decoration: const InputDecoration(
                    labelText: 'نام و نام خانوادگی',
                  ),
                  validator: _required,
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: nationalCode,
                        decoration: const InputDecoration(labelText: 'کد ملی'),
                        validator: _required,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: TextFormField(
                        controller: phone,
                        decoration: const InputDecoration(
                          labelText: 'شماره تماس',
                        ),
                        validator: _required,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: plate,
                  decoration: const InputDecoration(labelText: 'شماره پلاک'),
                  validator: _required,
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: vin,
                        decoration: const InputDecoration(labelText: 'VIN'),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: TextFormField(
                        controller: barcode,
                        decoration: const InputDecoration(
                          labelText: 'بارکد کارت خودرو',
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: engine,
                        decoration: const InputDecoration(
                          labelText: 'شماره موتور',
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: TextFormField(
                        controller: chassis,
                        decoration: const InputDecoration(
                          labelText: 'شماره شاسی',
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: DropdownButtonFormField<String>(
                        initialValue: insuranceCategory,
                        decoration: const InputDecoration(
                          labelText: 'نوع بیمه',
                        ),
                        items: const ['شخص ثالث', 'بدنه', 'هر دو']
                            .map(
                              (e) => DropdownMenuItem(value: e, child: Text(e)),
                            )
                            .toList(),
                        onChanged: (value) =>
                            setState(() => insuranceCategory = value!),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: DropdownButtonFormField<String>(
                        initialValue: insuranceCompany,
                        decoration: const InputDecoration(
                          labelText: 'شرکت بیمه',
                        ),
                        items:
                            const ['ایران', 'آسیا', 'البرز', 'دانا', 'پارسیان']
                                .map(
                                  (e) => DropdownMenuItem(
                                    value: e,
                                    child: Text(e),
                                  ),
                                )
                                .toList(),
                        onChanged: (value) =>
                            setState(() => insuranceCompany = value!),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: DropdownButtonFormField<int>(
                        initialValue: durationMonths,
                        decoration: const InputDecoration(
                          labelText: 'مدت بیمه',
                        ),
                        items: const [6, 12]
                            .map(
                              (e) => DropdownMenuItem(
                                value: e,
                                child: Text('$e ماه'),
                              ),
                            )
                            .toList(),
                        onChanged: (value) =>
                            setState(() => durationMonths = value!),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: DropdownButtonFormField<int>(
                        initialValue: discount,
                        decoration: const InputDecoration(
                          labelText: 'درصد تخفیف',
                        ),
                        items: const [0, 5, 10, 15, 20, 30]
                            .map(
                              (e) => DropdownMenuItem(
                                value: e,
                                child: Text('$e٪'),
                              ),
                            )
                            .toList(),
                        onChanged: (value) => setState(() => discount = value!),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: postal,
                  decoration: const InputDecoration(labelText: 'کد پستی'),
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: address,
                  minLines: 2,
                  maxLines: 3,
                  decoration: const InputDecoration(labelText: 'آدرس'),
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: details,
                  minLines: 2,
                  maxLines: 4,
                  decoration: const InputDecoration(
                    labelText: 'توضیحات تکمیلی',
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('انصراف'),
        ),
        FilledButton(
          onPressed: () async {
            if (!(formKey.currentState?.validate() ?? false)) return;
            await context.read<AppState>().submitServiceRequest(
              requestType: widget.serviceTitle,
              title: widget.serviceTitle,
              fullName: fullName.text,
              nationalCode: nationalCode.text,
              phoneNumber: phone.text,
              vehiclePlate: plate.text,
              vinCode: vin.text,
              barcode: barcode.text,
              engineNumber: engine.text,
              chassisNumber: chassis.text,
              postalCode: postal.text,
              address: address.text,
              insuranceCategory: insuranceCategory,
              insuranceCompany: insuranceCompany,
              durationMonths: durationMonths,
              discountPercent: discount,
              details: details.text,
            );
            if (context.mounted) Navigator.pop(context);
          },
          child: const Text('ارسال درخواست'),
        ),
      ],
    );
  }
}

class InquiryFormDialog extends StatefulWidget {
  const InquiryFormDialog({super.key});
  @override
  State<InquiryFormDialog> createState() => _InquiryFormDialogState();
}

class _InquiryFormDialogState extends State<InquiryFormDialog> {
  final formKey = GlobalKey<FormState>();
  String inquiryType = 'استعلام خلافی خودرو';
  String workflow = 'ADMIN_BALE';
  final plate = TextEditingController();
  final barcodeOrVin = TextEditingController();
  final nationalId = TextEditingController();
  final amount = TextEditingController(text: '250000');
  final fullName = TextEditingController();
  final phone = TextEditingController();
  final vin = TextEditingController();
  final barcode = TextEditingController();
  final engine = TextEditingController();
  final chassis = TextEditingController();
  final postal = TextEditingController();
  final address = TextEditingController();

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('استعلام جدید'),
      content: SizedBox(
        width: 520,
        child: Form(
          key: formKey,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                DropdownButtonFormField<String>(
                  initialValue: inquiryType,
                  decoration: const InputDecoration(labelText: 'نوع استعلام'),
                  items:
                      const [
                            'استعلام خلافی خودرو',
                            'استعلام عوارض آزادراهی',
                            'استعلام عوارض سالیانه',
                            'استعلام خلافی موتورسیکلت',
                          ]
                          .map(
                            (e) => DropdownMenuItem(value: e, child: Text(e)),
                          )
                          .toList(),
                  onChanged: (value) => setState(() => inquiryType = value!),
                ),
                const SizedBox(height: 10),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(14),
                    color: Theme.of(context).colorScheme.surfaceContainerHighest,
                  ),
                  child: const Text(
                    'درخواست استعلام پس از ثبت، به‌صورت فرم کامل برای ربات بله ارسال می‌شود. در صورت نبود اینترنت، ابتدا روی دستگاه صف می‌شود و بعد از اتصال ارسال خواهد شد.',
                    textAlign: TextAlign.right,
                  ),
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: plate,
                  decoration: const InputDecoration(labelText: 'پلاک / شناسه'),
                  validator: _required,
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: barcodeOrVin,
                  decoration: const InputDecoration(labelText: 'بارکد یا VIN'),
                  validator: _required,
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: nationalId,
                  decoration: const InputDecoration(labelText: 'کد ملی'),
                  validator: _required,
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: amount,
                  decoration: const InputDecoration(labelText: 'مبلغ'),
                  keyboardType: TextInputType.number,
                  validator: _required,
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: fullName,
                        decoration: const InputDecoration(labelText: 'نام'),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: TextFormField(
                        controller: phone,
                        decoration: const InputDecoration(
                          labelText: 'شماره تماس',
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: vin,
                        decoration: const InputDecoration(labelText: 'VIN'),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: TextFormField(
                        controller: barcode,
                        decoration: const InputDecoration(labelText: 'بارکد'),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: engine,
                        decoration: const InputDecoration(
                          labelText: 'شماره موتور',
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: TextFormField(
                        controller: chassis,
                        decoration: const InputDecoration(
                          labelText: 'شماره شاسی',
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: postal,
                  decoration: const InputDecoration(labelText: 'کد پستی'),
                ),
                const SizedBox(height: 10),
                TextFormField(
                  controller: address,
                  minLines: 2,
                  maxLines: 3,
                  decoration: const InputDecoration(labelText: 'آدرس'),
                ),
              ],
            ),
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('انصراف'),
        ),
        FilledButton(
          onPressed: () async {
            if (!(formKey.currentState?.validate() ?? false)) return;
            await context.read<AppState>().submitInquiry(
              inquiryType: inquiryType,
              title: inquiryType,
              plateNumber: plate.text,
              barcodeOrVin: barcodeOrVin.text,
              nationalId: nationalId.text,
              amount: int.tryParse(amount.text) ?? 0,
              workflowMethod: workflow,
              fullName: fullName.text,
              phoneNumber: phone.text,
              vinCode: vin.text,
              barcode: barcode.text,
              engineNumber: engine.text,
              chassisNumber: chassis.text,
              postalCode: postal.text,
              address: address.text,
            );
            if (context.mounted) Navigator.pop(context);
          },
          child: const Text('ثبت استعلام'),
        ),
      ],
    );
  }
}

class SetPinDialog extends StatefulWidget {
  const SetPinDialog({super.key});
  @override
  State<SetPinDialog> createState() => _SetPinDialogState();
}

class _SetPinDialogState extends State<SetPinDialog> {
  final pin = TextEditingController();
  final repeat = TextEditingController();
  String? error;
  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('تنظیم رمز ورود'),
      content: SizedBox(
        width: 420,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: pin,
              keyboardType: TextInputType.number,
              maxLength: 4,
              decoration: const InputDecoration(labelText: 'رمز ۴ رقمی'),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: repeat,
              keyboardType: TextInputType.number,
              maxLength: 4,
              decoration: const InputDecoration(labelText: 'تکرار رمز'),
            ),
            if (error != null) ...[
              const SizedBox(height: 10),
              Text(error!, style: const TextStyle(color: Colors.red)),
            ],
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('انصراف'),
        ),
        FilledButton(
          onPressed: () async {
            if (pin.text.length != 4 || pin.text != repeat.text) {
              setState(() => error = 'رمزها باید ۴ رقمی و یکسان باشند.');
              return;
            }
            await context.read<AppState>().securityManager.setPin(pin.text);
            if (context.mounted) Navigator.pop(context);
          },
          child: const Text('ذخیره'),
        ),
      ],
    );
  }
}

class InspectionDialog extends StatefulWidget {
  const InspectionDialog({super.key, required this.vehicle});
  final Vehicle vehicle;
  @override
  State<InspectionDialog> createState() => _InspectionDialogState();
}

class _InspectionDialogState extends State<InspectionDialog> {
  final center = TextEditingController();
  final date = TextEditingController();
  final notify = TextEditingController(text: '7');
  @override
  void initState() {
    super.initState();
    date.text = DateFormat(
      'yyyy-MM-dd',
    ).format(DateTime.now().add(const Duration(days: 30)));
    center.text = widget.vehicle.inspectionCenter;
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('تنظیم معاینه فنی'),
      content: SizedBox(
        width: 420,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: center,
              decoration: const InputDecoration(labelText: 'مرکز معاینه فنی'),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: date,
              decoration: const InputDecoration(
                labelText: 'تاریخ انقضا (yyyy-mm-dd)',
              ),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: notify,
              decoration: const InputDecoration(
                labelText: 'روزهای پیش از انقضا',
              ),
              keyboardType: TextInputType.number,
            ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('انصراف'),
        ),
        FilledButton(
          onPressed: () async {
            await context.read<AppState>().updateVehicleInspection(
              vehicle: widget.vehicle,
              expiryMillis: (DateTime.tryParse(date.text) ?? DateTime.now())
                  .millisecondsSinceEpoch,
              centerName: center.text,
              notifyDaysBefore: int.tryParse(notify.text) ?? 7,
            );
            if (context.mounted) Navigator.pop(context);
          },
          child: const Text('ذخیره'),
        ),
      ],
    );
  }
}

class InsuranceDialog extends StatefulWidget {
  const InsuranceDialog({super.key, required this.vehicle});
  final Vehicle vehicle;
  @override
  State<InsuranceDialog> createState() => _InsuranceDialogState();
}

class _InsuranceDialogState extends State<InsuranceDialog> {
  final company = TextEditingController();
  final type = TextEditingController(text: 'شخص ثالث');
  final date = TextEditingController();
  @override
  void initState() {
    super.initState();
    company.text = widget.vehicle.insuranceCompany;
    type.text = widget.vehicle.insuranceType.isEmpty
        ? 'شخص ثالث'
        : widget.vehicle.insuranceType;
    date.text = DateFormat(
      'yyyy-MM-dd',
    ).format(DateTime.now().add(const Duration(days: 365)));
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('ثبت اطلاعات بیمه'),
      content: SizedBox(
        width: 420,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: company,
              decoration: const InputDecoration(labelText: 'شرکت بیمه'),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: type,
              decoration: const InputDecoration(labelText: 'نوع بیمه'),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: date,
              decoration: const InputDecoration(
                labelText: 'تاریخ انقضا (yyyy-mm-dd)',
              ),
            ),
          ],
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('انصراف'),
        ),
        FilledButton(
          onPressed: () async {
            await context.read<AppState>().updateVehicleInsurance(
              vehicle: widget.vehicle,
              expiryMillis: (DateTime.tryParse(date.text) ?? DateTime.now())
                  .millisecondsSinceEpoch,
              company: company.text,
              type: type.text,
            );
            if (context.mounted) Navigator.pop(context);
          },
          child: const Text('ثبت بیمه'),
        ),
      ],
    );
  }
}

class DonationDialog extends StatefulWidget {
  const DonationDialog({super.key});
  @override
  State<DonationDialog> createState() => _DonationDialogState();
}

class _DonationDialogState extends State<DonationDialog> {
  final amount = TextEditingController(text: '100000');
  final customAmount = TextEditingController();
  final name = TextEditingController();
  final phone = TextEditingController();
  final note = TextEditingController();
  Uint8List? image;

  Future<void> _pick() async {
    final picker = ImagePicker();
    final xfile = await picker.pickImage(
      source: ImageSource.gallery,
      imageQuality: 90,
    );
    if (xfile == null) return;
    image = await xfile.readAsBytes();
    if (mounted) setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text('حمایت مالی و ارسال فیش'),
      content: SizedBox(
        width: 500,
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: amount,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(labelText: 'مبلغ ثابت'),
              ),
              const SizedBox(height: 10),
              TextField(
                controller: customAmount,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(labelText: 'مبلغ دلخواه'),
              ),
              const SizedBox(height: 10),
              Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: name,
                      decoration: const InputDecoration(
                        labelText: 'نام پرداخت‌کننده',
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: TextField(
                      controller: phone,
                      decoration: const InputDecoration(
                        labelText: 'شماره تماس',
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 10),
              TextField(
                controller: note,
                minLines: 2,
                maxLines: 4,
                decoration: const InputDecoration(labelText: 'یادداشت'),
              ),
              const SizedBox(height: 12),
              OutlinedButton.icon(
                onPressed: _pick,
                icon: const Icon(Icons.image_rounded),
                label: Text(
                  image == null ? 'انتخاب تصویر فیش' : 'تصویر انتخاب شد',
                ),
              ),
            ],
          ),
        ),
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text('انصراف'),
        ),
        FilledButton(
          onPressed: () async {
            await context.read<AppState>().sendDonationSupport(
              amount: int.tryParse(amount.text) ?? 0,
              customAmountText: customAmount.text,
              payerName: name.text,
              payerPhone: phone.text,
              note: note.text,
              photoBytes: image,
            );
            if (context.mounted) Navigator.pop(context);
          },
          child: const Text('ارسال به ربات'),
        ),
      ],
    );
  }
}

String? _required(String? value) =>
    (value == null || value.trim().isEmpty) ? 'این فیلد الزامی است.' : null;
