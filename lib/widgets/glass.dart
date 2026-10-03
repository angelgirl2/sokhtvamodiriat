import 'dart:ui';

import 'package:flutter/material.dart';

import '../core/theme.dart';

class GlassCard extends StatelessWidget {
  const GlassCard({super.key, required this.child, this.padding = const EdgeInsets.all(18), this.margin, this.borderRadius = 26, this.opacity = .72});
  final Widget child;
  final EdgeInsets padding;
  final EdgeInsets? margin;
  final double borderRadius;
  final double opacity;

  @override
  Widget build(BuildContext context) {
    final dark = Theme.of(context).brightness == Brightness.dark;
    return Container(
      margin: margin,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(borderRadius),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(dark ? .18 : .07),
            blurRadius: 28,
            offset: const Offset(0, 12),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(borderRadius),
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: 16, sigmaY: 16),
          child: Container(
            padding: padding,
            decoration: BoxDecoration(
              color: (dark ? const Color(0xFF172129) : Colors.white).withOpacity(opacity),
              borderRadius: BorderRadius.circular(borderRadius),
              border: Border.all(color: Colors.white.withOpacity(dark ? .10 : .55)),
            ),
            child: child,
          ),
        ),
      ),
    );
  }
}

class GradientHeader extends StatelessWidget {
  const GradientHeader({super.key, required this.title, required this.subtitle, this.trailing});
  final String title;
  final String subtitle;
  final Widget? trailing;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              ShaderMask(
                shaderCallback: (bounds) => LinearGradient(colors: [scheme.primary, AppTheme.purple]).createShader(bounds),
                child: Text(title, style: Theme.of(context).textTheme.headlineSmall?.copyWith(color: Colors.white, fontWeight: FontWeight.w900, height: 1.1)),
              ),
              const SizedBox(height: 8),
              Text(subtitle, style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: scheme.onSurfaceVariant, height: 1.45)),
            ],
          ),
        ),
        if (trailing != null) ...[const SizedBox(width: 12), trailing!],
      ],
    );
  }
}

class AnimatedAppear extends StatefulWidget {
  const AnimatedAppear({super.key, required this.child, this.delay = Duration.zero, this.duration = AppMotion.normal});
  final Widget child;
  final Duration delay;
  final Duration duration;

  @override
  State<AnimatedAppear> createState() => _AnimatedAppearState();
}

class _AnimatedAppearState extends State<AnimatedAppear> with SingleTickerProviderStateMixin {
  late final AnimationController _controller;
  late final Animation<double> _opacity;
  late final Animation<Offset> _slide;
  @override
  void initState() {
    super.initState();
    _controller = AnimationController(vsync: this, duration: widget.duration);
    _opacity = CurvedAnimation(parent: _controller, curve: Curves.easeOut);
    _slide = Tween(begin: const Offset(0, .06), end: Offset.zero).animate(CurvedAnimation(parent: _controller, curve: Curves.easeOutCubic));
    Future.delayed(widget.delay, () {
      if (mounted) _controller.forward();
    });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => FadeTransition(
        opacity: _opacity,
        child: SlideTransition(position: _slide, child: widget.child),
      );
}

class StatTile extends StatelessWidget {
  const StatTile({super.key, required this.icon, required this.title, required this.value, required this.color, this.onTap});
  final IconData icon;
  final String title;
  final String value;
  final Color color;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: GestureDetector(
        onTap: onTap,
        child: GlassCard(
          padding: const EdgeInsets.all(16),
          borderRadius: 22,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                width: 42,
                height: 42,
                decoration: BoxDecoration(color: color.withOpacity(.14), borderRadius: BorderRadius.circular(15)),
                child: Icon(icon, color: color),
              ),
              const SizedBox(height: 13),
              Text(value, style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900)),
              const SizedBox(height: 4),
              Text(title, style: Theme.of(context).textTheme.bodySmall?.copyWith(color: Theme.of(context).colorScheme.onSurfaceVariant)),
            ],
          ),
        ),
      ),
    );
  }
}

class SectionTitle extends StatelessWidget {
  const SectionTitle({super.key, required this.title, this.action});
  final String title;
  final Widget? action;
  @override
  Widget build(BuildContext context) => Row(
        children: [
          Expanded(child: Text(title, style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w900))),
          if (action != null) action!,
        ],
      );
}

class PrimaryButton extends StatelessWidget {
  const PrimaryButton({super.key, required this.label, required this.onPressed, this.icon, this.loading = false, this.expand = true});
  final String label;
  final VoidCallback? onPressed;
  final IconData? icon;
  final bool loading;
  final bool expand;

  @override
  Widget build(BuildContext context) {
    final button = FilledButton(
      onPressed: loading ? null : onPressed,
      style: FilledButton.styleFrom(
        padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 15),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(17)),
      ),
      child: AnimatedSwitcher(
        duration: AppMotion.fast,
        child: loading
            ? const SizedBox(key: ValueKey('loading'), width: 21, height: 21, child: CircularProgressIndicator(strokeWidth: 2.3))
            : Row(
                key: const ValueKey('label'),
                mainAxisSize: MainAxisSize.min,
                children: [
                  if (icon != null) ...[Icon(icon, size: 19), const SizedBox(width: 7)],
                  Text(label, style: const TextStyle(fontWeight: FontWeight.w800)),
                ],
              ),
      ),
    );
    return expand ? SizedBox(width: double.infinity, child: button) : button;
  }
}
