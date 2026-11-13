# PaperInk Android PDF Annotation — Design

## Intent
Deliver a native Android PDF reader that lets users draw and place page flags without modifying the imported source PDF. The app is designed for direct use on phones and tablets, with page-relative annotation coordinates and durable local storage.

## Goals
- Open local PDFs through the Storage Access Framework.
- Render pages and navigate with persistent reading position.
- Add pen, highlighter, eraser, geometric objects, and sticky flags.
- Store annotations separately from the source document.
- Export a separate annotated PDF copy.
- Provide Arabic-ready RTL support, English labels, and a compact Compose interface.

## Non-goals for the first delivery
- Cloud sync or account system.
- OCR and text-range selection.
- Collaboration or PDF form editing.
- Full vector PDF object editing.

## Approaches considered
1. Android PdfRenderer + custom Compose overlay + Room: selected. It keeps source rendering read-only, avoids a heavyweight native dependency, and provides a stable normalized coordinate layer.
2. Third-party PDF SDK: rejected for initial implementation because licensing/SDK integration adds cost and vendor constraints.
3. WebView/PDF.js: rejected because the request requires a native Android application and accurate touch drawing.

## Architecture
- PdfEngine: read-only page rendering and flattened-copy export.
- AnnotationModels: normalized point geometry and serialization.
- AnnotationDatabase: Room persistence for annotation records and reading positions.
- MainActivity: Compose reader surface, toolbar, gestures, marker manager and document pickers.
- GitHub Actions: remote JDK/Android SDK setup, unit tests, APK artifact, and tag-based release.

## Integrity rules
- Source PDFs are opened read-only.
- Annotation coordinates are normalized to page width and height.
- Every annotation row is keyed by source URI and annotation ID.
- Export writes only to a user-selected destination.
- Erasing deletes annotation records; it never paints over PDF source content.

## Engineering decisions
- Minimum Android API 26, compile/target API 35, JDK 17.
- Room is the local persistence source of truth.
- Debug builds require no private signing secrets.
- Release publishing uses the CI-generated debug APK until release signing secrets are intentionally configured.
