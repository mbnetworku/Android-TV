# CLAUDE.md

Guide for Claude Code in this repository. The upstream rules for every coding agent are in AGENTS.md,
imported at the end; this file adds what is specific to this fork and this TV.

## The TV

- Skyworth SWTV-21AE, Android TV 14 (SDK 34), firmware UKNV.260824.001, MediaTek, armeabi-v7a, about 0.9 GB RAM.
- ADB over TCP on port 5555 with USB debugging alone (no wireless-debugging pairing). The address changes
  with DHCP: ask for it, and never commit it.
- Stock home was `com.google.android.tvlauncher` (classic Android TV). Home is now `com.gathin.tvhome/.MainActivity`.
- Live TV screenshots come back blank (protected video layer); use
  `dumpsys activity activities | grep topResumedActivity` instead.
- `pm list packages -d` is the truth for what is disabled; some packages re-enable themselves after a reboot.

## This fork's changes

- `SettingsHubActivity` is the Apple TV-style Settings app: translucent window
  (`Theme.Translucent.NoTitleBar.Fullscreen`, so no `screenOrientation`), two `glass_panel` panels, rows built in
  code with `row(icon, title, value, intent)`. Every row stays visible; a page the TV lacks shows `not_available`.
  Values are read without new permissions (no Bluetooth state, no Wi-Fi SSID).
- `AppAdapter.launch()` sends the `com.android.tv.settings` tile to `SettingsHubActivity`; the Settings pill in
  `MainActivity` does the same unless `launcher_settings_button` is on.
- Corner radii were raised for a rounder look (tiles 26dp, `glass_panel` 28dp, rows 18–20dp).
- New strings are English only and `translatable="false"`, so release lint does not fail on missing translations.
- `themes/nature-calm/config.json` is this TV's theme: `dock_apps` lists only installed apps with
  `com.android.tv.settings` last, `tile_menu` on, `settings_key_panel` off.

## Build, install, check

```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
cd launcher; .\gradlew.bat assembleRelease -q
adb -s <tv ip>:5555 install -r app\build\outputs\apk\release\app-release.apk
adb -s <tv ip>:5555 shell cmd package set-home-activity --user 0 com.gathin.tvhome/.MainActivity
tools\theme.ps1 -TvHost <tv ip> themes\nature-calm
```

- `SettingsHubActivity` is not exported, so `am start` from the shell is refused: open it from the dock
  (Home, Down, Right to the last tile, OK) and check with a screenshot: `tools\tv.py --screenshot shots\x.png`.
- If Gradle fails with "file is being used by another process", run `.\gradlew.bat --stop` and build again.
- Keep the APK under 589,824 bytes and the launcher without `INTERNET`.

## Rules for this repo

- Disable, never uninstall. Back up package lists and settings before changing the TV.
- Never commit backups (`backup/`, `getprop`, settings dumps), the ADB key (`tools/adbkey*`), APKs, `tools/venv`,
  build output, `local.properties`, IP addresses or account emails.
- Keep the stock launcher enabled until the new one is confirmed as Home.

@AGENTS.md
