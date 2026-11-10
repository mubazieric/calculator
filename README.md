# IWAT Scientific Calculator

Native Android scientific calculator with a textured charcoal keypad, lavender function controls, amber accents and a high-contrast display. Source repository: [mubazieric/calculator](https://github.com/mubazieric/calculator).

## Features

- Scientific arithmetic: precedence, parentheses, powers and roots, fractions as division, factorial, permutations/combinations, constants π and e, inverse and hyperbolic trig, logs, absolute value, scientific notation, DEG/RAD and answer recall.
- MODE tools: numerical derivative, definite integral and two-sided limit; symbolic differentiation/integration of polynomials through degree 8; bracketed root finder, quadratic roots (including complex roots), 2- to 6-variable linear systems; descriptive statistics and function tables; 2×2 through 6×6 matrix determinant, inverse and multiplication; vector dot, cross and magnitude; complex +/−/×/÷; polar/rectangular and binary/octal/decimal/hex conversion; normal PDF/CDF, binomial/Poisson PMF and CDF, hypergeometric PMF; GCD/LCM, floor/ceil/mod/cot, random values and scientific constants.
- Decimal/fraction display toggle, engineering notation, optional advanced keypad layer and separate memory controls.
- Calculator display and keypad fit the screen without scrolling in portrait. Tap **2nd** to access inverse/advanced scientific functions and **1st** to return; converter, history and scan remain scrollable where needed.
- Memory controls (M+, M−, MR, MC), live preview and locally saved calculation history.
- Dedicated converter: length, mass, temperature, area, volume, time, speed and data.
- Photo-to-expression: full-size camera image, on-device ML Kit Latin text recognition, editable extraction and explicit solve. No server, API key or image upload.
- Optional **IWAT AI Plus**: Play Billing monthly subscription with up to 50 cloud-AI photo analyses per UTC calendar month, backend Play subscription verification, capped usage and server-side AI key. Free on-device OCR remains available. The AI purchase UI stays disabled until a deployed HTTPS server is configured. See [backend/README.md](backend/README.md) for deployment and proposed pricing; this source checkout does not include a running cloud service.
- Contact screen: validated name, email, subject and message fields open a prefilled email to **ericmubazi2010@gmail.com**. The user must review and tap Send in an installed email app; the calculator does not transmit messages itself.
- Google AdMob banner integration on non-calculator screens only, so the keypad stays visible. Google UMP checks consent before ads; Contact provides a privacy-options entry point where required. Debug builds use Google test IDs. Release builds do not request ads until real IDs are configured.

## Build

Open the repository root in Android Studio and run the `app` module, or from the repository root run `gradlew.bat :app:assembleDebug` on Windows (`./gradlew :app:assembleDebug` on macOS/Linux). Requires Android SDK 36, Java 17+ and internet for first-time dependencies. Debug APK: `app/build/outputs/apk/debug/app-debug.apk`. Minimum Android version: 7.0 (API 24); target: Android 16 (API 36).

Run `gradlew.bat :app:testDebugUnitTest :app:lintDebug` (or `./gradlew` on macOS/Linux) for arithmetic regression tests and Android lint checks. See [QA.md](QA.md) for device checks that a build cannot verify. The calculation keypad switches between **1st** and **2nd** function layers to keep buttons comfortably sized without scrolling. On Android 11+, the keypad temporarily hides while the system keyboard is open.

## Scope notes

This is not a complete hardware fx-991 emulator. Numerical calculus and limits support arbitrary expressions in `x`; **symbolic** calculus supports only polynomials up to degree 8 with `+`, `−`, `×`, parentheses, nonnegative integer powers and division by constants. Matrix systems are limited to 6×6, vectors to 2D/3D, and statistics to descriptive measures. Arbitrary complex-expression parsing and spreadsheet-style tables are not implemented. Photo recognition works best for clear printed expressions; it may misread handwriting or multi-line problems. Always review extracted text before solving. No cloud generative-AI service is included; ML Kit provides on-device machine-learning OCR.

The arithmetic engine rejects undefined results and limits factorial/permutation inputs to nonnegative integers up to 170. History and memory are stored only on the device.

## Advertising and email setup

Debug APKs display **test ads only**. The app ID and banner unit in debug are Google's published demo IDs. Ads never appear over the calculator keypad. For a production release, create your own AdMob app and banner unit, publish/review [PRIVACY.md](PRIVACY.md) as a public privacy-policy page, configure a consent message in AdMob Privacy & messaging, then set `IWAT_ADMOB_APP_ID=ca-app-pub-...~...` and `IWAT_ADMOB_BANNER_ID=ca-app-pub-.../...` in your private `~/.gradle/gradle.properties` (or as `-P` flags). The release build disables ad requests if either ID is missing. Follow Google Play's data-safety and ads-disclosure requirements before publishing. Do not click your own live ads.

Contact uses an Android email intent, not an SMTP password or backend. An installed email app and the user's final Send action are required for delivery; without an email app, the address remains visible for manual contact. No email credentials are bundled in the APK.
