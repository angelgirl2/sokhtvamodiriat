import 'package:flutter/material.dart';

class AppLogo extends StatelessWidget {
  const AppLogo({super.key, this.size = 84, this.showTitle = false});
  final double size;
  final bool showTitle;

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Hero(
          tag: 'app-logo',
          child: ClipRRect(
            borderRadius: BorderRadius.circular(size * .22),
            child: Image.asset('assets/app_logo.png', width: size, height: size, fit: BoxFit.cover),
          ),
        ),
        if (showTitle) ...[
          const SizedBox(height: 12),
          Text('مدیریت سوخت و استعلام', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w900)),
        ],
      ],
    );
  }
}
