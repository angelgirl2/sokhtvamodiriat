import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:crypto/crypto.dart';
import 'package:local_auth/local_auth.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../core/app_config.dart';

class SecurityService {
  static const _pinHashKey = 'pin_hash';
  static const _pinEnabledKey = 'pin_enabled';
  static const _bioEnabledKey = 'biometric_enabled';
  static const _tutorialKey = 'tutorial_seen';
  static const _themeKey = 'theme_color';
  static const _darkModeKey = 'dark_mode';

  SecurityService(this.prefs);
  final SharedPreferences prefs;
  final LocalAuthentication _localAuth = LocalAuthentication();


  static const MethodChannel _legacyChannel = MethodChannel('sookht_man/legacy_security');

  Future<void> migrateLegacyAndroidPrefs() async {
    if (prefs.getBool('_legacy_security_migrated') == true) return;
    try {
      final raw = await _legacyChannel.invokeMethod<Map<dynamic, dynamic>>('readLegacySecurityPrefs');
      if (raw != null) {
        final legacy = Map<String, dynamic>.from(raw);
        if (prefs.getString(_pinHashKey) == null && legacy['pin_hash'] is String) {
          await prefs.setString(_pinHashKey, legacy['pin_hash'] as String);
        }
        if (!prefs.containsKey(_pinEnabledKey) && legacy['pin_enabled'] is bool) {
          await prefs.setBool(_pinEnabledKey, legacy['pin_enabled'] as bool);
        }
        if (!prefs.containsKey(_bioEnabledKey) && legacy['biometric_enabled'] is bool) {
          await prefs.setBool(_bioEnabledKey, legacy['biometric_enabled'] as bool);
        }
        if (!prefs.containsKey(_tutorialKey) && legacy['tutorial_seen'] is bool) {
          await prefs.setBool(_tutorialKey, legacy['tutorial_seen'] as bool);
        }
        if (!prefs.containsKey(_themeKey) && legacy['selected_theme_color'] is String) {
          final theme = switch ((legacy['selected_theme_color'] as String).toUpperCase()) {
            'RED' => 'red',
            'PURPLE' => 'purple',
            'EMERALD' => 'emerald',
            _ => 'skyBlue',
          };
          await prefs.setString(_themeKey, theme);
        }
        if (!prefs.containsKey(_darkModeKey) && legacy['dark_mode_pref'] is String) {
          final dark = switch ((legacy['dark_mode_pref'] as String).toUpperCase()) {
            'LIGHT' => 'light',
            'DARK' => 'dark',
            _ => 'system',
          };
          await prefs.setString(_darkModeKey, dark);
        }
      }
    } catch (_) {
      // The channel is only present on Android. A missing legacy store is fine.
    } finally {
      await prefs.setBool('_legacy_security_migrated', true);
    }
  }

  bool get isPinEnabled => prefs.getBool(_pinEnabledKey) ?? false;
  bool get isBiometricEnabled => prefs.getBool(_bioEnabledKey) ?? false;
  bool get tutorialSeen => prefs.getBool(_tutorialKey) ?? false;
  String get themeColor => prefs.getString(_themeKey) ?? AppConfig.defaultThemeColor;
  String get darkMode => prefs.getString(_darkModeKey) ?? AppConfig.defaultDarkMode;

  Future<void> setPin(String pin) async {
    await prefs.setString(_pinHashKey, _hash(pin));
    await prefs.setBool(_pinEnabledKey, true);
  }

  Future<void> removePin() async {
    await prefs.remove(_pinHashKey);
    await prefs.setBool(_pinEnabledKey, false);
    await prefs.setBool(_bioEnabledKey, false);
  }

  bool verifyPin(String input) {
    final stored = prefs.getString(_pinHashKey);
    if (stored == null) return true;
    return stored == _hash(input);
  }

  Future<bool> canUseBiometric() async {
    try {
      return await _localAuth.canCheckBiometrics || await _localAuth.isDeviceSupported();
    } catch (_) {
      return false;
    }
  }

  Future<bool> authenticate() async {
    try {
      return await _localAuth.authenticate(
        localizedReason: 'برای ورود امن به برنامه احراز هویت کنید',
        persistAcrossBackgrounding: true,
      );
    } catch (_) {
      return false;
    }
  }

  Future<void> setBiometricEnabled(bool enabled) async => prefs.setBool(_bioEnabledKey, enabled);
  Future<void> setTutorialSeen(bool value) async => prefs.setBool(_tutorialKey, value);
  Future<void> setThemeColor(String value) async => prefs.setString(_themeKey, value);
  Future<void> setDarkMode(String value) async => prefs.setString(_darkModeKey, value);

  String _hash(String pin) => sha256.convert(utf8.encode(pin + AppConfig.securitySalt)).toString();
}
