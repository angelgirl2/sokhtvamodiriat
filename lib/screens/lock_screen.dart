import 'package:flutter/material.dart';

import '../app_controller.dart';
import '../core/theme.dart';
import '../widgets/logo.dart';

class LockScreen extends StatefulWidget {
  const LockScreen({super.key, required this.controller});
  final AppController controller;
  @override
  State<LockScreen> createState() => _LockScreenState();
}

class _LockScreenState extends State<LockScreen> with SingleTickerProviderStateMixin {
  final List<String> digits = [];
  String error = '';
  late final AnimationController shake;

  @override
  void initState() {
    super.initState();
    shake = AnimationController(vsync: this, duration: const Duration(milliseconds: 260));
  }

  @override
  void dispose() {
    shake.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (digits.length < 4) return;
    final ok = await widget.controller.unlockWithPin(digits.join());
    if (!ok) {
      error = 'رمز واردشده صحیح نیست';
      shake.forward(from: 0);
      digits.clear();
      setState(() {});
    }
  }

  @override
  Widget build(BuildContext context) {
    final canBio = widget.controller.security.isBiometricEnabled;
    return Scaffold(
      body: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(colors: [Theme.of(context).colorScheme.primary.withValues(alpha: .14), Colors.transparent, AppTheme.purple.withValues(alpha: .08)], begin: Alignment.topRight, end: Alignment.bottomLeft),
        ),
        child: SafeArea(
          child: Center(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 28),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const AppLogo(size: 116),
                  const SizedBox(height: 22),
                  Text('برنامه قفل است', style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w900)),
                  const SizedBox(height: 8),
                  Text('رمز عبور را وارد کنید', style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Theme.of(context).colorScheme.onSurfaceVariant)),
                  const SizedBox(height: 24),
                  AnimatedBuilder(
                    animation: shake,
                    builder: (context, child) => Transform.translate(offset: Offset((shake.value < .5 ? shake.value : 1 - shake.value) * 16 * (shake.value < .5 ? 1 : -1), 0), child: child),
                    child: Row(mainAxisAlignment: MainAxisAlignment.center, children: List.generate(6, (i) => AnimatedContainer(duration: AppMotion.fast, width: 13, height: 13, margin: const EdgeInsets.all(7), decoration: BoxDecoration(shape: BoxShape.circle, color: i < digits.length ? Theme.of(context).colorScheme.primary : Theme.of(context).colorScheme.onSurface.withValues(alpha: .14))))),
                  ),
                  const SizedBox(height: 8),
                  SizedBox(height: 24, child: Text(error, style: TextStyle(color: Theme.of(context).colorScheme.error, fontWeight: FontWeight.w700))),
                  const SizedBox(height: 20),
                  SizedBox(
                    width: 320,
                    child: GridView.count(
                      shrinkWrap: true,
                      crossAxisCount: 3,
                      mainAxisSpacing: 12,
                      crossAxisSpacing: 12,
                      childAspectRatio: 1.32,
                      children: [
                        for (final d in ['1','2','3','4','5','6','7','8','9','⌫','0','✓'])
                          _KeyButton(label: d, onTap: () {
                            if (d == '⌫') {
                              if (digits.isNotEmpty) digits.removeLast();
                            } else if (d == '✓') {
                              _submit();
                            } else if (digits.length < 6) {
                              digits.add(d);
                            }
                            setState(() {});
                          }),
                      ],
                    ),
                  ),
                  if (canBio) ...[
                    const SizedBox(height: 18),
                    TextButton.icon(onPressed: widget.controller.unlockBiometric, icon: const Icon(Icons.fingerprint_rounded), label: const Text('ورود با اثر انگشت')),
                  ],
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _KeyButton extends StatelessWidget {
  const _KeyButton({required this.label, required this.onTap});
  final String label;
  final VoidCallback onTap;
  @override
  Widget build(BuildContext context) {
    return Material(
      color: Theme.of(context).colorScheme.surface.withValues(alpha: .8),
      borderRadius: BorderRadius.circular(20),
      child: InkWell(
        borderRadius: BorderRadius.circular(20),
        onTap: onTap,
        child: Center(child: Text(label, style: TextStyle(fontSize: label == '✓' ? 22 : 21, fontWeight: FontWeight.w900, color: Theme.of(context).colorScheme.onSurface))),
      ),
    );
  }
}
