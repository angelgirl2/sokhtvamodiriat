import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class AppTheme {
  static const Color skyBlue = Color(0xFF58A8C2);
  static const Color skyBlueDeep = Color(0xFF2E7D9B);
  static const Color navy = Color(0xFF17324D);
  static const Color purple = Color(0xFF7C3AED);
  static const Color turquoise = Color(0xFF14B8A6);
  static const Color red = Color(0xFFEF4444);
  static const Color softBlue = Color(0xFF60A5FA);

  static ThemeData light({Color seedColor = skyBlue}) {
    final scheme = ColorScheme.fromSeed(
      seedColor: seedColor,
      brightness: Brightness.light,
    ).copyWith(
      primary: seedColor,
      secondary: turquoise,
      surface: const Color(0xFFF7FAFC),
      surfaceContainerHighest: const Color(0xFFE8F0F4),
    );
    return ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      scaffoldBackgroundColor: const Color(0xFFF3F7F9),
      fontFamily: 'sans',
      pageTransitionsTheme: const PageTransitionsTheme(
        builders: {
          TargetPlatform.android: FadeForwardsPageTransitionsBuilder(),
          TargetPlatform.iOS: CupertinoPageTransitionsBuilder(),
        },
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: Colors.white.withValues(alpha: .75),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(18),
          borderSide: BorderSide.none,
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(18),
          borderSide: BorderSide(color: Colors.black.withValues(alpha: .06)),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(18),
          borderSide: const BorderSide(color: skyBlueDeep, width: 1.4),
        ),
      ),
      cardTheme: CardThemeData(
        elevation: 0,
        color: Colors.white.withValues(alpha: .82),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(26)),
      ),
    );
  }

  static ThemeData dark({Color seedColor = skyBlue}) {
    final scheme = ColorScheme.fromSeed(
      seedColor: seedColor,
      brightness: Brightness.dark,
    ).copyWith(
      primary: seedColor,
      secondary: const Color(0xFF34D6C4),
      surface: const Color(0xFF111820),
      surfaceContainerHighest: const Color(0xFF19232D),
    );
    return ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      scaffoldBackgroundColor: const Color(0xFF0D141A),
      fontFamily: 'sans',
      pageTransitionsTheme: const PageTransitionsTheme(
        builders: {
          TargetPlatform.android: FadeForwardsPageTransitionsBuilder(),
          TargetPlatform.iOS: CupertinoPageTransitionsBuilder(),
        },
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: Colors.white.withValues(alpha: .06),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(18),
          borderSide: BorderSide.none,
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(18),
          borderSide: BorderSide(color: Colors.white.withValues(alpha: .08)),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(18),
          borderSide: BorderSide(color: scheme.primary, width: 1.2),
        ),
      ),
      cardTheme: CardThemeData(
        elevation: 0,
        color: const Color(0xFF151E26).withValues(alpha: .92),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(26)),
      ),
    );
  }
}

class AppMotion {
  static const Duration fast = Duration(milliseconds: 180);
  static const Duration normal = Duration(milliseconds: 340);
  static const Duration slow = Duration(milliseconds: 550);

  static Curve get curve => Curves.easeOutCubic;
}

class GlassDecoration extends Decoration {
  const GlassDecoration({
    this.color,
    this.borderColor,
    this.radius = 24,
  });

  final Color? color;
  final Color? borderColor;
  final double radius;

  @override
  BoxPainter createBoxPainter([VoidCallback? onChanged]) {
    return _GlassBoxPainter(
      color: color,
      borderColor: borderColor,
      radius: radius,
    );
  }
}

class _GlassBoxPainter extends BoxPainter {
  const _GlassBoxPainter({
    this.color,
    this.borderColor,
    required this.radius,
  });

  final Color? color;
  final Color? borderColor;
  final double radius;

  @override
  void paint(Canvas canvas, Offset offset, ImageConfiguration configuration) {
    final rect = offset & (configuration.size ?? Size.zero);
    final rrect = RRect.fromRectAndRadius(rect, Radius.circular(radius));
    final fill = Paint()..color = color ?? Colors.white.withValues(alpha: .18);
    canvas.drawRRect(rrect, fill);
    final border = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1
      ..color = borderColor ?? Colors.white.withValues(alpha: .24);
    canvas.drawRRect(rrect, border);
  }
}
