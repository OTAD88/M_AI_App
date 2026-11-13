# PaperInk — Android PDF Drawing & Annotation

Native Android PDF reader and annotation app built with Kotlin, Jetpack Compose, Android PdfRenderer and Room.

## Build

Requires JDK 17 and Android SDK Platform 35.

```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Current implementation

- Import PDFs through Android's Storage Access Framework.
- Render and navigate one PDF page at a time.
- Freehand pen, transparent highlighter, geometric line/arrow/rectangle/circle tools.
- Annotation records use normalized page-relative coordinates and are stored separately in Room.
- Eraser removes matching annotation records, never PDF source content.
- Add colored paper-flag markers and browse all markers in the document.
- Remember the last reading page.
- Export a separate annotated PDF copy; the imported source URI is never opened for writing.
- Undo/redo for annotation mutations.

## Data integrity

The source PDF is opened read-only via a persisted document URI. Annotations are stored in `paperink.db`; export writes to a destination selected by the user and does not alter the source.

## CI

The repository workflow at `.github/workflows/android.yml` builds and tests this project and uploads the debug APK as an Actions artifact. Version tags also publish a GitHub Release when the workflow succeeds.

## Known limitations in this initial implementation

- Annotation object selection supports moving strokes/objects; interactive resize/recolor and marker-note editing are not yet complete.
- Highlighter uses translucent freehand strokes rather than PDF text-selection geometry.
- Runtime behavior must still be exercised on physical Android devices, including stylus latency and vendor-specific document providers.
