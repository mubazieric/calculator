# IWAT Scientific Calculator

Native Android scientific calculator with a dark, custom-designed interface. Source repository: [mubazieric/calculator](https://github.com/mubazieric/calculator).

## Features

- Scientific arithmetic: precedence, parentheses, powers and roots, fractions as division, factorial, permutations/combinations, constants π and e, inverse and hyperbolic trig, logs, absolute value, scientific notation, DEG/RAD and answer recall.
- Memory controls (M+, M−, MR, MC), live preview and locally saved calculation history.
- Dedicated converter: length, mass, temperature, area, volume, time, speed and data.
- Photo-to-expression: full-size camera image, on-device ML Kit Latin text recognition, editable extraction and explicit solve. No server, API key or image upload.

## Build

Open the repository root in Android Studio and run the `app` module, or from the repository root run `gradlew.bat :app:assembleDebug` on Windows (`./gradlew :app:assembleDebug` on macOS/Linux). Requires Android SDK 35, Java 17+ and internet for first-time dependencies. Debug APK: `app/build/outputs/apk/debug/app-debug.apk`. Minimum Android version: 7.0 (API 24).

## Scope notes

This is a working first release of the scientific arithmetic functions, not a complete hardware fx-991 emulator: matrix/vector algebra, complex-number mode, equation solving, statistics and programmable tables are not yet implemented. Photo recognition works best for clear printed expressions; it may misread handwriting or multi-line problems. Always review extracted text before solving. No cloud generative-AI service is included; ML Kit provides on-device machine-learning OCR.

The arithmetic engine rejects undefined results and limits factorial/permutation inputs to nonnegative integers up to 170. History and memory are stored only on the device.
