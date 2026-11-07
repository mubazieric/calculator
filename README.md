# IWAT Scientific Calculator

Native Android scientific calculator with a textured charcoal keypad, lavender function controls, amber accents and a high-contrast display. Source repository: [mubazieric/calculator](https://github.com/mubazieric/calculator).

## Features

- Scientific arithmetic: precedence, parentheses, powers and roots, fractions as division, factorial, permutations/combinations, constants π and e, inverse and hyperbolic trig, logs, absolute value, scientific notation, DEG/RAD and answer recall.
- MODE tools: numerical derivative, definite integral and two-sided limit; bracketed root finder, quadratic roots (including complex roots), 2- and 3-variable linear systems; descriptive statistics and function tables; 2×2/3×3 matrix determinant, inverse and multiplication; vector dot, cross and magnitude; complex +/−/×/÷; polar/rectangular and binary/octal/decimal/hex conversion; normal PDF/CDF, binomial and Poisson probabilities; GCD/LCM, floor/ceil/mod/cot, random values and scientific constants. Numeric calculus uses `x` as the variable and evaluates expressions locally.
- Decimal/fraction display toggle, engineering notation, optional advanced keypad layer and separate memory controls.
- Calculator display and keypad fit the screen without scrolling in portrait. Tap **2nd** to access inverse/advanced scientific functions and **1st** to return; converter, history and scan remain scrollable where needed.
- Memory controls (M+, M−, MR, MC), live preview and locally saved calculation history.
- Dedicated converter: length, mass, temperature, area, volume, time, speed and data.
- Photo-to-expression: full-size camera image, on-device ML Kit Latin text recognition, editable extraction and explicit solve. No server, API key or image upload.

## Build

Open the repository root in Android Studio and run the `app` module, or from the repository root run `gradlew.bat :app:assembleDebug` on Windows (`./gradlew :app:assembleDebug` on macOS/Linux). Requires Android SDK 36, Java 17+ and internet for first-time dependencies. Debug APK: `app/build/outputs/apk/debug/app-debug.apk`. Minimum Android version: 7.0 (API 24); target: Android 16 (API 36).

Run `gradlew.bat :app:testDebugUnitTest :app:lintDebug` (or `./gradlew` on macOS/Linux) for arithmetic regression tests and Android lint checks. The calculation keypad switches between **1st** and **2nd** function layers to keep buttons comfortably sized without scrolling. On Android 11+, the keypad temporarily hides while the system keyboard is open.

## Scope notes

This is not a complete hardware fx-991 emulator. Calculus and limits are numerical (not symbolic); matrix support is limited to 2×2/3×3, vector support to 2D/3D, and statistics to descriptive measures. Advanced distribution families, large matrix systems, symbolic algebra, spreadsheet-style tables, and arbitrary complex-expression parsing are not yet implemented. Photo recognition works best for clear printed expressions; it may misread handwriting or multi-line problems. Always review extracted text before solving. No cloud generative-AI service is included; ML Kit provides on-device machine-learning OCR.

The arithmetic engine rejects undefined results and limits factorial/permutation inputs to nonnegative integers up to 170. History and memory are stored only on the device.
