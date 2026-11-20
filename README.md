<div dir="rtl">

# 📥 Nazil

<div align="center">

**تطبيق أندرويد عربي لتحميل الفيديو والصوت والصور من أي منصة — مبني على yt-dlp**

[![Build APK](https://github.com/ko-co87de/nazil/actions/workflows/build.yml/badge.svg)](https://github.com/ko-co87de/nazil/actions/workflows/build.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-blue)](https://developer.android.com/jetpack/compose)
[![yt-dlp](https://img.shields.io/badge/yt--dlp-1000%2B%20sites-red)](https://github.com/yt-dlp/yt-dlp)
[![License](https://img.shields.io/badge/License-MIT-green)](#الترخيص)

</div>

---

## ✨ المميزات

### 🎯 الميزات الأساسية
- **دعم 1000+ منصة** عبر محرك yt-dlp (يوتيوب، إنستغرام، تيك توك، X، فيسبوك، سناب شات، تلغرام، تويش، ريديت، بنترست، فيميو، ديلي موشن، ساوند كلاود، بيليبيلي…)
- **واجهة عربية كاملة** بتصميم Material 3 عصري
- **اللصق والمشاركة**: الصق رابطاً مباشرةً، أو شارك رابطاً من أي تطبيق إلى Nazil
- **3 تبويبات**: تحميل، مكتبة، إعدادات

### 🆕 الميزات الجديدة في الإصدار 1.1

#### 1. منتقي الصيغ (Format Picker)
اعرض كل الصيغ المتاحة للفيديو مع التفاصيل:
- الدقة (1080p, 720p, 480p, audio-only…)
- معدل الإطارات (fps)
- صيغة الملف (mp4, webm, m4a…)
- معدل البت (kbps)
- الحجم التقريبي للملف
- تمييز الصيغ: "صوت فقط" / "فيديو فقط" / "فيديو + صوت"

<div align="center">

```
┌─────────────────────────────────────┐
│  🎚️ اختر الصيغة          12 صيغة  │
├─────────────────────────────────────┤
│  ▶ 1080p@60 • mp4 • 5.2k    85 MB │
│  ▶ 720p • mp4 • 3.1k       48 MB │
│  🎵 audio only • m4a • 128k 3 MB │
│  🎬 video only • mp4        60 MB │
└─────────────────────────────────────┘
```

</div>

#### 2. SponsorBlock 🚫
- شيل تلقائياً مقاطع: الإعلان، الترويج الذاتي، المقدمة، التفاعل، الخاتمة، المعاينة
- يستخدم [sponsor.ajay.app API](https://sponsor.ajay.app)
- يطبّق على فيديوهات يوتيوب فقط

#### 3. الترجمة (Subtitles) 📝
- نزّل ترجمة تلقائية أو مرفقة مع الفيديو
- ادمجها داخل ملف MP4 النهائي (مدمجة بالكامل)
- اضبط لغات الترجمة: `ar,en,es,fr` (مفصولة بفاصلة)

#### 4. واجهة محسّنة بالكامل 🎨
- **رأس متدرّج** بألوان Nazil المميزة (Indigo + Rose + Amber)
- **بطاقة إحصائيات حية** على الشاشة الرئيسية: عدد التنزيلات النشطة، المكتملة، الحجم الكلي
- **تقدّم دائري** فوق الصور المصغّرة للتنزيلات النشطة
- **عرض السرعة** أثناء التحميل (MB/s) مع وقت الانتهاء المتوقع
- **شارات الحالة** الملوّنة (مكتمل، فشل، ملغى، بالانتظار…)

#### 5. مكتبة ذكية 📚
- **بحث فوري** بالعنوان، الموقع، أو اسم المنشئ
- **فلاتر دقيقة**: الكل / فيديو / صوت / صور / مكتمل / فشل
- **إعادة المحاولة** بضغطة زر للتنزيلات الفاشلة
- **زر "مسح الكل"** و"مسح المكتمل" و"مسح الفاشل" منفصلة
- **إحصائيات رأس الصفحة**: عدد المكتمل، الفاشل، الإجمالي، والمساحة المستخدمة

#### 6. إعدادات موسّعة ⚙️
- المظهر (نظامي/فاتح/داكن)
- الجودة الافتراضية (أفضل/1080p/720p/480p/MP3)
- تفعيل/تعطيل SponsorBlock
- تفعيل الترجمة + ضبط اللغات
- التنزيلات المتزامنة (1-4)
- التنزيل على واي فاي فقط
- إعادة المحاولة التلقائية

---

## 🛠️ التقنيات المستخدمة

| المكوّن | التقنية | الإصدار |
|---|---|---|
| اللغة | Kotlin | 2.0 |
| واجهة المستخدم | Jetpack Compose | BOM 2024.10 |
| التصميم | Material 3 | — |
| محرك التنزيل | yt-dlp-android | 0.18.1 |
| معالجة الفيديو | FFmpeg (مدمج) | — |
| معالج التنزيل المتوازي | aria2c (مدمج) | — |
| التخزين | DataStore Preferences | 1.1.1 |
| التسلسل | kotlinx-serialization | 1.7.3 |
| التزامن | kotlinx-coroutines | 1.9.0 |
| تحميل الصور | Coil | 2.7.0 |
| الحد الأدنى للأندرويد | API 26 (Android 8.0) | — |
| الهدف | API 35 (Android 15) | — |
| البنية | arm64-v8a | 64-bit |

---

## 📁 هيكل المشروع

```
nazil/
├── app/
│   ├── build.gradle.kts                 # إعدادات الوحدة
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/nazil/app/
│       │   ├── NazilApp.kt              # تهيئة yt-dlp
│       │   ├── MainActivity.kt          # نقطة الدخول + التنقل
│       │   ├── data/
│       │   │   ├── Models.kt            # نماذج البيانات + دوال التنسيق
│       │   │   └── AppRepository.kt     # مستودع البيانات (DataStore)
│       │   ├── download/
│       │   │   └── DownloadEngine.kt    # محرك yt-dlp + SponsorBlock + الترجمة
│       │   ├── ui/
│       │   │   ├── MainViewModel.kt     # منطق الأعمال + الإحصائيات
│       │   │   ├── components/
│       │   │   │   ├── NazilComponents.kt       # بطاقات التنزيل + شارات الحالة
│       │   │   │   └── FormatPickerDialog.kt    # منتقي الصيغ الجديد
│       │   │   ├── screens/
│       │   │   │   ├── HomeScreen.kt            # الشاشة الرئيسية
│       │   │   │   ├── LibraryScreen.kt         # المكتبة + البحث + الفلاتر
│       │   │   │   └── SettingsScreen.kt        # الإعدادات الموسّعة
│       │   │   └── theme/
│       │   │       ├── Color.kt                # لوحة الألوان المميزة
│       │   │       ├── Theme.kt                # تطبيق الثيم
│       │   │       └── Type.kt                 # الخطوط
│       │   └── util/
│       │       ├── SiteDetector.kt            # كاشف المنصة (20 موقعاً)
│       │       └── FileActions.kt             # فتح/مشاركة الملفات
│       └── res/
│           ├── values/strings.xml             # ترجمات عربية كاملة
│           ├── values-night/themes.xml        # ثيم داكن
│           └── xml/file_paths.xml             # FileProvider paths
├── .github/workflows/build.yml                # CI: بناء APK تلقائياً
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/wrapper/
```

---

## 🚀 البناء من المصدر

### المتطلبات
- Android Studio Koala Feature Drop أو أحدث
- JDK 17
- Android SDK Platform 35
- Build Tools 34.0.0

### خطوات البناء

```bash
# 1. استنساخ المستودع
git clone https://github.com/ko-co87de/nazil.git
cd nazil

# 2. منح صلاحية التنفيذ لـ gradlew
chmod +x gradlew

# 3. بناء APK للتصحيح
./gradlew assembleDebug

# 4. بناء APK للإصدار (موقّع)
./gradlew assembleRelease
```

الـ APK الناتج سيكون في:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-arm64-v8a-release.apk`

### بناء تلقائي عبر GitHub Actions

كل `push` على فرع `main` يشغّل سير عمل البناء تلقائياً. حمّل الـ APK من تبويب **Actions** → آخر build → Artifacts.

---

## 📱 التثبيت

1. حمّل ملف APK من [Actions](https://github.com/ko-co87de/nazil/actions)
2. فعّل "تثبيت تطبيقات من مصادر غير معروفة" من إعدادات الأندرويد
3. افتح ملف APK واتبع التعليمات
4. امنح الصلاحيات المطلوبة (التخزين، الإشعارات)

### الصلاحيات المطلوبة

| الصلاحية | السبب |
|---|---|
| `INTERNET` | للاتصال بالمواقع وتنزيل الملفات |
| `ACCESS_NETWORK_STATE` | للتحقق من حالة الشبكة (واي فاي/موبايل) |
| `POST_NOTIFICATIONS` | إشعارات حالة التنزيل (Android 13+) |
| `WRITE_EXTERNAL_STORAGE` | حفظ الملفات (Android 9 وأقل) |
| `READ_MEDIA_VIDEO` | فتح ملفات الفيديو (Android 13+) |
| `READ_MEDIA_IMAGES` | فتح الصور (Android 13+) |

---

## 🎨 لوحة الألوان

<div align="center">

| اللون | HEX | الاستخدام |
|---|---|---|
| 🟣 Indigo Deep | `#1E1B4B` | الخلفية الداكنة، العناوين |
| 🟠 Amber | `#F59E0B` | اللون الأساسي، الأزرار |
| 🌹 Rose | `#F43F5E` | اللون الثانوي، التدرّجات |
| 🟢 Success | `#10B981` | الحالة المكتملة |
| ⚪ Warm White | `#FFFBF5` | خلفية الثيم الفاتح |

</div>

---

## 🌍 المنصات المدعومة

الكشف التلقائي عن 20 منصة شائعة:

| فيديو | صوت | صور |
|---|---|---|
| YouTube | SoundCloud | Instagram |
| TikTok | — | Pinterest |
| Instagram | — | Imgur |
| X (Twitter) | — | Flickr |
| Facebook | — | — |
| Reddit | — | — |
| Vimeo | — | — |
| Dailymotion | — | — |
| Twitch | — | — |
| Bilibili | — | — |
| Snapchat | — | — |
| Telegram | — | — |
| LinkedIn | — | — |

> ملاحظة: yt-dlp نفسه يدعم 1000+ منصة، لكن الكاشف التلقائي يتعرف على الـ20 الأكثر شيوعاً.

---

## 🔐 الأمان والخصوصية

- ✅ **بدون تتبّع**: لا نجمع أي بيانات استخدام
- ✅ **بدون إعلانات**
- ✅ **كل البيانات محلية**: تاريخ التنزيلات والإعدادات تُحفظ على جهازك فقط
- ✅ **مفتوح المصدر**: الكود متاح للمراجعة الكاملة
- ⚠️ **استخدم بمسؤولية**: احترم حقوق الملكية الفكرية وشروط استخدام كل منصة

---

## 🤝 المساهمة

المساهمات مرحّب بها! اتبع هذه الخطوات:

1. اعمل Fork للمستودع
2. أنشئ فرعاً للميزة: `git checkout -b feature/amazing-feature`
3. اكتب الكود واختبره
4. اكتب رسائل commit واضحة بالعربية أو الإنجليزية
5. افتح Pull Request

### أفكار للمساهمة
- [ ] إضافة دعم أكثر من 20 منصة في الكاشف التلقائي
- [ ] تنزيل قوائم التشغيل (Playlists)
- [ ] جدولة التنزيلات
- [ ] دعم تخزين على SD Card مخصص
- [ ] واجهة باللغة الإنجليزية (i18n)
- [ ] ويدجت للشاشة الرئيسية

---

## 📋 المتطلبات الأساسية للمشروع

- تنزيل سريع وموثوق من أي منصة
- واجهة بسيطة وسهلة الاستخدام بالعربية
- احترام خصوصية المستخدم
- مفتوح المصدر بالكامل

---

## 📜 الترخيص

هذا المشروع مرخّص تحت رخصة MIT. راجع ملف [LICENSE](LICENSE) للتفاصيل.

> ملاحظة: المشروع يعتمد على `yt-dlp` و`FFmpeg` و`aria2c`، ولكل منها رخصته الخاصة.

---

## 🙏 شكر خاص

- [yt-dlp](https://github.com/yt-dlp/yt-dlp) — المحرك الأساسي للتنزيل
- [youtubedl-android](https://github.com/yausername/youtubedl-android) — تغليف أندرويد لـ yt-dlp
- [SponsorBlock](https://sponsor.ajay.app) — قاعدة بيانات رعاية المقاطع
- [Jetpack Compose](https://developer.android.com/jetpack/compose) — إطار واجهة المستخدم

---

<div align="center">

**صنع بحب 🇮🇶 — بغداد**

Nazil v1.1.0

</div>

</div>
