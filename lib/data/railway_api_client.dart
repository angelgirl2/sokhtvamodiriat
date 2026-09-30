import 'dart:convert';

import 'package:http/http.dart' as http;

/// Public API client for the app's Railway backend.
///
/// This class deliberately never receives provider or Bale secrets. Only the
/// backend knows those values. Multiple public base URLs can be supplied so the
/// app can try a custom domain and the Railway domain in sequence.
class RailwayApiClient {
  RailwayApiClient({
    required List<String> baseUrls,
    http.Client? client,
  })  : baseUrls = _normalize(baseUrls),
        _client = client ?? http.Client();

  final List<String> baseUrls;
  final http.Client _client;

  static List<String> _normalize(List<String> urls) {
    final values = <String>[];
    for (final raw in urls) {
      final value = raw.trim().replaceAll(RegExp(r'/+$'), '');
      if (value.isEmpty || value.contains('YOUR-') || value.contains('example.com')) {
        continue;
      }
      if (!value.startsWith('http://') && !value.startsWith('https://')) {
        continue;
      }
      if (!values.contains(value)) values.add(value);
    }
    return List.unmodifiable(values);
  }

  Future<http.Response?> getJson(
    String path, {
    Duration timeout = const Duration(seconds: 12),
    Map<String, String> headers = const {},
  }) async {
    for (final base in baseUrls) {
      try {
        final response = await _client
            .get(
              Uri.parse('$base$path'),
              headers: {
                'Accept': 'application/json',
                ...headers,
              },
            )
            .timeout(timeout);
        if (response.statusCode >= 200 && response.statusCode < 500) {
          return response;
        }
      } catch (_) {
        // Try the next public endpoint.
      }
    }
    return null;
  }

  Future<http.Response?> postJson(
    String path,
    Map<String, dynamic> body, {
    Duration timeout = const Duration(seconds: 15),
    Map<String, String> headers = const {},
  }) async {
    for (final base in baseUrls) {
      try {
        final response = await _client
            .post(
              Uri.parse('$base$path'),
              headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json; charset=utf-8',
                ...headers,
              },
              body: jsonEncode(body),
            )
            .timeout(timeout);
        if (response.statusCode >= 200 && response.statusCode < 500) {
          return response;
        }
      } catch (_) {
        // Try the next public endpoint.
      }
    }
    return null;
  }

  Future<bool> isReachable() async {
    final response = await getJson('/api/health', timeout: const Duration(seconds: 6));
    return response?.statusCode == 200;
  }

  Future<Map<String, dynamic>?> getObject(
    String path, {
    Map<String, String> headers = const {},
  }) async {
    final response = await getJson(path, headers: headers);
    if (response == null || response.statusCode != 200 || response.body.isEmpty) return null;
    try {
      return jsonDecode(response.body) as Map<String, dynamic>;
    } catch (_) {
      return null;
    }
  }

  Future<Map<String, dynamic>?> postObject(
    String path,
    Map<String, dynamic> body, {
    Map<String, String> headers = const {},
  }) async {
    final response = await postJson(path, body, headers: headers);
    if (response == null || response.body.isEmpty) return null;
    try {
      return jsonDecode(response.body) as Map<String, dynamic>;
    } catch (_) {
      return null;
    }
  }
}
