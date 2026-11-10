# IWAT device QA checklist

Automated: run `gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleRelease` (use `./gradlew` on macOS/Linux). The arithmetic, symbolic and advanced-math unit tests cover valid operations, singular matrices, undefined expressions and selected numerical edge cases. The unsigned release APK is **not** ready for store distribution; it must be signed after production configuration.

Manual checks still required on physical Android devices (preferably Android 7, 12, 15 and 16, including a small portrait screen):

1. Open Calculate and confirm all keys fit, 1st/2nd layers switch, and keyboard insets do not obscure results.
2. Check DEG/RAD, constants, fractional output, converter, history persistence and contact tab.
3. Test camera OCR with a printed expression, cancellation and a device without a camera app.
4. Check Contact form validation and that the email app receives the destination, subject and message; confirm nothing is sent until the user taps Send.
5. Check Google test banners after consent on non-calculator screens only, the privacy-options entry point where required, and a device with no Google Play services.
6. Before publishing: configure your own AdMob IDs and privacy message, review the privacy notice and Play Data safety form, sign a release build and test live integration using designated test devices—not by clicking your own production ads.
7. For AI Plus, configure a real Play monthly subscription and HTTPS backend, then test checkout, cancellation before expiration, grace period, pending payments, expiry, restore after reinstall, 50-request cap, repeated request IDs, image failures and refund behavior. Confirm the price in Play checkout and that free OCR never uploads an image. Without backend configuration, subscription buttons must be disabled. Check your provider dashboard for unexpected spend.

No automated build or lint run can guarantee the absence of bugs across all devices and third-party services.
