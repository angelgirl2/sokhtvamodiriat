import 'dart:ui' as ui;

import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

String money(num value) => NumberFormat('#,##0', 'en_US').format(value);
String dateFa(int milliseconds) => DateFormat('yyyy/MM/dd', 'en_US').format(DateTime.fromMillisecondsSinceEpoch(milliseconds));
int parseInt(String value) => int.tryParse(value.replaceAll(',', '').trim()) ?? 0;
double parseDouble(String value) => double.tryParse(value.replaceAll(',', '').trim()) ?? 0;

Future<void> showAppSheet({
  required BuildContext context,
  required Widget child,
  String? title,
}) async {
  await showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    backgroundColor: Colors.transparent,
    barrierColor: Colors.black.withValues(alpha: .35),
    builder: (context) => Directionality(
      textDirection: ui.TextDirection.rtl,
      child: Padding(
        padding: EdgeInsets.only(bottom: MediaQuery.of(context).viewInsets.bottom),
        child: Container(
          constraints: const BoxConstraints(maxHeight: 0.92 * 900),
          padding: const EdgeInsets.fromLTRB(18, 12, 18, 24),
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.surface.withValues(alpha: .97),
            borderRadius: const BorderRadius.vertical(top: Radius.circular(30)),
            border: Border.all(color: Colors.white.withValues(alpha: .15)),
          ),
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Container(
                  width: 42,
                  height: 4,
                  decoration: BoxDecoration(
                    color: Theme.of(context).colorScheme.onSurface.withValues(alpha: .16),
                    borderRadius: BorderRadius.circular(4),
                  ),
                ),
                if (title != null) ...[
                  const SizedBox(height: 14),
                  Align(
                    alignment: Alignment.centerRight,
                    child: Text(title, style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w800)),
                  ),
                  const SizedBox(height: 12),
                ],
                child,
              ],
            ),
          ),
        ),
      ),
    ),
  );
}

InputDecoration fieldDecoration(BuildContext context, String hint, {Widget? prefixIcon}) => InputDecoration(
      hintText: hint,
      prefixIcon: prefixIcon,
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
    );


class AppBlur extends StatelessWidget {
  const AppBlur({super.key, required this.child, this.sigma = 16, this.borderRadius = 24});
  final Widget child;
  final double sigma;
  final double borderRadius;

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(borderRadius),
      child: BackdropFilter(
        filter: ui.ImageFilter.blur(sigmaX: sigma, sigmaY: sigma),
        child: child,
      ),
    );
  }
}
