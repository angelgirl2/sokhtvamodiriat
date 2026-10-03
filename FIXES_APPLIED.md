# SookhtMan Flutter fixes

This package is based on the uploaded `sookht.zip` source tree.

Applied compile-error fixes:
- Added `ServiceRequest.statusPending` and `ServiceRequest.statusApproved`.
- Extended `ServiceRequest.copyWith` to preserve/update IDs and timestamps.
- Added `Vehicle.effectiveInspectionExpiryMillis` compatibility getter.
- Fixed nullable request/inquiry IDs before SQLite updates.
- Fixed the legacy `LocalDatabase` Future<Database> query calls.
- Updated `flutter_local_notifications` calls to the named-argument API used by 22.3.x.
- Imported Cupertino transitions correctly in the theme.
- Disambiguated `dart:ui` TextDirection in `core/utils.dart`.
- Added the missing legacy `AppRepository.updateRequestStatus` bridge.
- Replaced deprecated `withOpacity` calls with `withValues(alpha: ...)` in app code.
- Updated PDF table helper calls in Settings screen.

The legacy Android sources remain in `legacy_android_original/` and the Railway server remains in `railway_server/`.
Additional alignment applied on 2026-10-03:
- Updated `AppState.approveRequest` to call the current named `AppRepository.updateRequestStatus` API and preserve the existing Bale message id.
- Rewrote legacy `LocalDatabase` list queries to await the `Database` instance explicitly before calling `query()`.
- Removed the redundant `dart:typed_data` import from `AppController`.
- Added `AppController.refresh()` and replaced direct external calls to protected `notifyListeners()`.
- Replaced deprecated `RadioListTile.groupValue/onChanged` usage with the `RadioGroup` API.
- Kept the current named-argument `flutter_local_notifications` 22.3.x API already present in the uploaded source.
