import 'package:flutter/material.dart';

/// Palette ported 1:1 from the original Compose `ui/theme/Color.kt`.
class AppColors {
  AppColors._();

  // Sky Blue theme (unified main app theme)
  static const skyBluePrimary = Color(0xFF0284C7);
  static const skyBluePrimaryDark = Color(0xFF38BDF8);
  static const skyBlueContainer = Color(0xFFE0F2FE);
  static const skyBlueOnContainer = Color(0xFF0369A1);
  static const skyBlueLight = Color(0xFFBAE6FD);

  // Base petrol / slate accents
  static const petrolDark = Color(0xFF0F172A);
  static const petrolSurfaceDark = Color(0xFF1E293B);
  static const petrolCardDark = Color(0xFF334155);

  // Status colors
  static const fuelGold = Color(0xFFF59E0B);
  static const fuelGreen = Color(0xFF10B981);
  static const fuelRed = Color(0xFFEF4444);

  static const emeraldGreen = fuelGreen;
  static const rosePink = Color(0xFFEC4899);
  static const orangeAccent = Color(0xFFF97316);
  static const deepIndigo = Color(0xFF6366F1);

  // Light backgrounds
  static const lightBackground = Color(0xFFF8FAFC);
  static const lightSurface = Color(0xFFFFFFFF);
  static const lightSurfaceVariant = Color(0xFFF1F5F9);

  // Dark backgrounds
  static const darkBackground = Color(0xFF0F172A);
  static const darkSurface = Color(0xFF1E293B);
  static const darkSurfaceVariant = Color(0xFF334155);

  /// Chart slice colors (monthly distribution) - identical to the Compose list.
  static const chartSlices = <Color>[
    Color(0xFF0284C7), // Sky Blue
    Color(0xFF38BDF8), // Light Sky Blue
    Color(0xFF0D9488), // Teal
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber
    Color(0xFFF97316), // Orange
    Color(0xFFEF4444), // Red
    Color(0xFF8B5CF6), // Violet
    Color(0xFFEC4899), // Pink
    Color(0xFF6366F1), // Indigo
    Color(0xFF14B8A6), // Cyan
    Color(0xFF84CC16), // Lime
  ];

  /// Splash gradient (from SplashScreen.kt).
  static const splashGradient = <Color>[
    Color(0xFF0F172A),
    Color(0xFF003544),
    Color(0xFF0284C7),
  ];

  /// Inquiry hero gradient.
  static const heroGradient = <Color>[
    Color(0xFF0F172A),
    Color(0xFF0369A1),
    Color(0xFF0284C7),
  ];
}

/// User-selectable accent themes (mirrors `AppThemeColor` enum).
enum AppThemeColor {
  skyBlue('آبی آسمانی', Color(0xFF0284C7), Color(0xFF38BDF8)),
  red('قرمز', Color(0xFFDC2626), Color(0xFFF87171)),
  purple('بنفش', Color(0xFF7C3AED), Color(0xFFA78BFA)),
  emerald('سبز زمردی', Color(0xFF059669), Color(0xFF34D399));

  const AppThemeColor(this.title, this.primary, this.primaryDark);

  String get label => title;

  final String title;
  final Color primary;
  final Color primaryDark;
}

/// Dark / light mode preference (mirrors `DarkModePref` enum).
enum DarkModePref {
  system('خودکار (سیستم)'),
  light('روشن (لایت)'),
  dark('تاریک (دارک)');

  const DarkModePref(this.title);

  String get label => title;

  final String title;
}
