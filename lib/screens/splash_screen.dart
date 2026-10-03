import 'package:flutter/material.dart';

import '../core/theme.dart';
import '../widgets/logo.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key, required this.onDone});
  final VoidCallback onDone;
  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> with TickerProviderStateMixin {
  late final AnimationController _logo;
  late final AnimationController _text;
  late final Animation<double> _scale;
  late final Animation<double> _opacity;

  @override
  void initState() {
    super.initState();
    _logo = AnimationController(vsync: this, duration: const Duration(milliseconds: 950));
    _text = AnimationController(vsync: this, duration: const Duration(milliseconds: 700));
    _scale = CurvedAnimation(parent: _logo, curve: Curves.easeOutBack);
    _opacity = CurvedAnimation(parent: _text, curve: Curves.easeOutCubic);
    _logo.forward();
    Future.delayed(const Duration(milliseconds: 320), () => _text.forward());
    Future.delayed(const Duration(milliseconds: 1850), () {
      if (mounted) widget.onDone();
    });
  }

  @override
  void dispose() {
    _logo.dispose();
    _text.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final dark = Theme.of(context).brightness == Brightness.dark;
    return Scaffold(
      body: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: dark
                ? const [Color(0xFF08131C), Color(0xFF123447), Color(0xFF091118)]
                : const [Color(0xFFE9F7FB), Color(0xFFBFE8F3), Color(0xFFF9FCFD)],
          ),
        ),
        child: Stack(
          children: [
            Positioned(top: -80, right: -50, child: _blob(220, AppTheme.skyBlue.withValues(alpha: .15))),
            Positioned(bottom: -100, left: -70, child: _blob(260, AppTheme.purple.withValues(alpha: .12))),
            Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  ScaleTransition(scale: _scale, child: const AppLogo(size: 176)),
                  const SizedBox(height: 26),
                  FadeTransition(
                    opacity: _opacity,
                    child: Column(
                      children: [
                        Text('مدیریت سوخت و استعلام', style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w900)),
                        const SizedBox(height: 8),
                        Text('مدیریت هوشمند خودرو، سوخت و خدمات', style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Theme.of(context).colorScheme.onSurfaceVariant)),
                      ],
                    ),
                  ),
                  const SizedBox(height: 44),
                  SizedBox(width: 28, height: 28, child: CircularProgressIndicator(strokeWidth: 2.3, color: Theme.of(context).colorScheme.primary)),
                ],
              ),
            ),
            const Positioned(bottom: 34, left: 0, right: 0, child: Text('SookhtMan • Smart Vehicle Services', textAlign: TextAlign.center, style: TextStyle(fontSize: 11, fontWeight: FontWeight.w700, color: Colors.black45))),
          ],
        ),
      ),
    );
  }

  Widget _blob(double size, Color color) => Container(width: size, height: size, decoration: BoxDecoration(color: color, shape: BoxShape.circle));
}
