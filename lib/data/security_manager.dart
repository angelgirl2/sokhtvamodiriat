import 'dart:convert';
import 'dart:math';

import 'package:crypto/crypto.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../core/app_colors.dart';

/// Persists security, theme and integration settings.
///
/// Port of the Kotlin `SecurityManager` (SharedPreferences -> SharedPreferences).
class SecurityManager {
  SecurityManager(this._prefs);

  final SharedPreferences _prefs;

  static const _keyPinHash = 'pin_hash';
  static const _keyPinEnabled = 'pin_enabled';
  static const _keyBiometricEnabled = 'biometric_enabled';
  static const _keyTutorialSeen = 'tutorial_seen';
  static const _keyRailwayUrl = 'railway_url';
  static const _keyThemeColor = 'selected_theme_color';
  static const _keyDarkModePref = 'dark_mode_pref';
  static const _keyDeviceId = 'railway_device_id';
  static const _keyCloudSyncDirty = 'railway_cloud_sync_dirty';
  static const _salt = 'AngelGirlBrandFuelSecuritySalt#2026';


  bool isAppLocked = false;

  static Future<SecurityManager> create() async {
    final prefs = await SharedPreferences.getInstance();
    final manager = SecurityManager(prefs);
    manager.isAppLocked = manager.isPinEnabled;
    return manager;
  }

  // --- Theme -----------------------------------------------------------------
  AppThemeColor getSelectedThemeColor() {
    final name = _prefs.getString(_keyThemeColor);
    return AppThemeColor.values.firstWhere(
      (e) => e.name == name,
      orElse: () => AppThemeColor.skyBlue,
    );
  }

  Future<void> setSelectedThemeColor(AppThemeColor theme) =>
      _prefs.setString(_keyThemeColor, theme.name);

  DarkModePref getDarkModePref() {
    final name = _prefs.getString(_keyDarkModePref);
    return DarkModePref.values.firstWhere(
      (e) => e.name == name,
      orElse: () => DarkModePref.system,
    );
  }

  Future<void> setDarkModePref(DarkModePref pref) =>
      _prefs.setString(_keyDarkModePref, pref.name);

  // --- Lock / biometrics -----------------------------------------------------
  bool get isPinEnabled => _prefs.getBool(_keyPinEnabled) ?? false;

  bool get isBiometricEnabled => _prefs.getBool(_keyBiometricEnabled) ?? false;

  Future<void> setBiometricEnabled(bool enabled) =>
      _prefs.setBool(_keyBiometricEnabled, enabled);

  // --- First-run guide -------------------------------------------------------
  /// The onboarding guide is shown exactly once: this flag is set the first time
  /// it is dismissed and never cleared afterwards.
  bool get isTutorialSeen => _prefs.getBool(_keyTutorialSeen) ?? false;

  Future<void> setTutorialSeen(bool seen) => _prefs.setBool(_keyTutorialSeen, seen);

  // --- Integrations ----------------------------------------------------------
  static const _compilePrimaryRailwayUrl = String.fromEnvironment(
    'SOKHT_API_PRIMARY_URL',
    defaultValue: 'https://sokhtvamodiriat-production.up.railway.app',
  );
  static const _compileFallbackRailwayUrl = String.fromEnvironment(
    'SOKHT_API_FALLBACK_URL',
    defaultValue: '',
  );

  List<String> getRailwayUrls() {
    final storedUrl = _prefs.getString(_keyRailwayUrl)?.trim() ?? '';
    final normalizedStoredUrl = storedUrl == 'https://fuel-management-production.up.railway.app'
        ? _compilePrimaryRailwayUrl
        : storedUrl;
    final urls = <String>[
      normalizedStoredUrl,
      _compilePrimaryRailwayUrl,
      _compileFallbackRailwayUrl,
    ];
    final seen = <String>{};
    return urls
        .map((value) => value.trim().replaceAll(RegExp(r'/+$'), ''))
        .where((value) => value.isNotEmpty && seen.add(value))
        .toList(growable: false);
  }

  String getRailwayUrl() => getRailwayUrls().first;

  Future<void> setRailwayUrl(String url) => _prefs.setString(_keyRailwayUrl, url.trim());


  // --- Railway cloud sync identity ------------------------------------------
  // This is a random per-installation bearer identifier, not a provider secret.
  // It lets the server keep one private snapshot per installed copy without
  // requiring a login screen.
  String getDeviceId() {
    final existing = _prefs.getString(_keyDeviceId)?.trim();
    if (existing != null && RegExp(r'^[a-f0-9]{64}$').hasMatch(existing)) {
      return existing;
    }
    final random = Random.secure();
    final value = List<int>.generate(32, (_) => random.nextInt(256));
    final id = value.map((byte) => byte.toRadixString(16).padLeft(2, '0')).join();
    _prefs.setString(_keyDeviceId, id);
    return id;
  }

  bool get isCloudSyncDirty => _prefs.getBool(_keyCloudSyncDirty) ?? false;

  Future<void> markCloudSyncDirty() => _prefs.setBool(_keyCloudSyncDirty, true);

  Future<void> markCloudSyncComplete() => _prefs.setBool(_keyCloudSyncDirty, false);

  // --- PIN -------------------------------------------------------------------
  Future<void> setPin(String pin) async {
    await _prefs.setString(_keyPinHash, _hashPin(pin));
    await _prefs.setBool(_keyPinEnabled, true);
    isAppLocked = false;
  }

  Future<void> removePin() async {
    await _prefs.remove(_keyPinHash);
    await _prefs.setBool(_keyPinEnabled, false);
    await _prefs.setBool(_keyBiometricEnabled, false);
    isAppLocked = false;
  }

  bool verifyPin(String inputPin) {
    final storedHash = _prefs.getString(_keyPinHash);
    if (storedHash == null) return true;
    final matches = storedHash == _hashPin(inputPin);
    if (matches) isAppLocked = false;
    return matches;
  }

  void unlockLocally() => isAppLocked = false;

  String _hashPin(String pin) =>
      sha256.convert(utf8.encode('$pin$_salt')).toString();

  /// Random reference generator used for simulated bank/admin references.
  static String randomRef(String prefix) {
    final value = 10000000 + Random().nextInt(90000000);
    return '$prefix-$value';
  }
}
