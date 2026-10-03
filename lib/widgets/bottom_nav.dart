import 'dart:ui';

import 'package:flutter/material.dart';

import '../app_controller.dart';
import '../core/theme.dart';

class GlassBottomNav extends StatelessWidget {
  const GlassBottomNav({super.key, required this.controller});
  final AppController controller;

  static const items = [
    (Icons.local_gas_station_rounded, 'سوخت'),
    (Icons.assignment_rounded, 'خدمات'),
    (Icons.search_rounded, 'استعلام'),
    (Icons.build_circle_rounded, 'سرویس'),
    (Icons.settings_rounded, 'تنظیمات'),
  ];

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(14, 0, 14, 14),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(30),
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: 18, sigmaY: 18),
          child: DecoratedBox(
            decoration: BoxDecoration(
              color: Theme.of(context).brightness == Brightness.dark
                  ? const Color(0xFF15212B).withOpacity(.78)
                  : Colors.white.withOpacity(.70),
              borderRadius: BorderRadius.circular(30),
              border: Border.all(color: Colors.white.withOpacity(.32)),
              boxShadow: [BoxShadow(color: Colors.black.withOpacity(.10), blurRadius: 24, offset: const Offset(0, 10))],
            ),
            child: SafeArea(
              top: false,
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 6),
                child: Row(
                  children: List.generate(items.length, (index) {
                    final selected = controller.tab == index;
                    final item = items[index];
                    return Expanded(
                      child: GestureDetector(
                        behavior: HitTestBehavior.opaque,
                        onTap: () { controller.tab = index; controller.notifyListeners(); },
                        child: AnimatedContainer(
                          duration: AppMotion.fast,
                          curve: AppMotion.curve,
                          margin: const EdgeInsets.symmetric(horizontal: 3),
                          padding: const EdgeInsets.symmetric(vertical: 8),
                          decoration: BoxDecoration(
                            color: selected ? Theme.of(context).colorScheme.primary.withOpacity(.12) : Colors.transparent,
                            borderRadius: BorderRadius.circular(20),
                          ),
                          child: TweenAnimationBuilder<double>(
                            duration: AppMotion.fast,
                            tween: Tween(begin: 1, end: selected ? 1.07 : 1),
                            builder: (context, scale, child) => Transform.scale(scale: scale, child: child),
                            child: Column(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Icon(item.$1, size: 21, color: selected ? Theme.of(context).colorScheme.primary : Theme.of(context).colorScheme.onSurfaceVariant.withOpacity(.72)),
                                const SizedBox(height: 4),
                                Text(item.$2, style: TextStyle(fontSize: 10.5, fontWeight: selected ? FontWeight.w900 : FontWeight.w600, color: selected ? Theme.of(context).colorScheme.primary : Theme.of(context).colorScheme.onSurfaceVariant)),
                                const SizedBox(height: 2),
                                AnimatedContainer(duration: AppMotion.fast, width: selected ? 5 : 0, height: selected ? 5 : 0, decoration: BoxDecoration(color: AppTheme.skyBlue, shape: BoxShape.circle)),
                              ],
                            ),
                          ),
                        ),
                      ),
                    );
                  }),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
