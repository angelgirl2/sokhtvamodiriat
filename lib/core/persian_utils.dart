import 'package:intl/intl.dart';

/// Persian (Farsi) digit + number/date formatting helpers used across the app.
class PersianUtils {
  PersianUtils._();

  static const _faDigits = <String>['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];

  /// Converts every ASCII digit in [input] to its Persian counterpart.
  static String toFa(String input) {
    final buffer = StringBuffer();
    for (final rune in input.runes) {
      if (rune >= 0x30 && rune <= 0x39) {
        buffer.write(_faDigits[rune - 0x30]);
      } else {
        buffer.writeCharCode(rune);
      }
    }
    return buffer.toString();
  }

  static final NumberFormat _grouped = NumberFormat('#,##0', 'en_US');

  /// `1234567` -> `۱,۲۳۴,۵۶۷`
  static String number(num value) => toFa(_grouped.format(value));

  /// Alias used for currency amounts (Tomans).
  static String money(num value) => number(value);

  /// Compatibility alias used by the UI for currency values.
  static String withPrice(num value) => money(value);

  /// Formats a decimal value with Persian digits.
  static String decimal(num value) => toFa(value.toStringAsFixed(value == value.roundToDouble() ? 0 : 1));

  /// Formats an epoch-millisecond timestamp as a date.
  static String dateFromMillis(int millis) => date(DateTime.fromMillisecondsSinceEpoch(millis));

  /// `12.34` -> `۱۲.۳`
  static String liters(double value) => toFa(value.toStringAsFixed(1));

  static String date(DateTime value) => toFa(DateFormat('yyyy/MM/dd').format(value));

  static String dateTime(DateTime value) =>
      toFa(DateFormat('yyyy/MM/dd - HH:mm').format(value));

  /// Persian month names indexed by Gregorian month index (0 = January),
  /// matching `getPersianMonthName()` in the original FuelConsumptionChart.kt.
  static const persianMonths = <String>[
    'فروردین',
    'اردیبهشت',
    'خرداد',
    'تیر',
    'مرداد',
    'شهریور',
    'مهر',
    'آبان',
    'آذر',
    'دی',
    'بهمن',
    'اسفند',
  ];

  static const gregorianMonths = <String>[
    'ژانویه',
    'فوریه',
    'مارس',
    'آوریل',
    'مه',
    'ژوئن',
    'ژوئیه',
    'اوت',
    'سپتامبر',
    'اکتبر',
    'نوامبر',
    'دسامبر',
  ];

  static String monthLabel(int gregorianMonthIndex) {
    if (gregorianMonthIndex < 0 || gregorianMonthIndex > 11) {
      return 'ماه ${toFa('${gregorianMonthIndex + 1}')}';
    }
    return '${persianMonths[gregorianMonthIndex]} (${gregorianMonths[gregorianMonthIndex]})';
  }

  /// Days remaining until [targetMillis] (negative when already expired).
  static int daysUntil(int targetMillis) =>
      ((targetMillis - DateTime.now().millisecondsSinceEpoch) /
              Duration.millisecondsPerDay)
          .floor();

  /// Formats a day count with Persian digits and a +/- suffix.
  static String daysLabel(int days) => toFa('$days');
}
