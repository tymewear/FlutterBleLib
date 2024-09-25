import 'package:flutter/material.dart';

class ButtonView extends StatelessWidget {
  final String _text;
  final void Function()? action;

  ButtonView(this._text, {this.action});

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.all(2.0),
        color: Colors.blue,
        child: ElevatedButton(
          child: Text(
            _text,
            style: Theme.of(context)
                .textTheme
                .bodyMedium
                ?.copyWith(color: Colors.white),
          ),
          onPressed: action,
        ),
      ),
    );
  }
}
