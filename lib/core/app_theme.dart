import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

import 'app_colors.dart';

/// Material 3 theme builder, ported from the Compose `Theme.kt`.
///
/// The accent color is user-selectable and the light/dark decision follows the
/// `DarkModePref` enum.
class AppTheme {
  AppTheme._();

  static const fontFamily = 'Vazirmatn';

  static ColorScheme _lightScheme(AppThemeColor accent) {
    final isSky = accent == AppThemeColor.skyBlue;
    return ColorScheme.light(
      primary: accent.primary,
      onPrimary: Colors.white,
      primaryContainer: isSky
          ? AppColors.skyBlueContainer
          : accent.primary.withValues(alpha: 0.15),
      onPrimaryContainer: isSky ? AppColors.skyBlueOnContainer : accent.primary,
      secondary: AppColors.petrolDark,
      onSecondary: Colors.white,
      secondaryContainer: const Color(0xFFE2E8F0),
      onSecondaryContainer: AppColors.petrolDark,
      surface: AppColors.lightSurface,
      surfaceContainerHighest: AppColors.lightSurfaceVariant,
      onSurface: AppColors.petrolSurfaceDark,
      outline: const Color(0xFFCBD5E1),
      outlineVariant: const Color(0xFFE2E8F0),
    );
  }

  static ColorScheme _darkScheme(AppThemeColor accent) {
    final isSky = accent == AppThemeColor.skyBlue;
    return ColorScheme.dark(
      primary: accent.primaryDark,
      onPrimary: isSky ? const Color(0xFF003544) : Colors.white,
      primaryContainer: isSky
          ? const Color(0xFF004D61)
          : accent.primary.withValues(alpha: 0.28),
      onPrimaryContainer: isSky ? const Color(0xFFBBE9FF) : accent.primaryDark,
      secondary: const Color(0xFF93C5FD),
      onSecondary: AppColors.petrolDark,
      secondaryContainer: AppColors.petrolSurfaceDark,
      onSecondaryContainer: const Color(0xFFF1F5F9),
      surface: AppColors.darkSurface,
      surfaceContainerHighest: AppColors.darkSurfaceVariant,
      onSurface: const Color(0xFFF8FAFC),
      outline: const Color(0xFF475569),
      outlineVariant: const Color(0xFF334155),
    );
  }

  static ThemeData build({
    required AppThemeColor accent,
    required DarkModePref darkModePref,
    required Brightness platformBrightness,
  }) {
    final isDark = switch (darkModePref) {
      DarkModePref.system => platformBrightness == Brightness.dark,
      DarkModePref.light => false,
      DarkModePref.dark => true,
    };

    final scheme = isDark ? _darkScheme(accent) : _lightScheme(accent);

    final base = isDark ? ThemeData.dark() : ThemeData.light();

    return base.copyWith(
      colorScheme: scheme,
      scaffoldBackgroundColor: scheme.surface,
      splashFactory: InkSparkle.splashFactory,
      textTheme: _textTheme(base.textTheme, scheme),
      appBarTheme: AppBarTheme(
        backgroundColor: scheme.surface,
        foregroundColor: scheme.onSurface,
        elevation: 0,
        centerTitle: false,
        titleTextStyle: TextStyle(
          fontFamily: fontFamily,
          fontSize: 17,
          fontWeight: FontWeight.w800,
          color: scheme.onSurface,
        ),
      ),
      cardTheme: CardThemeData(
        color: scheme.surface,
        elevation: 2,
        margin: EdgeInsets.zero,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      ),
      chipTheme: ChipThemeData(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
        side: BorderSide(color: scheme.outlineVariant),
        labelStyle: const TextStyle(fontFamily: fontFamily, fontSize: 11.5),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: scheme.surfaceContainerHighest.withValues(alpha: 0.45),
        isDense: true,
        contentPadding: const EdgeInsets.symmetric(
          horizontal: 12,
          vertical: 12,
        ),
        labelStyle: TextStyle(
          fontFamily: fontFamily,
          fontSize: 12.5,
          color: scheme.onSurfaceVariant,
        ),
        hintStyle: TextStyle(
          fontFamily: fontFamily,
          fontSize: 12,
          color: scheme.onSurfaceVariant,
        ),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: scheme.outlineVariant),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: scheme.outlineVariant),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: scheme.primary, width: 1.6),
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
          textStyle: const TextStyle(
            fontFamily: fontFamily,
            fontWeight: FontWeight.w700,
            fontSize: 12.5,
          ),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
          side: BorderSide(color: scheme.outlineVariant),
          textStyle: const TextStyle(
            fontFamily: fontFamily,
            fontWeight: FontWeight.w600,
            fontSize: 12.5,
          ),
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          textStyle: const TextStyle(
            fontFamily: fontFamily,
            fontWeight: FontWeight.w700,
            fontSize: 12.5,
          ),
        ),
      ),
      dividerTheme: DividerThemeData(
        color: scheme.outlineVariant.withValues(alpha: 0.5),
        thickness: 1,
        space: 1,
      ),
      dialogTheme: DialogThemeData(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(22)),
        backgroundColor: scheme.surface,
      ),
      snackBarTheme: SnackBarThemeData(
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        contentTextStyle: const TextStyle(
          fontFamily: fontFamily,
          fontSize: 12.5,
          color: Colors.white,
        ),
      ),
      pageTransitionsTheme: const PageTransitionsTheme(
        builders: {
          TargetPlatform.android: FadeForwardsPageTransitionsBuilder(),
          TargetPlatform.iOS: CupertinoPageTransitionsBuilder(),
        },
      ),
    );
  }

  static TextTheme _textTheme(TextTheme base, ColorScheme scheme) {
    TextStyle s(
      double size,
      FontWeight weight, {
      double? height,
      double? letterSpacing,
    }) => TextStyle(
      fontFamily: fontFamily,
      fontSize: size,
      fontWeight: weight,
      height: height,
      letterSpacing: letterSpacing,
      color: scheme.onSurface,
    );

    return base.copyWith(
      displaySmall: s(28, FontWeight.w800),
      headlineMedium: s(24, FontWeight.w800),
      headlineSmall: s(20, FontWeight.w800),
      titleLarge: s(17, FontWeight.w800),
      titleMedium: s(14.5, FontWeight.w700),
      titleSmall: s(13, FontWeight.w700),
      bodyLarge: s(14.5, FontWeight.w400, height: 1.7),
      bodyMedium: s(13, FontWeight.w400, height: 1.7),
      bodySmall: s(11.5, FontWeight.w400, height: 1.6),
      labelLarge: s(12.5, FontWeight.w700),
      labelMedium: s(11, FontWeight.w600),
      labelSmall: s(10, FontWeight.w500),
    );
  }
}
