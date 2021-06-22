# fcitx5-android-poc

An attempt to run fcitx5 on Android.

## Project status

It can build, run, and print to logcat.

## Build

### Dependencies

- Android SDK Platform & Build-Tools 30
- Android NDK (Side by side) 23 & cmake 3.18.1, they can be installed using SDK Manager in Android Studio or `sdkmanager` command line. **Note:** you may need to install Android Studio Beta for Android NDK 23, or use `sdkmanager` from Android SDK Command-line Tools. NDK 21 & 22 are confirmed not working with this project.
- [KDE/extra-cmake-modules](https://github.com/KDE/extra-cmake-modules)

### `libime` data

I don't know why cmake won't download and generate those data. Just install [libime](https://archlinux.org/packages/community/x86_64/libime/), and copy `/usr/{lib,share}/libime/*` to `app/src/main/assets/fcitx5/libime/`.

### `fcitx5-chinese-addons` dict

Make it read dict path from environment variable, we need to specify that path at runtime.

```diff
diff --git a/im/pinyin/pinyin.cpp b/im/pinyin/pinyin.cpp
index 2f98f7f..1cceb7e 100644
--- a/im/pinyin/pinyin.cpp
+++ b/im/pinyin/pinyin.cpp
@@ -607,7 +607,7 @@ PinyinEngine::PinyinEngine(Instance *instance)
             libime::DefaultLanguageModelResolver::instance()
                 .languageModelFileForLanguage("zh_CN")));
     ime_->dict()->load(libime::PinyinDictionary::SystemDict,
-                       LIBIME_INSTALL_PKGDATADIR "/sc.dict",
+                       stringutils::joinPath(getenv("LIBIME_INSTALL_PKGDATADIR"), "sc.dict").c_str(),
                        libime::PinyinDictFormat::Binary);
     prediction_.setUserLanguageModel(ime_->model());
```

In order to make "table" input methods work, patch below should be applied:

```diff
diff --git a/im/table/engine.cpp b/im/table/engine.cpp
index 89fce9c..1dec491 100644
--- a/im/table/engine.cpp
+++ b/im/table/engine.cpp
@@ -144,7 +144,7 @@ const libime::PinyinDictionary &TableEngine::pinyinDict() {
     if (!pinyinLoaded_) {
         try {
             pinyinDict_.load(libime::PinyinDictionary::SystemDict,
-                             LIBIME_INSTALL_PKGDATADIR "/sc.dict",
+                             stringutils::joinPath(getenv("LIBIME_INSTALL_PKGDATADIR"), "sc.dict").c_str(),
                              libime::PinyinDictFormat::Binary);
         } catch (const std::exception &) {
         }
diff --git a/im/table/ime.cpp b/im/table/ime.cpp
index 0f4668e..4379f0f 100644
--- a/im/table/ime.cpp
+++ b/im/table/ime.cpp
@@ -111,7 +111,7 @@ TableIME::requestDict(const std::string &name) {
         try {
             auto dict = std::make_unique<libime::TableBasedDictionary>();
             auto dictFile = StandardPath::global().open(
-                StandardPath::Type::PkgData, *root.config->file, O_RDONLY);
+                StandardPath::Type::PkgData, stringutils::joinPath(getenv("LIBIME_INSTALL_PKGDATADIR"), *root.config->file), O_RDONLY);
             TABLE_DEBUG() << "Load table at: " << *root.config->file;
             if (dictFile.fd() < 0) {
                 throw std::runtime_error("Couldn't open file");
```

## PoC

<details>
<summary>Logcat</summary>

```
D/fcitx5: I
D/fcitx5: 2021-06-18 22:15:01.644201 instance.cpp:
D/fcitx5: 1371] Override Enabled Addons:
D/fcitx5: {}
D/fcitx5: I
D/fcitx5: 2021-06-18 22:15:01.644339 instance.cpp:
D/fcitx5: 1372] Override Disabled Addons:
D/fcitx5: {}
D/fcitx5: I
D/fcitx5: 2021-06-18 22:15:01.697704 addonmanager.cpp:
D/fcitx5: 189
D/fcitx5: ]
D/fcitx5: Loaded addon unicode
D/fcitx5: I
D/fcitx5: 2021-06-18 22:15:01.752736
D/fcitx5:
D/fcitx5: addonmanager.cpp
D/fcitx5: :
D/fcitx5: 189
D/fcitx5: ]
D/fcitx5: Loaded addon
D/fcitx5: quickphrase
D/fcitx5: I2021-06-18 22:15:01.778334 addonmanager.cpp:189] Loaded addon imselector
    I2021-06-18 22:15:01.779049 addonmanager.cpp:189] Loa
D/fcitx5: ded addon androidfrontend
D/fcitx5: I
D/fcitx5: 2021-06-18 22:15:01.784554
D/fcitx5:
D/fcitx5: addonmanager.cpp
D/fcitx5: :
D/fcitx5: 189
D/fcitx5: ]
D/fcitx5: Loaded addon
D/fcitx5: pinyinhelper
D/fcitx5: I
D/fcitx5: 2021-06-18 22:15:01.811568
D/fcitx5:
D/fcitx5: inputmethodmanager.cpp
D/fcitx5: :
D/fcitx5: 117
D/fcitx5: ]
D/fcitx5: No valid input method group in configuration.
D/fcitx5: Building a default one
D/fcitx5: I
D/fcitx5: 2021-06-18 22:15:01.812048
D/fcitx5:
D/fcitx5: instance.cpp
D/fcitx5: :
D/fcitx5: 730
D/fcitx5: ]
D/fcitx5: Items in
D/fcitx5: Default
D/fcitx5: :
D/fcitx5: [
D/fcitx5: InputMethodGroupItem(
D/fcitx5: keyboard-us
D/fcitx5: ,layout=
D/fcitx5: )
D/fcitx5: ]
D/fcitx5: I
D/fcitx5: 2021-06-18 22:15:01.812359
D/fcitx5:
D/fcitx5: instance.cpp
D/fcitx5: :
D/fcitx5: 735
D/fcitx5: ]
D/fcitx5: Generated groups:
D/fcitx5: [
D/fcitx5: Default
D/fcitx5: ]
D/fcitx5: E
D/fcitx5: 2021-06-18 22:15:01.812695
D/fcitx5:
D/fcitx5: instance.cpp
D/fcitx5: :
D/fcitx5: 1381
D/fcitx5: ]
D/fcitx5: Couldn't find keyboard-us
W/Thread-2: type=1400 audit(0.0:9717): avc: denied { read } for name="uuid" dev="proc" ino=4488195 scontext=u:r:untrusted_app:s0:c512,c768 tcontext=u:object_r:proc:s0 tclass=file permissive=0
D/fcitx5: I
D/fcitx5: 2021-06-18 22:15:01.834927 addonmanager.cpp
D/fcitx5: :
D/fcitx5: 189]
D/fcitx5: Loaded addon
D/fcitx5: punctuation
D/fcitx5: E
D/fcitx5: 2021-06-18 22:15:02.209061
D/fcitx5:
D/fcitx5: pinyin.cpp
D/fcitx5: :
D/fcitx5: 647
D/fcitx5: ]
D/fcitx5: Failed to load pinyin history:
D/fcitx5: io fail: unspecified iostream_category error
D/fcitx5: I
D/fcitx5: 2021-06-18 22:15:02.299407
D/fcitx5:
D/fcitx5: addonmanager.cpp
D/fcitx5: :
D/fcitx5: 189
D/fcitx5: ]
D/fcitx5: Loaded addon
D/fcitx5: pinyin
D/fcitx5: I
D/fcitx5: 2021-06-18 22:15:02.305780
D/fcitx5:
D/fcitx5: addonmanager.cpp
D/fcitx5: :
D/fcitx5: 189
D/fcitx5: ]
D/fcitx5: Loaded addon
D/fcitx5: cloudpinyin
D/fcitx5: I
D/fcitx5: 2021-06-18 22:15:02.689011
D/fcitx5:
D/fcitx5: addonmanager.cpp
D/fcitx5: :
D/fcitx5: 189
D/fcitx5: ]
D/fcitx5: Loaded addon
D/fcitx5: spell
D/fcitx5: I
D/fcitx5: 2021-06-18 22:15:04.757577
D/fcitx5:
D/fcitx5: androidfrontend.cpp
D/fcitx5: :
D/fcitx5: 83
D/fcitx5: ]
D/fcitx5: KeyEvent key: n isRelease: 0 accepted: 1
D/fcitx5: I
D/fcitx5: 2021-06-18 22:15:04.785501
D/fcitx5:
D/fcitx5: androidfrontend.cpp
D/fcitx5: :
D/fcitx5: 83
D/fcitx5: ]
D/fcitx5: KeyEvent key: i isRelease: 0 accepted: 1
D/fcitx5: I
D/fcitx5: 2021-06-18 22:15:04.833655
D/fcitx5:
D/fcitx5: androidfrontend.cpp
D/fcitx5: :
D/fcitx5: 83
D/fcitx5: ]
D/fcitx5: KeyEvent key: h isRelease: 0 accepted: 1
D/fcitx5: I
D/fcitx5: 2021-06-18 22:15:04.858009
D/fcitx5:
D/fcitx5: androidfrontend.cpp
D/fcitx5: :
D/fcitx5: 83
D/fcitx5: ]
D/fcitx5: KeyEvent key: a isRelease: 0 accepted: 1
D/fcitx5: I
D/fcitx5: 2021-06-18 22:15:04.872927
D/fcitx5:
D/fcitx5: androidfrontend.cpp
D/fcitx5: :
D/fcitx5: 83
D/fcitx5: ]
D/fcitx5: KeyEvent key: o isRelease: 0 accepted: 1
D/androidfrontend: candidateListCallback
D/androidfrontend: 108 candidates
D/FcitxEvent: CandidateList, [108]你好,你,尼,泥,妮,逆,腻,拟,呢,倪,妳,溺,👋,祢,匿,霓,昵,睨,怩,猊,擬,膩,鲵,旎,坭,伲,铌,輗,袮,貎,儗,麑,抳,柅,暱,埿,禰,惄,薿,孨,聻,蜺,苨,迡,檷,嫟,眤,籾,秜,縌,腝,馜,鯢,氼,狔,孴,婗,痆,懝,胒,隬,棿,齯,晲,淣,㘈,掜,抐,愵,屰,屔,嬺,堄,儞,聣,伱,䵒,䵑,䦵,䝚,臡,䛏,䘽,蚭,蛪,䘦,䘌,觬,誽,譺,䕥,跜,䁥,㹸,㵫,郳,鈮,鑈,㲻,㮏,㪒,㩘,鯓,㦐,㥾,㠜,㞾,𣲷
D/androidfrontend: select candidate #42
D/androidfrontend: candidateListCallback
D/androidfrontend: 90 candidates
D/FcitxEvent: CandidateList, [90]好,号,浩,豪,耗,毫,郝,昊,嚎,皓,號,蒿,灏,蚝,壕,镐,濠,嗥,哈,薅,貉,颢,晧,皞,暠,蠔,灝,滈,淏,呺,恏,鎬,鄗,皜,顥,澔,秏,嚆,譹,暤,諕,竓,哠,籇,藃,茠,傐,儫,椃,䪽,䧫,㘪,嘷,噑,䧚,虠,㙱,薧,峼,䝥,悎,薃,昦,㚪,聕,䝞,暭,曍,鰝,毜,㝀,㞻,䚽,䒵,㬶,㠙,皥,㬔,獆,獋,獔,皡,㩝,䯫,蛤,虾,铪,奤,鉿,丷
D/androidfrontend: select candidate #42
D/fcitx5: I
D/fcitx5: 2021-06-18 22:15:05.848549
D/fcitx5:
D/fcitx5: androidfrontend.cpp
D/fcitx5: :
D/fcitx5: 29
D/fcitx5: ]
D/fcitx5: Commit: 苨哠
D/androidfrontend: commitStringCallback
D/FcitxEvent: CommitString, 苨哠
D/androidfrontend: candidateListCallback
D/FcitxEvent: CandidateList, [0]
```
</details>

## Nix

Appropriate Android SDK with NDK is available in the development shell.  The `gradlew` should work out-of-the-box, so you can install the app to your phone with `./gradlew installDebug` after applying the patch mentioned above. For development, you may want to install the unstable version of Android Studio, and point the project SDK path to `$ANDROID_SDK_ROOT` defined in the shell. Notice that Android Studio may generate wrong `local.properties` which sets the SDK location to `~/Android/SDK` (installed by SDK Manager). In such case, you need specify `sdk.dir` as the project SDK in that file manually, in case Android Studio sticks to the wrong global SDK.

<img align="left" width="80" height="80"
src="fastlane/metadata/android/en-US/images/icon.png" alt="App icon">

# FlorisBoard [![Crowdin](https://badges.crowdin.net/florisboard/localized.svg)](https://crowdin.florisboard.patrickgold.dev) [![Matrix badge](https://img.shields.io/badge/chat-%23florisboard%3amatrix.org-blue)](https://matrix.to/#/#florisboard:matrix.org) ![FlorisBoard CI](https://github.com/florisboard/florisboard/workflows/FlorisBoard%20CI/badge.svg?event=push)

**FlorisBoard** is a free and open-source keyboard for Android 6.0+
devices. It aims at being modern, user-friendly and customizable while
fully respecting your privacy. Currently in early-beta state.

### Stable [![Latest stable release](https://img.shields.io/github/v/release/florisboard/florisboard)](https://github.com/florisboard/florisboard/releases/latest)

Releases on this track are in general stable and ready for everyday use, except for features marked as experimental. Use one of the following options to receive FlorisBoard's stable releases:

_A. Get it on F-Droid_:

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="64" alt="F-Droid badge">](https://f-droid.org/packages/dev.patrickgold.florisboard)

_B. Google Play Public Alpha Test_:

You can join the public alpha test programme on Google Play. To become a
tester, follow these steps:
1. Join the
   [FlorisBoard Public Alpha Test](https://groups.google.com/g/florisboard-public-alpha-test)
   Google Group to be able to access the testing programme.
2. Go to the
   [FlorisBoard Testing Page](https://play.google.com/apps/testing/dev.patrickgold.florisboard),
   then click "Become a tester". Now you are enrolled in the testing
   programme.
3. To try out FlorisBoard, download it via Google Play. To do so, click
   on "Download it on Google Play", which takes you to the [PlayStore
   listing](https://play.google.com/store/apps/details?id=dev.patrickgold.florisboard).
4. Finished! You will receive future versions of FlorisBoard via Google
   Play.

With the v0.4.0 release FlorisBoard will enter the public beta in GPlay, allowing to directly search
for and download FlorisBoard without prior joining the alpha group.

_C. Use the APK provided in the release section of this repo_

### Beta [![Latest beta release](https://img.shields.io/github/v/release/florisboard/florisboard?include_prereleases)](https://github.com/florisboard/florisboard/releases)

Releases on this track are also in general stable and should be ready for everyday use, though crashes and bugs are more likely to occur. Use releases from this track if you want to get new features faster and give feedback for brand-new stuff. Options to get beta releases:

_A. IzzySoft's repo for F-Droid_:

[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" height="64" alt="IzzySoft repo badge">](https://apt.izzysoft.de/fdroid/index/apk/dev.patrickgold.florisboard.beta)

_B. Google Play_:

Follow the same steps as for the stable track, the app can then be accessed [here](https://play.google.com/store/apps/details?id=dev.patrickgold.florisboard.beta).

_C. Use the APK provided in the release section of this repo_

### Giving feedback
If you want to give feedback to FlorisBoard, there are several ways to
do so, as listed [here](CONTRIBUTING.md#giving-general-feedback).

---

<img align="right" height="256"
src="https://patrickgold.dev/media/previews/florisboard-preview-day.png"
alt="Preview image">

## Implemented features
This list contains all implemented and fully functional features
FlorisBoard currently has to offer. For planned features and its
milestones, please refer to the [Feature roadmap](#feature-roadmap).

### Basics
* [x] Implementation of the keyboard core (InputMethodService)
* [x] Custom implementation of deprecated KeyboardView (base only)
* [x] Caps + Caps Lock
* [x] Key popups
* [x] Extended key popups (e.g. a -> á, à, ä, ...)
* [x] Key press sound/vibration
* [x] Portrait orientation support
* [x] Landscape orientation support (needs tweaks)

### Layouts
* [x] Latin character layouts (QWERTY, QWERTZ, AZERTY, Swiss, Spanish, Norwegian, Swedish/Finnish, Icelandic, Danish,
      Hungarian, Croatian, Polish, Romanian, Colemak, Dvorak, Turkish-Q, Turkish-F, and more...)
* [x] Non-latin character layouts (Arabic, Persian, Kurdish, Greek, Russian (JCUKEN), and more...)
* [x] Adapt to situation in app (password, url, text, etc. )
* [x] Special character layout(s)
* [x] Numeric layout
* [x] Numeric layout (advanced)
* [x] Phone number layout
* [x] Emoji layout
* [x] Emoticon layout

### Preferences
* [x] Setup wizard
* [x] Preferences screen
* [x] Customize look and behaviour of keyboard
* [x] Theme presets (currently only day/night theme + borderless)
* [x] Theme customization
* [x] Subtype selection (language/layout)
* [x] Keyboard behaviour preferences
* [x] Gesture preferences
* [x] User dictionary manager (system and internal)

### Other useful features
* [x] Support for Android 11+ inline autofill API
* [x] One-handed mode
* [x] Clipboard/cursor tools
* [x] Clipboard manager/history
* [x] Integrated number row / symbols in character layouts
* [x] Gesture support
* [x] Full support for the system user dictionary (shared dictionary
      between all keyboards) and a private, internal user dictionary
* [x] Full integration in IME service list of Android (xml/method)
      (integration is internal-only, because Android's default subtype
      implementation not really allows for dynamic language/layout
      pairs, only compile-time defined ones)
* [ ] Description and settings reference in System Language & Input
* [ ] (dev only) Generate well-structured documentation of code
* [ ] ...

## Feature roadmap
This section describes the features which are planned to be implemented
in FlorisBoard for the next major versions, modularized into sections.
Please note that the milestone due dates are only raw estimates and will
most likely be delayed back, even though I'm eager to stick to these as
close as possible.

### [v0.4.0](https://github.com/florisboard/florisboard/milestone/4)
- Module A: Smartbar rework (Implemented with [#91])
  - Ability to enable/disable Smartbar (features below thus only work if
    Smartbar is enabled)
  - Dynamic switching between clipboard tools and word suggestions
  - Ability to show both the number row and word suggestions at once
  - Better icons in quick actions
  - Complete rework of the Smartbar code base and the Smartbar layout
    definition in XML

- Module B: Composing suggestions (Phase 1: [#329])
  - Auto-suggestion of words based of precompiled dictionaries
  - Management of custom dictionary entries
  - Next-word suggestions by training language models. Data collected here is stored locally and never leaves
    the user's device.

- Module C: Extension packs (Implemented with [#162], reworked several times and still not stable)
  - Ability to load dictionaries (and later potentially other cool
    features too) only if needed to keep the core APK size small
  - Currently unclear how exactly this will work, but this is definitely
    a must-have feature
  - A full implementation may come only in v0.5.0

- Module D: Glide typing (Implemented with [#544])
  - Swiping over the characters will automatically convert this to a word
  - Possibly also add improvements based on the Flow keyboard

- Module E: Theme rework (Implemented with [#162])
  - Themes are now based on the Asset schema
  - Dynamic theme creation
  - Different theme modes (`Always day`, `Always night`, `Follow system`
    and `Follow time`)
  - Define a separate theme both for day and night theme
  - Adapt to app theme if possible
  - Theme import/export

### [v0.5.0](https://github.com/florisboard/florisboard/milestone/5)
There's no exact roadmap yet, but these are the most important points:
- Full layout customization in runtime
- Extensive rework and customization of the media input (emojis, emoticons, kaomoji)
- Better Smartbar customization
- As an extension GIF support

### > v0.5.0
This is completely open as of now and will gather planned features as time
passes...

Backlog (currently not assigned to any milestone):

- Floating keyboard

[#91]: https://github.com/florisboard/florisboard/pull/91
[#162]: https://github.com/florisboard/florisboard/pull/162
[#329]: https://github.com/florisboard/florisboard/pull/329
[#544]: https://github.com/florisboard/florisboard/pull/544

## Contributing
Wanna contribute to FlorisBoard? That's great to hear! There are lots of
different ways to help out. Bug reporting, making pull requests,
translating FlorisBoard to make it more accessible, etc. For more
information see the ![contributing guidelines](CONTRIBUTING.md). Thank
you for your help!

## List of permissions FlorisBoard requests
Please refer to this [page](https://github.com/florisboard/florisboard/wiki/List-of-permissions-FlorisBoard-requests)
to get more information on this topic.

## Used libraries, components and icons
* [Google Flexbox Layout for Android](https://github.com/google/flexbox-layout)
  by [google](https://github.com/google)
* [Google Material icons](https://github.com/google/material-design-icons) by
  [google](https://github.com/google)
* [KotlinX serialization library](https://github.com/Kotlin/kotlinx.serialization) by
  [Kotlin](https://github.com/Kotlin)
* [ColorPicker preference](https://github.com/jaredrummler/ColorPicker) by
  [Jared Rummler](https://github.com/jaredrummler)
* [Timber](https://github.com/JakeWharton/timber) by
  [JakeWharton](https://github.com/JakeWharton)
* [expandable-fab](https://github.com/nambicompany/expandable-fab) by
  [Nambi](https://github.com/nambicompany)

## Usage notes for included binary dictionary files
All binary dictionaries included within this project in
(this)[app/src/main/assets/ime/dict] asset folder are built from various
sources, as stated below.

### Source 1: [wordfreq library by LuminosoInsight](https://github.com/LuminosoInsight/wordfreq):
`wordfreq` is a repository which provides both a Python library and raw
data (the wordlists). Only the data has been extracted in order to build
binary dictionary files from it. `wordfreq`'s data is licensed under the
Creative Commons Attribution-ShareAlike 4.0 license
(https://creativecommons.org/licenses/by-sa/4.0/).

For further information on what wordfreq's data depends on, see
(https://github.com/LuminosoInsight/wordfreq#license).

## License
```
Copyright 2020 Patrick Goldinger

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
