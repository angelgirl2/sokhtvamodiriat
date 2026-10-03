import 'package:flutter/material.dart';

class IranianPlate extends StatelessWidget {
  const IranianPlate({super.key, required this.first2, required this.letter, required this.last3, required this.city});
  final String first2;
  final String letter;
  final String last3;
  final String city;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 7),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(9),
        border: Border.all(color: Colors.black.withValues(alpha: .12)),
        boxShadow: [BoxShadow(color: Colors.black.withValues(alpha: .07), blurRadius: 7, offset: const Offset(0, 3))],
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(width: 16, height: 28, color: const Color(0xFF1677C8)),
          const SizedBox(width: 7),
          Text(first2, style: const TextStyle(color: Colors.black, fontWeight: FontWeight.w900, fontSize: 16)),
          const SizedBox(width: 6),
          Text(letter, style: const TextStyle(color: Colors.black, fontWeight: FontWeight.w900, fontSize: 16)),
          const SizedBox(width: 6),
          Text(last3, style: const TextStyle(color: Colors.black, fontWeight: FontWeight.w900, fontSize: 16)),
          const SizedBox(width: 8),
          Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Text('IR', style: TextStyle(fontSize: 7, color: Colors.black, fontWeight: FontWeight.bold)),
              Text(city, style: const TextStyle(fontSize: 9, color: Colors.black, fontWeight: FontWeight.w800)),
            ],
          ),
        ],
      ),
    );
  }
}
