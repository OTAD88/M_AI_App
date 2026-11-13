package com.paperink.pdf

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paperink.pdf.data.*
import com.paperink.pdf.ui.graphicsLayerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AnnotationDatabase.get(this)
        setContent {
            var dark by remember { mutableStateOf(false) }
            MaterialTheme(colorScheme = if (dark) darkColorScheme(primary = Color(0xFF8EC8B5)) else lightColorScheme(primary = Color(0xFF356B5D), background = Color(0xFFF4F3EF))) {
                PaperInkApp(database, dark, onToggleTheme = { dark = !dark })
            }
        }
    }
}

private val palette = listOf(
    Color(0xFF17212B), Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047),
    Color(0xFFFFB300), Color(0xFF8E24AA), Color(0xFFFF80AB), Color(0xFFFF6D00)
)

@Composable
private fun PaperInkApp(db: AnnotationDatabase, darkTheme: Boolean, onToggleTheme: () -> Unit) {
    var isArabic by remember { mutableStateOf(false) }
    fun label(en: String, ar: String) = if (isArabic) ar else en
    val screenBg = if (darkTheme) Color(0xFF151A18) else Color(0xFFF4F3EF)
    val topSurface = if (darkTheme) Color(0xFF202724) else Color(0xFFFCFBF8)
    val toolbarSurface = if (darkTheme) Color(0xFF252D29) else Color(0xFFEAE9E3)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var uri by remember { mutableStateOf<Uri?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var page by remember { mutableIntStateOf(0) }
    var pageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var tool by remember { mutableStateOf("PEN") }
    var color by remember { mutableStateOf(0xFF17212B.toInt()) }
    var width by remember { mutableFloatStateOf(.0035f) }
    var alpha by remember { mutableFloatStateOf(.34f) }
    var showToolbar by remember { mutableStateOf(true) }
    var showMarkers by remember { mutableStateOf(false) }
    var markerNoteTarget by remember { mutableStateOf<String?>(null) }
    var noteText by remember { mutableStateOf("") }
    var markerEditColor by remember { mutableIntStateOf(0xFFFFC107.toInt()) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var showCustomColor by remember { mutableStateOf(false) }
    var customHex by remember { mutableStateOf("#356B5D") }
    var scale by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var draft by remember { mutableStateOf<List<InkPoint>>(emptyList()) }
    var undoStack by remember { mutableStateOf<List<List<AnnotationRow>>>(emptyList()) }
    var redoStack by remember { mutableStateOf<List<List<AnnotationRow>>>(emptyList()) }
    val rows by if (uri != null) db.annotations().observeDocument(uri.toString()).collectAsStateWithLifecycle(initialValue = emptyList())
        else remember { mutableStateOf(emptyList()) }

    val openPdf = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { picked ->
        if (picked != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(picked, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            uri = picked
            page = 0
            scale = 1f
            pan = Offset.Zero
        }
    }
    val savePdf = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { destination ->
        val current = uri
        if (destination != null && current != null) scope.launch(Dispatchers.IO) {
            runCatching { PdfEngine.export(context.contentResolver, current, destination, rows) }
                .onFailure { withContext(Dispatchers.Main) { android.widget.Toast.makeText(context, "Export failed: ${it.message}", android.widget.Toast.LENGTH_LONG).show() } }
                .onSuccess { withContext(Dispatchers.Main) { android.widget.Toast.makeText(context, "Annotated PDF exported", android.widget.Toast.LENGTH_SHORT).show() } }
        }
    }

    LaunchedEffect(uri) {
        val current = uri ?: return@LaunchedEffect
        pageCount = withContext(Dispatchers.IO) { PdfEngine.pageCount(context.contentResolver, current) }
        page = withContext(Dispatchers.IO) { db.positions().get(current.toString())?.pageIndex ?: 0 }.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
    }
    LaunchedEffect(uri, page) {
        val current = uri ?: return@LaunchedEffect
        withContext(Dispatchers.IO) { db.positions().save(ReadingPosition(current.toString(), page)) }
        pageBitmap = withContext(Dispatchers.IO) { PdfEngine.renderPage(context.contentResolver, current, page) }
        scale = 1f
        pan = Offset.Zero
    }

    val pageRows = rows.filter { it.pageIndex == page }
    val markerRows = rows.filter { runCatching { InkObject.fromJson(it.payload).kind == "MARKER" }.getOrDefault(false) }

    fun saveSnapshot() { undoStack = undoStack + listOf(rows); if (undoStack.size > 40) undoStack = undoStack.takeLast(40); redoStack = emptyList() }
    fun persist(item: InkObject) {
        val current = uri ?: return
        scope.launch(Dispatchers.IO) { db.annotations().upsert(AnnotationRow(current.toString(), item.id, page, item.toJson())) }
    }
    fun deleteRow(row: AnnotationRow) {
        val current = uri ?: return
        scope.launch(Dispatchers.IO) { db.annotations().delete(current.toString(), row.id) }
    }
    fun addMarker(point: InkPoint) {
        saveSnapshot()
        persist(InkObject(UUID.randomUUID().toString(), "MARKER", listOf(point), color, width, 1f, ""))
    }

    CompositionLocalProvider(LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr) {
    Column(Modifier.fillMaxSize().background(screenBg)) {
        Row(Modifier.fillMaxWidth().background(topSurface).padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("PaperInk", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (darkTheme) Color(0xFFB8E0D2) else Color(0xFF244E43))
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { isArabic = !isArabic }) { Text(if (isArabic) "EN" else "عربي") }
            TextButton(onClick = onToggleTheme) { Text(if (darkTheme) label("Light", "فاتح") else label("Dark", "داكن")) }
            IconButton(onClick = { openPdf.launch(arrayOf("application/pdf")) }) { Icon(Icons.Default.FileOpen, "Open PDF") }
            IconButton(onClick = { showMarkers = true }, enabled = uri != null) { Icon(Icons.Default.Bookmarks, "Important markers") }
            IconButton(onClick = { if (uri != null) savePdf.launch("PaperInk-annotated.pdf") }, enabled = uri != null) { Icon(Icons.Default.SaveAlt, "Export annotated PDF") }
            TextButton(onClick = { showToolbar = !showToolbar }) { Text(if (showToolbar) "Hide tools" else "Tools") }
        }

        if (uri == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(label("Open a PDF to start writing", "افتح ملف PDF للبدء"), fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { openPdf.launch(arrayOf("application/pdf")) }) { Icon(Icons.Default.FileOpen, null); Spacer(Modifier.width(8.dp)); Text(label("Choose PDF", "اختر ملف PDF")) }
                    Text(label("Your original file stays unchanged.", "الملف الأصلي لن يتغير."), color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        } else {
            if (showToolbar) {
                Column(Modifier.fillMaxWidth().background(toolbarSurface).padding(vertical = 6.dp)) {
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        listOf("PEN", "HIGHLIGHT", "ERASER", "SELECT", "LINE", "ARROW", "RECTANGLE", "CIRCLE", "MARKER", "PAN").forEach { name ->
                            FilterChip(selected = tool == name, onClick = { tool = name }, label = { Text(name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) }, modifier = Modifier.padding(end = 5.dp))
                        }
                    }
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        palette.forEach { swatch ->
                            Box(Modifier.padding(end = 8.dp).size(25.dp).background(swatch, CircleShape).then(if (color == swatch.value.toLong().toInt()) Modifier.border(2.dp, Color.Black, CircleShape) else Modifier).clickable {
                                val chosen = swatch.value.toLong().toInt()
                                color = chosen
                                pageRows.firstOrNull { it.id == selectedId }?.let { row ->
                                    val obj = runCatching { InkObject.fromJson(row.payload) }.getOrNull()
                                    if (obj != null) { saveSnapshot(); scope.launch(Dispatchers.IO) { db.annotations().upsert(row.copy(payload = obj.copy(color = chosen).toJson())) } }
                                }
                            })
                        }
                        TextButton(onClick = { showCustomColor = true }) { Text("Custom", fontSize = 11.sp) }
                        Text(label("Width", "السُمك"), fontSize = 11.sp)
                        Slider(value = width, onValueChange = { width = it }, valueRange = .0015f.. .018f, modifier = Modifier.width(100.dp))
                        if (tool == "HIGHLIGHT") {
                            Text(label("Opacity", "الشفافية"), fontSize = 11.sp)
                            Slider(value = alpha, onValueChange = { alpha = it }, valueRange = .12f.. .65f, modifier = Modifier.width(90.dp))
                        }
                        IconButton(onClick = {
                            if (undoStack.isNotEmpty()) {
                                val previous = undoStack.last()
                                redoStack = redoStack + listOf(rows)
                                undoStack = undoStack.dropLast(1)
                                scope.launch(Dispatchers.IO) {
                                    db.annotations().deleteDocument(uri.toString())
                                    previous.forEach { db.annotations().upsert(it) }
                                }
                            }
                        }, enabled = undoStack.isNotEmpty()) { Icon(Icons.Default.Undo, "Undo") }
                        IconButton(onClick = {
                            if (redoStack.isNotEmpty()) {
                                val next = redoStack.last()
                                undoStack = undoStack + listOf(rows)
                                redoStack = redoStack.dropLast(1)
                                scope.launch(Dispatchers.IO) {
                                    db.annotations().deleteDocument(uri.toString())
                                    next.forEach { db.annotations().upsert(it) }
                                }
                            }
                        }, enabled = redoStack.isNotEmpty()) { Icon(Icons.Default.Redo, "Redo") }
                    }
                }
            }

            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp), contentAlignment = Alignment.Center) {
                val bitmap = pageBitmap
                if (bitmap != null) {
                    val displayHeight = maxWidth * (bitmap.height.toFloat() / bitmap.width.toFloat())
                    Box(Modifier.width(maxWidth).height(displayHeight).background(Color.White).graphicsLayerCompat(scale, pan)) {
                        Image(bitmap.asImageBitmap(), contentDescription = "PDF page", modifier = Modifier.fillMaxSize())
                        Canvas(Modifier.fillMaxSize()
                            .pointerInput(tool, color, width, alpha, pageRows) {
                                if (tool == "MARKER") detectTapGestures { tap ->
                                    saveSnapshot()
                                    addMarker(AnnotationGeometry.normalized(tap.x / size.width, tap.y / size.height))
                                }
                                else if (tool == "PAN") detectTransformGestures { _, delta, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 5f); pan += delta
                                }
                                else detectDragGestures(
                                    onDragStart = { start ->
                                        val p = AnnotationGeometry.normalized(start.x / size.width, start.y / size.height)
                                        if (tool == "ERASER") draft = listOf(p)
                                        else if (tool == "SELECT") draft = listOf(p)
                                        else draft = listOf(p)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val p = AnnotationGeometry.normalized(change.position.x / size.width, change.position.y / size.height)
                                        draft = if (tool in listOf("LINE", "ARROW", "RECTANGLE", "CIRCLE", "SELECT")) listOf(draft.firstOrNull() ?: p, p) else draft + p
                                    },
                                    onDragEnd = {
                                        if (draft.isNotEmpty()) {
                                            when (tool) {
                                                "ERASER" -> {
                                                    saveSnapshot()
                                                    val hit = draft.any { p -> pageRows.any { row ->
                                                        val obj = runCatching { InkObject.fromJson(row.payload) }.getOrNull()
                                                        obj != null && obj.kind != "MARKER" && AnnotationGeometry.hitTest(obj, p, .025f)
                                                    } }
                                                    if (hit) pageRows.filter { row ->
                                                        val obj = runCatching { InkObject.fromJson(row.payload) }.getOrNull()
                                                        obj != null && obj.kind != "MARKER" && draft.any { p -> AnnotationGeometry.hitTest(obj, p, .025f) }
                                                    }.forEach(::deleteRow)
                                                }
                                                "SELECT" -> {
                                                    val start = draft.first()
                                                    val target = pageRows.firstOrNull { row ->
                                                        val obj = runCatching { InkObject.fromJson(row.payload) }.getOrNull()
                                                        obj != null && AnnotationGeometry.hitTest(obj, start, .03f)
                                                    }
                                                    if (target != null) {
                                                        selectedId = target.id
                                                        if (draft.size > 1) {
                                                            val dx = draft.last().x - start.x; val dy = draft.last().y - start.y
                                                            val obj = InkObject.fromJson(target.payload)
                                                            val endpoint = obj.points.lastOrNull()
                                                            val resizeEndpoint = endpoint != null && kotlin.math.hypot((endpoint.x-start.x).toDouble(), (endpoint.y-start.y).toDouble()) < .045
                                                            val moved = if (resizeEndpoint && obj.points.size >= 2) {
                                                                obj.points.dropLast(1) + AnnotationGeometry.normalized(draft.last().x, draft.last().y)
                                                            } else obj.points.map { AnnotationGeometry.normalized(it.x + dx, it.y + dy) }
                                                            saveSnapshot()
                                                            scope.launch(Dispatchers.IO) {
                                                                db.annotations().upsert(target.copy(payload = obj.copy(points = moved).toJson()))
                                                            }
                                                        }
                                                    }
                                                }
                                                else -> {
                                                    saveSnapshot()
                                                    val kind = when (tool) {
                                                        "HIGHLIGHT" -> "HIGHLIGHT"; "LINE" -> "LINE"; "ARROW" -> "ARROW"
                                                        "RECTANGLE" -> "RECTANGLE"; "CIRCLE" -> "CIRCLE"; else -> "PEN"
                                                    }
                                                    persist(InkObject(UUID.randomUUID().toString(), kind, draft, color, width, if (kind == "HIGHLIGHT") alpha else 1f))
                                                }
                                            }
                                        }
                                        draft = emptyList()
                                    }
                                )
                            }
                        ) {
                            pageRows.forEach { row ->
                                val obj = runCatching { InkObject.fromJson(row.payload) }.getOrNull() ?: return@forEach
                                val pts = obj.points.map { Offset(it.x * size.width, it.y * size.height) }
                                val stroke = Stroke(width = (obj.width * size.width * if (obj.kind == "HIGHLIGHT") 5f else 1f).coerceAtLeast(1f))
                                val c = Color(obj.color).copy(alpha = obj.alpha)
                                when (obj.kind) {
                                    "MARKER" -> if (pts.isNotEmpty()) {
                                        val p = pts[0]
                                        drawRoundRect(Color(obj.color), Offset(p.x, p.y), androidx.compose.ui.geometry.Size(size.width*.055f, size.height*.038f), androidx.compose.ui.geometry.CornerRadius(5f))
                                        drawRect(Color.White.copy(alpha=.35f), Offset(p.x,p.y), androidx.compose.ui.geometry.Size(size.width*.055f,size.height*.007f))
                                    }
                                    "RECTANGLE" -> if (pts.size >= 2) drawRect(c, pts[0], androidx.compose.ui.geometry.Size(pts[1].x-pts[0].x, pts[1].y-pts[0].y), style=stroke)
                                    "CIRCLE" -> if (pts.size >= 2) drawOval(c, Offset(minOf(pts[0].x,pts[1].x),minOf(pts[0].y,pts[1].y)), androidx.compose.ui.geometry.Size(kotlin.math.abs(pts[1].x-pts[0].x),kotlin.math.abs(pts[1].y-pts[0].y)), style=stroke)
                                    "LINE", "ARROW" -> if (pts.size >= 2) drawLine(c, pts[0], pts[1], stroke.width)
                                    else -> if (pts.size >= 2) {
                                        val path = Path().apply { moveTo(pts[0].x,pts[0].y); pts.drop(1).forEach { lineTo(it.x,it.y) } }
                                        drawPath(path, c, style=stroke)
                                    } else if (pts.isNotEmpty()) drawCircle(c, 2.dp.toPx(), pts[0])
                                }
                            }
                            if (draft.isNotEmpty() && tool !in listOf("ERASER","SELECT")) {
                                val pts = draft.map { Offset(it.x*size.width,it.y*size.height) }
                                if (pts.size > 1) {
                                    val path = Path().apply { moveTo(pts[0].x,pts[0].y); pts.drop(1).forEach { lineTo(it.x,it.y) } }
                                    drawPath(path, Color(color).copy(alpha=if(tool=="HIGHLIGHT") alpha else 1f), style=Stroke(width=(width*size.width*(if(tool=="HIGHLIGHT")5 else 1)).coerceAtLeast(1f)))
                                }
                            }
                        }
                    }
                } else CircularProgressIndicator()
            }

            Row(Modifier.fillMaxWidth().background(Color(0xFFFCFBF8)).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { page = (page - 1).coerceAtLeast(0) }, enabled = page > 0) { Icon(Icons.Default.ArrowBack, "Previous page") }
                Spacer(Modifier.weight(1f))
                Text(label("Page ${page + 1} / $pageCount", "صفحة ${page + 1} من $pageCount"), fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { page = (page + 1).coerceAtMost(pageCount - 1) }, enabled = page < pageCount - 1) { Icon(Icons.Default.ArrowForward, "Next page") }
            }
        }
    }

    if (showMarkers) {
        AlertDialog(
            onDismissRequest = { showMarkers = false },
            title = { Text(label("Important markers", "العلامات المهمة")) },
            text = {
                if (markerRows.isEmpty()) Text(label("No markers in this document yet.", "لا توجد علامات بعد."))
                else LazyColumn(Modifier.heightIn(max = 420.dp)) {
                    items(markerRows) { row ->
                        val obj = runCatching { InkObject.fromJson(row.payload) }.getOrNull()
                        Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(13.dp).background(Color(obj?.color ?: 0xFFFFC107.toInt()), RoundedCornerShape(3.dp)))
                            Column(Modifier.weight(1f).padding(start = 8.dp)) {
                                Text("Page ${row.pageIndex + 1}", fontWeight = FontWeight.SemiBold)
                                Text(obj?.note?.ifBlank { "No note" } ?: "Marker", fontSize = 12.sp, color = Color.Gray)
                            }
                            TextButton(onClick = { page = row.pageIndex; showMarkers = false }) { Text("Go") }
                            TextButton(onClick = { markerNoteTarget = row.id; noteText = obj?.note.orEmpty(); markerEditColor = obj?.color ?: 0xFFFFC107.toInt() }) { Text("Edit") }
                            IconButton(onClick = { saveSnapshot(); deleteRow(row) }) { Icon(Icons.Default.Delete, "Delete marker") }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showMarkers = false }) { Text(label("Close", "إغلاق")) } }
        )
    }
    if (markerNoteTarget != null) {
        val targetRow = rows.firstOrNull { it.id == markerNoteTarget }
        AlertDialog(
            onDismissRequest = { markerNoteTarget = null },
            title = { Text("Edit paper flag") },
            text = {
                Column {
                    TextField(value = noteText, onValueChange = { noteText = it }, label = { Text("Optional note") }, singleLine = true)
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 12.dp)) {
                        palette.forEach { swatch ->
                            Box(Modifier.padding(end = 8.dp).size(26.dp).background(swatch, CircleShape)
                                .then(if (markerEditColor == swatch.value.toLong().toInt()) Modifier.border(2.dp, Color.Black, CircleShape) else Modifier)
                                .pointerInput(swatch) { detectTapGestures { markerEditColor = swatch.value.toLong().toInt() } })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (targetRow != null) {
                        val obj = InkObject.fromJson(targetRow.payload)
                        saveSnapshot()
                        scope.launch(Dispatchers.IO) {
                            db.annotations().upsert(targetRow.copy(payload = obj.copy(note = noteText.trim(), color = markerEditColor).toJson()))
                        }
                    }
                    markerNoteTarget = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { markerNoteTarget = null }) { Text("Cancel") } }
        )
    }

    if (showCustomColor) {
        AlertDialog(
            onDismissRequest = { showCustomColor = false },
            title = { Text("Custom color") },
            text = { TextField(value = customHex, onValueChange = { customHex = it }, label = { Text("#RRGGBB") }, singleLine = true) },
            confirmButton = {
                TextButton(onClick = {
                    runCatching { color = android.graphics.Color.parseColor(if (customHex.startsWith("#")) customHex else "#$customHex") }
                    showCustomColor = false
                }) { Text("Apply") }
            },
            dismissButton = { TextButton(onClick = { showCustomColor = false }) { Text("Cancel") } }
        )
    }

    }

}
