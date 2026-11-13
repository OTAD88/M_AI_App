# PaperInk Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans to implement and verify the project task-by-task.

**Goal:** Build a native Android PDF reader with persistent page-relative annotations and an installable APK pipeline.

**Architecture:** Android PdfRenderer reads source documents; Compose draws a separate overlay; Room persists normalized annotation records and reading position. Export rasterizes each PDF page and overlays the stored annotations into a separate PDF copy.

**Tech Stack:** Kotlin, Android SDK 35, Jetpack Compose, Room 2.7.2, Gradle 8.11.1, JDK 17, GitHub Actions.

**Spec:** docs/superpowers/specs/2026-11-13-paperink-design.md

## Global Constraints
- Never open the source PDF for writing.
- Persist coordinates relative to the PDF page, not screen pixels.
- Erase only records in the annotation database.
- Debug APK builds must not require secrets.
- Keep the native project under android-pdf-annotator/ to avoid changing the existing Capacitor app.

## Review Focus
- URI permission revocation or provider failures must show an error rather than corrupting the source.
- Large page dimensions must not allocate unbounded bitmaps.
- Empty/one-point strokes must not crash geometry or export.
- Rotation and window resizing must preserve normalized positions.
- Interrupted writes must leave the source document unchanged.

---

### Task 1: Native project and CI
- [x] Create Gradle/Android configuration, manifest, theme, README.
- [x] Add Actions workflow for push, manual runs, tests, APK artifact and version-tag release.
- [ ] Verify remote build and artifact.

### Task 2: Annotation data and PDF engine
- [x] Add normalized geometry model and JSON serialization.
- [x] Add Room entities/DAOs for annotations and reading position.
- [x] Add read-only PdfRenderer page rendering and separate-copy export.
- [ ] Verify high-resolution render and export on device.

### Task 3: Reader UI and tools
- [x] Add Compose document picker, page navigation, toolbar, pen, highlighter, eraser, geometric tools, marker manager, undo/redo and pan/zoom.
- [ ] Add marker note editing and marker dragging.
- [ ] Add full selection resize/recolor interaction.
- [ ] Add Arabic/English localization resources and dark theme.
- [ ] Verify stylus/touch behavior on physical Android devices.

### Task 4: Quality gates
- [x] Add unit tests for normalized coordinates and stroke hit testing.
- [ ] Execute test suite and CI build.
- [ ] Fix compiler/runtime defects found by CI.
- [ ] Verify export and original-file hash remain unchanged on a real device.
