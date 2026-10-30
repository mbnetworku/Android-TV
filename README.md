# Android-TV

A clean, Apple TV-style home screen for an Android TV, set up and tested on a **Skyworth SWTV-21AE**
(Android TV 14, firmware UKNV.260824.001, MediaTek, about 0.9 GB RAM).

It is built on [Clean Your TV](https://github.com/Gathin23/cleanyourtv) by Gathin (MIT). That project
debloats a TV over ADB and installs a light launcher (`com.gathin.tvhome`). This repository adds:

| | |
|---|---|
| **Settings app** | A full-screen, translucent Settings screen in the style of tvOS: a large gear and the TV's model on a glass panel to the left, and one scrolling list on a glass panel to the right. Every row has an icon and its current state (network, IP address, resolution, volume, app count, free storage, free memory, time, language, model) and opens the TV's own page for it, so a page that misbehaves in the TV's Settings app is still one click away. |
| **Settings in the dock** | The TV's Settings tile in the dock opens this Settings app; so does the Settings pill at the top of Home. |
| **Rounder look** | Tiles, panels, rows and the input menu have larger corner radii, closer to Apple TV. |
| **nature-calm theme** | Twelve calm nature photos from Wikimedia Commons, changing every five minutes, a frosted-glass dock and round icon-only buttons, with `tile_menu` on (hold OK on a tile to move it left or right). Photos are CC BY / CC BY-SA: see `themes/nature-calm/CREDITS.md`. |

The original project's README is kept in [docs/CLEANYOURTV-README.md](docs/CLEANYOURTV-README.md); it covers
debloat lists, the other themes, add-ons and troubleshooting.

## What changed in the launcher

| File | Change |
|---|---|
| `launcher/app/src/main/java/com/gathin/tvhome/SettingsHubActivity.java` | New: the Settings app |
| `launcher/app/src/main/java/com/gathin/tvhome/AppAdapter.java` | The `com.android.tv.settings` tile opens the Settings app; tile corner radius 16dp → 26dp |
| `launcher/app/src/main/java/com/gathin/tvhome/MainActivity.java` | The Settings pill opens the Settings app (unless `launcher_settings_button` is on) |
| `launcher/app/src/main/java/com/gathin/tvhome/QuickSettingsActivity.java` | More rows and a scrolling list in the remote's Settings-button panel (`settings_key_panel`) |
| `launcher/app/src/main/AndroidManifest.xml` | Registers `SettingsHubActivity` (translucent, not exported) |
| `launcher/app/src/main/res/values/strings.xml` | English labels for the new rows (`translatable="false"`) |
| `launcher/app/src/main/res/layout/activity_quick_settings.xml` | The panel's rows in a `ScrollView` |
| `launcher/app/src/main/res/drawable/*.xml` | Rounder `tile_bg`, `glass_panel`, `input_row_bg`, `settings_choice_bg` |
| `themes/nature-calm/` | The theme, with `dock_apps` for this TV and the Settings tile last |

The release APK is about 540 KB, under the original project's 576 KB limit. The launcher still has no
`INTERNET` permission.

## Build and install (Windows)

You need JDK 17, the Android SDK (platform 34, build-tools 34.0.0) and Python 3, with USB debugging on
the TV. Replace `<tv ip>` with the TV's address.

```powershell
python -m venv tools\venv
tools\venv\Scripts\python.exe -m pip install -r tools\requirements.txt
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.21.21-hotspot"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
launcher\build-and-install.ps1 -TvHost <tv ip>
tools\theme.ps1 -TvHost <tv ip> themes\nature-calm
```

`build-and-install.ps1` installs the launcher, makes it Home and only then disables the stock launcher;
if Home did not move it re-enables the stock launcher and stops.

For a rebuild after a code change:

```powershell
cd launcher
.\gradlew.bat assembleRelease
adb -s <tv ip>:5555 install -r app\build\outputs\apk\release\app-release.apk
adb -s <tv ip>:5555 shell cmd package set-home-activity --user 0 com.gathin.tvhome/.MainActivity
```

## Undo

```powershell
tools\theme.ps1 -TvHost <tv ip> -Reset                                  # remove the theme
adb -s <tv ip>:5555 shell pm enable com.google.android.tvlauncher       # stock home back
adb -s <tv ip>:5555 shell pm enable com.google.android.tvrecommendations
```

Every app on this TV was **disabled, not uninstalled** (`pm disable-user --user 0`); `pm enable <package>`
brings any of them back.

## Debloat on this TV

Disabled: Skyworth preloads and ads (`com.cltv.fast`, `com.smartdevice.recommendation`,
`com.smartdevice.leanbacklauncher.customizer`, `fusion.android.tv.demo`, `com.Smartdevice.emanual`),
telemetry (`com.google.android.feedback`, `com.android.tv.feedbackconsent`), unused apps (Play Games,
YouTube Music, Prime Video, TalkBack, `com.google.android.backdrop`), phone-only services (Health Connect,
federated compute, on-device personalisation, ad services), the stock launcher and its recommendations,
and both setup wizards (`com.google.android.tungsten.setupwraith`, `com.mediatek.wwtv.setupwizard`).

Kept on purpose: Live TV and HDMI (`com.mediatek.wwtv.tvcenter`, `com.mediatek.tvinput`,
`com.mediatek.tv.service`), remote pairing and keys (`com.mediatek.autopair`,
`com.smartdevice.global.service`), Play Store, Play Services, keyboards, Assistant, Chromecast, Dolby audio.

Known: some disabled apps (`com.cltv.fast`, `com.google.android.backdrop`) re-enable themselves after a
reboot on this firmware, and the TV's load average stays high (about 50 on 4 cores) with the CPU mostly
idle, which the debloat did not change.

## Licence

MIT, as the original project: see [LICENSE](LICENSE). Photos and fonts have their own terms (see
`themes/nature-calm/CREDITS.md` and the font licence files).
