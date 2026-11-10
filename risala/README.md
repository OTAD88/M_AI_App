# رسالة — Android

تطبيق Android مستقل مبني بـ Kotlin وJetpack Compose.

## البناء محلياً
يتطلب JDK 17 وAndroid SDK 35 وGradle 8.9:

```bash
gradle assembleRelease
```

الناتج:
`app/build/outputs/apk/release/app-release.apk`

## النطاق الحالي
- واجهات عربية RTL.
- جهات الاتصال والمحادثات التجريبية المحلية.
- الملف الشخصي ومعرّف رسالة.
- المظهر الداكن والفاتح واختيار لون الواجهة.
- إعدادات الإشعارات والخصوصية واللغة.
- لا يوجد خادم أو إرسال رسائل عبر الإنترنت في هذه المرحلة.

تُوقّع نسخة Release الحالية بمفتاح debug حتى تكون قابلة للتثبيت والاختبار. يلزم مفتاح إصدار خاص قبل النشر التجاري.


Build pipeline: GitHub Actions (`.github/workflows/risala-android.yml`).
