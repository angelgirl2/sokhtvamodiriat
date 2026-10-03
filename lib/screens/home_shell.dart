import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../app_controller.dart';
import '../widgets/bottom_nav.dart';
import 'fuel_screen.dart';
import 'inquiry_screen.dart';
import 'reminders_screen.dart';
import 'services_screen.dart';
import 'settings_screen.dart';

class HomeShell extends StatefulWidget {
  const HomeShell({super.key, required this.controller});
  final AppController controller;
  @override
  State<HomeShell> createState() => _HomeShellState();
}

class _HomeShellState extends State<HomeShell> {
  DateTime? _lastBack;
  @override
  Widget build(BuildContext context) {
    final pages = [
      FuelScreen(controller: widget.controller),
      ServicesScreen(controller: widget.controller),
      InquiryScreen(controller: widget.controller),
      RemindersScreen(controller: widget.controller),
      SettingsScreen(controller: widget.controller),
    ];
    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, result) {
        if (didPop) return;
        if (widget.controller.tab != 0) {
          widget.controller.tab = 0;
          widget.controller.notifyListeners();
          return;
        }
        final now = DateTime.now();
        if (_lastBack == null || now.difference(_lastBack!) > const Duration(seconds: 2)) {
          _lastBack = now;
          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('برای خروج دوباره دکمه بازگشت را بزنید')));
          return;
        }
        SystemNavigator.pop();
      },
      child: Scaffold(
        body: SafeArea(
          child: AnimatedSwitcher(
            duration: const Duration(milliseconds: 360),
            switchInCurve: Curves.easeOutCubic,
            switchOutCurve: Curves.easeInCubic,
            transitionBuilder: (child, animation) => FadeTransition(opacity: animation, child: SlideTransition(position: Tween(begin: const Offset(0, .025), end: Offset.zero).animate(animation), child: child)),
            child: KeyedSubtree(key: ValueKey(widget.controller.tab), child: pages[widget.controller.tab]),
          ),
        ),
        bottomNavigationBar: GlassBottomNav(controller: widget.controller),
      ),
    );
  }
}
