# Desert Ledger — Android app

This project opens your existing Income & Expenses Apps Script website inside an Android WebView.
The website owns the Jaisalmer luxury theme, forms, admin login, member choices, records, edit/view/delete,
and Google Sheet connection. The Android app adds an icon, loading bar, network error/retry page,
options menu, browser fallback, and native JavaScript confirmation dialogs for Delete.

**This ZIP contains source code, not a compiled APK. No Android SDK or Gradle was available in the
creation environment. Compilation, Android lint, and physical-device testing have not run.**

## Website already configured

https://script.google.com/macros/s/AKfycbxVk-ghCoZW2GYFriYEZum9Zl3rNPvC5fmOCOaDZxckr4MdGUfv2qfKR0l-7ScGwa0q/exec

Open this link in Chrome on your phone first. The app displays the currently deployed website;
if View/Delete are missing, update BOTH Code.gs and Index.html in Apps Script and deploy a new
version on the SAME deployment. The app does not independently add website features.
Your existing ADMIN_PASSWORD and Google Sheet remain in Apps Script, never in the Android source.
Internet is required. There is no offline submission queue or separate phone database.

## EASIEST: Get an APK using GitHub, without Android Studio

1. Create a new private GitHub repository (for example desert-ledger-android).
2. Extract this ZIP. Open the DesertLedgerAndroid folder.
3. Upload ALL CONTENTS of that folder into the repository ROOT. Include the `.github` folder.
   The repository root must contain settings.gradle.kts, app, and .github — not a nested
   DesertLedgerAndroid folder. A Git client is the most reliable way to include .github.
4. Open the repository's Actions tab. Enable workflows if GitHub asks.
5. Select **Build Android APK → Run workflow → Run workflow**. A push to main/master also builds.
6. Wait for a green completed run. Open it and scroll to **Artifacts**.
7. Download **Desert-Ledger-APK**. Extract the downloaded ZIP; inside is `app-debug.apk`.
8. Send that APK to your Android phone, tap it, and allow installation from the app opening it
   when Android asks. Install and open **Desert Ledger**.

The workflow is supplied but has NOT been run by the author. GitHub availability/usage limits are
controlled by your account. If the workflow fails, download/copy its error log for troubleshooting.

## Windows build (Android SDK required)

1. Install Java JDK 17 and Android Studio, including Android SDK Platform 35 and Build Tools 35.0.0.
   Accept SDK licenses through Android Studio's SDK Manager.
2. Extract this ZIP into a normal writable folder.
3. Open PowerShell in the DesertLedgerAndroid folder.
4. Run:

   powershell -ExecutionPolicy Bypass -File .\BUILD_APK_WINDOWS.ps1

   This uses an execution-policy override only for that PowerShell process; it does not change
   the machine policy. The script downloads Gradle 8.11.1 from its official distribution site,
   verifies its SHA-256, generates the Gradle wrapper, builds the APK and runs Android lint.
5. Find `Desert-Ledger.apk` in the project folder. Copy it to your phone and install.

If Android SDK uses a custom folder, set ANDROID_HOME to that folder before running the script.
The script needs internet to download Gradle and Android build dependencies. JDK 17 must be on PATH.
Once the wrapper has been generated, Android Studio can open and sync this project normally.
The source ZIP intentionally does not include a Gradle wrapper JAR; the Windows script generates it.

## Signing and updates

The default build is a debug-signed APK suitable for direct personal testing/installation.
Every fresh GitHub runner may use a DIFFERENT debug signing key. A later build may require uninstalling
the earlier app before installing. Uninstalling removes phone sessions, but not Google Sheet records.
For stable updates or Play distribution, generate and retain your own release signing key and use
Android Studio's Build → Generate Signed App Bundle or APK. Keep that keystore and passwords private.
Do not upload signing keys into the repository. Release signing is not preconfigured in this ZIP.

## Google login and deployment access

An Apps Script deployment accessible to your intended audience must already be active.
Google account sign-in may be blocked inside embedded WebViews. Use the app menu's
**Open in Chrome / Browser** option in that case; browser and app cookies are separate.
For a public-deployment workflow, set the web app audience to the permitted users you need in Apps Script.
Anyone permitted to open a public website can submit forms; the website's admin password protects
its read/edit/delete functions. This app does not add member authentication.
Only Google Script HTTPS navigation stays embedded; external links open in the system browser.
No native JavaScript bridge, storage permission, payment permission or certificate bypass is used.
Google-hosted iframe cookies are enabled because Apps Script renders the form inside an iframe.

## Device check after installing

- Android 8.0 or newer, internet connection and updated Android System WebView/Chrome required.
- Open Expenses, choose member and Cash/UPI, then submit a test entry.
- Submit an Income test with advance/due/extra amounts; verify the Google Sheet row.
- Log into Admin. Check separate customer/remark columns and View's five-field popup.
- Edit the test entry. Delete only that test entry and confirm the prompt.
- Test Back/Home buttons, keyboard, rotation, connection retry, and browser fallback.
- Use the website's Logout button to end an admin session.

The app menu's Clear website session clears only phone web data; saved Google Sheet records remain.
Form drafts can be lost on app closure, OS process termination or reload. Wait for saved confirmation.

## Technical versions

Kotlin 2.1.20; Android Gradle Plugin 8.9.2; Gradle 8.11.1; Java 17; compile/target SDK 35; minimum SDK 26.
AGP 8.9 compatibility reference: https://developer.android.com/build/releases/agp-8-9-0-release-notes
Source entry point: app/src/main/java/com/jaisalmer/desertledger/MainActivity.kt
Change WEB_APP_URL there only if you create a completely different deployment URL.
