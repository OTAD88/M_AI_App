package com.otad.risala

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Mist = Color(0xFFAFC9D5)
private val Ink = Color(0xFF26343A)
private val Muted = Color(0xFF7A898D)
private val Paper = Color(0xFFF7F8F7)
private val contactsSeed = listOf(
    Contact("مايا باتيل", "صانعة مساحة للأشياء الجميلة", "MP", "ST • 6932 8041"),
    Contact("جيمي تشين", "قهوة وأحاديث جميلة", "JC", "ST • 5729 1084"),
    Contact("أوليفيا بروكس", "يوماً بعد يوم", "OB", "ST • 8401 5726"),
    Contact("إيثان ريد", "هنا للحظات الصغيرة", "ER", "ST • 2048 6193"),
    Contact("نوح ويليامز", "غالباً في الخارج", "NW", "ST • 3186 4207")
)

data class Contact(val name: String, val note: String, val initials: String, val id: String)
private enum class Page { CONTACTS, CHAT, PROFILE, APPEARANCE, EDIT_PROFILE, VERIFY, PRIVACY, NOTIFICATIONS, LANGUAGE, HELP }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RisalaApp() }
    }
}

@Composable
private fun RisalaApp() {
    var page by remember { mutableStateOf(Page.CONTACTS) }
    var selected by remember { mutableStateOf(contactsSeed[1]) }
    var contacts by remember { mutableStateOf(contactsSeed) }
    var displayName by remember { mutableStateOf("أليكس مورغان") }
    var darkMode by remember { mutableStateOf(false) }
    var accent by remember { mutableStateOf(Mist) }
    var customHex by remember { mutableStateOf("#AFC9D5") }
    var showAdd by remember { mutableStateOf(false) }
    var addName by remember { mutableStateOf("") }
    var addId by remember { mutableStateOf("") }
    var readReceipts by remember { mutableStateOf(true) }
    var notifications by remember { mutableStateOf(true) }
    var onlineStatus by remember { mutableStateOf(true) }

    val scheme = if (darkMode) darkColorScheme(
        primary = accent, onPrimary = Color(0xFF17252B),
        background = Color(0xFF11191D), surface = Color(0xFF1B252A),
        onBackground = Color(0xFFE7ECEE), onSurface = Color(0xFFE7ECEE),
        secondary = Mist
    ) else lightColorScheme(
        primary = accent, onPrimary = Ink,
        background = Paper, surface = Color.White,
        onBackground = Ink, onSurface = Ink, secondary = Muted
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = scheme) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                when (page) {
                    Page.CONTACTS -> ContactsPage(
                        contacts = contacts,
                        onOpenProfile = { page = Page.PROFILE },
                        onOpenChat = { selected = it; page = Page.CHAT },
                        onAdd = { showAdd = true }
                    )
                    Page.CHAT -> ChatPage(contact = selected, onBack = { page = Page.CONTACTS })
                    Page.PROFILE -> ProfilePage(
                        displayName = displayName,
                        onBack = { page = Page.CONTACTS },
                        onAppearance = { page = Page.APPEARANCE },
                        onEdit = { page = Page.EDIT_PROFILE },
                        onVerify = { page = Page.VERIFY },
                        onPrivacy = { page = Page.PRIVACY },
                        onNotifications = { page = Page.NOTIFICATIONS },
                        onLanguage = { page = Page.LANGUAGE },
                        onHelp = { page = Page.HELP }
                    )
                    Page.APPEARANCE -> AppearancePage(
                        darkMode = darkMode,
                        onDarkMode = { darkMode = it },
                        accent = accent,
                        onAccent = { accent = it; customHex = "#%02X%02X%02X".format(it.redInt(), it.greenInt(), it.blueInt()) },
                        customHex = customHex,
                        onHexChange = { customHex = it },
                        onApplyHex = {
                            parseHex(customHex)?.let { accent = it }
                        },
                        onBack = { page = Page.PROFILE }
                    )
                    Page.EDIT_PROFILE -> EditProfilePage(
                        name = displayName,
                        onNameChange = { displayName = it },
                        onBack = { page = Page.PROFILE }
                    )
                    Page.VERIFY -> VerifyPage(onBack = { page = Page.PROFILE }, displayName = displayName)
                    Page.PRIVACY -> SettingsPage(
                        title = "الخصوصية والأمان", onBack = { page = Page.PROFILE }
                    ) {
                        SettingToggle("إظهار حالة الاتصال", "يمكن لجهات اتصالك معرفة أنك متصل", onlineStatus) { onlineStatus = it }
                        SettingRow("معرّف رسالة", "لا حاجة إلى مشاركة رقم الهاتف", Icons.Outlined.Fingerprint)
                        SettingRow("التحقق من الهوية", "تحكم بهويتك بنفسك", Icons.Outlined.VerifiedUser) { page = Page.VERIFY }
                    }
                    Page.NOTIFICATIONS -> SettingsPage(
                        title = "الإشعارات", onBack = { page = Page.PROFILE }
                    ) {
                        SettingToggle("الإشعارات", "تنبيهات الرسائل الجديدة", notifications) { notifications = it }
                        SettingToggle("إيصالات القراءة", "إظهار وقت قراءة الرسائل", readReceipts) { readReceipts = it }
                    }
                    Page.LANGUAGE -> SettingsPage(
                        title = "اللغة", onBack = { page = Page.PROFILE }
                    ) {
                        SettingRow("العربية", "لغة التطبيق الحالية", Icons.Outlined.CheckCircle)
                    }
                    Page.HELP -> SettingsPage(
                        title = "المساعدة والدعم", onBack = { page = Page.PROFILE }
                    ) {
                        SettingRow("مركز المساعدة", "إرشادات استخدام رسالة", Icons.Outlined.HelpOutline)
                        SettingRow("حول التطبيق", "رسالة 1.0 · تواصل أقرب، بلا ضجيج", Icons.Outlined.Info)
                    }
                }
            }
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("إضافة شخص") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("اختر شخصاً جديداً للاتصال به عبر معرّف رسالة.")
                    OutlinedTextField(addName, { addName = it }, label = { Text("الاسم") }, singleLine = true)
                    OutlinedTextField(addId, { addId = it }, label = { Text("معرّف رسالة") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (addName.isNotBlank()) {
                        val initials = addName.trim().take(2)
                        contacts = contacts + Contact(addName.trim(), "جهة اتصال جديدة", initials, addId.ifBlank { "ST • —" })
                    }
                    addName = ""; addId = ""; showAdd = false
                }) { Text("إضافة") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("إلغاء") } }
        )
    }
}

private fun Color.redInt() = (red * 255).toInt()
private fun Color.greenInt() = (green * 255).toInt()
private fun Color.blueInt() = (blue * 255).toInt()
private fun parseHex(value: String): Color? = try {
    val clean = value.trim().removePrefix("#")
    if (clean.length == 6) Color(android.graphics.Color.parseColor("#$clean")) else null
} catch (_: Exception) { null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactsPage(
    contacts: List<Contact>,
    onOpenProfile: () -> Unit,
    onOpenChat: (Contact) -> Unit,
    onAdd: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("رسالة", fontWeight = FontWeight.SemiBold) },
                actions = { IconButton(onClick = onOpenProfile) { Icon(Icons.Outlined.AccountCircle, "الملف الشخصي") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Outlined.Add, "إضافة شخص")
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Outlined.People, null) }, label = { Text("جهات الاتصال") })
                NavigationBarItem(selected = false, onClick = onOpenProfile, icon = { Icon(Icons.Outlined.Person, null) }, label = { Text("ملفي") })
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(8.dp))
            Text("دائرتك الخاصة", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium)
            Text("قرب أكثر، بلا ضجيج.", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 4.dp, bottom = 18.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("جهات الاتصال", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Spacer(Modifier.weight(1f))
                Text("${contacts.size} أشخاص", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
            }
            Spacer(Modifier.height(10.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                items(contacts) { contact ->
                    ContactCard(contact = contact, onClick = { onOpenChat(contact) })
                }
            }
        }
    }
}

@Composable
private fun ContactCard(contact: Contact, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(contact.initials, 48)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(contact.name, fontWeight = FontWeight.SemiBold)
                Text(contact.note, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
            }
            Icon(Icons.Outlined.ChevronLeft, null, tint = MaterialTheme.colorScheme.secondary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatPage(contact: Contact, onBack: () -> Unit) {
    var draft by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(listOf(
            "مرحباً! هل نلتقي قرب الماء لاحقاً؟" to false,
            "بالتأكيد. الساعة الخامسة والنصف في المكان المعتاد؟" to true,
            "اتفقنا. أراك قريباً!" to false,
            "سأحضر القهوة!" to true
        ))
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text(contact.name, fontWeight = FontWeight.SemiBold); Text("متصل الآن", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary) } },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowForward, "رجوع") } },
                actions = { Avatar(contact.initials, 38) }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 14.dp)) {
            Text("هذه المحادثة بينك وبين ${contact.name} فقط", color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), textAlign = TextAlign.Center)
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), reverseLayout = true, verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 10.dp)) {
                items(messages.reversed()) { item ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (item.second) Arrangement.Start else Arrangement.End) {
                        Surface(
                            color = if (item.second) MaterialTheme.colorScheme.primary.copy(alpha = 0.28f) else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(18.dp)
                        ) { Text(item.first, Modifier.widthIn(max = 280.dp).padding(horizontal = 14.dp, vertical = 11.dp)) }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = draft, onValueChange = { draft = it }, modifier = Modifier.weight(1f),
                    placeholder = { Text("اكتب رسالة…") }, shape = RoundedCornerShape(24.dp), maxLines = 4
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { if (draft.isNotBlank()) { messages = messages + (draft to true); draft = "" } }) {
                    Icon(Icons.Outlined.Send, "إرسال", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfilePage(
    displayName: String, onBack: () -> Unit, onAppearance: () -> Unit, onEdit: () -> Unit,
    onVerify: () -> Unit, onPrivacy: () -> Unit, onNotifications: () -> Unit,
    onLanguage: () -> Unit, onHelp: () -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text("الملف والإعدادات") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowForward, "رجوع") } }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Avatar("AM", 66)
                        Spacer(Modifier.height(10.dp))
                        Text(displayName, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                        Text("ST • 1024 0876", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                        Text("رسالة 1.0 · تواصل أقرب، بلا ضجيج", color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                        TextButton(onClick = onEdit) { Text("تعديل الملف الشخصي") }
                    }
                }
            }
            item { SectionLabel("تفضيلاتك") }
            item { SettingRow("المظهر واللون", "اختر المظهر واللون المفضل", Icons.Outlined.Palette, onAppearance) }
            item { SettingRow("اللغة", "العربية", Icons.Outlined.Language, onLanguage) }
            item { SettingRow("الإشعارات", "إعدادات التنبيه وإيصالات القراءة", Icons.Outlined.Notifications, onNotifications) }
            item { SettingRow("الخصوصية والأمان", "تحكم بمعلوماتك وهويتك", Icons.Outlined.Lock, onPrivacy) }
            item { SettingRow("التحقق من الهوية", "تأكيد هويتك بشكل خاص", Icons.Outlined.Fingerprint, onVerify) }
            item { SettingRow("المساعدة والدعم", "معلومات ومساعدة التطبيق", Icons.Outlined.HelpOutline, onHelp) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearancePage(
    darkMode: Boolean, onDarkMode: (Boolean) -> Unit, accent: Color, onAccent: (Color) -> Unit,
    customHex: String, onHexChange: (String) -> Unit, onApplyHex: () -> Unit, onBack: () -> Unit
) {
    val colors = listOf(Mist, Color(0xFFB9C8B3), Color(0xFFD6B9AF), Color(0xFFC3B9D8), Color(0xFFE0C98D), Color(0xFF9FC4C0))
    Scaffold(topBar = { TopAppBar(title = { Text("المظهر والألوان") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowForward, "رجوع") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("اجعل رسالة تشبهك", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium)
            Text("لون هادئ ومساحة تشبهك.", color = MaterialTheme.colorScheme.secondary)
            SettingToggle("الوضع الداكن", "راحة أكبر في الإضاءة المنخفضة", darkMode, onDarkMode)
            Text("لون الواجهة", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                colors.forEach { color ->
                    Box(Modifier.size(42.dp).background(color, CircleShape).clickable { onAccent(color) }, contentAlignment = Alignment.Center) {
                        if (accent == color) Icon(Icons.Outlined.Check, null, tint = Ink)
                    }
                }
            }
            OutlinedTextField(customHex, onHexChange, label = { Text("لون مخصص (HEX)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = onApplyHex, modifier = Modifier.fillMaxWidth()) { Text("استخدام هذا اللون") }
            Text("يمكن تغيير اللون والمظهر في أي وقت.", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfilePage(name: String, onNameChange: (String) -> Unit, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("تعديل الملف الشخصي") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowForward, "رجوع") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("اجعل المكان لك", style = MaterialTheme.typography.headlineSmall)
            Text("اسمك هو هويتك.", color = MaterialTheme.colorScheme.secondary)
            OutlinedTextField(name, onNameChange, label = { Text("الاسم المعروض") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar("AM", 46); Spacer(Modifier.width(12.dp))
                    Column { Text("معرّف رسالة"); Text("ST • 1024 0876", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp) }
                }
            }
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("حفظ التغييرات") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerifyPage(onBack: () -> Unit, displayName: String) {
    Scaffold(topBar = { TopAppBar(title = { Text("تأكيد الهوية") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowForward, "رجوع") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Outlined.Fingerprint, null, modifier = Modifier.size(84.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(22.dp))
            Text("هويتك مؤكدة", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text("يمكنك التواصل باسم $displayName. بصمتك لا تتم مشاركتها مع جهات اتصالك.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(vertical = 14.dp))
            Text("ST • 6831 2905", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(22.dp))
            Button(onClick = onBack) { Text("العودة إلى جهات الاتصال") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsPage(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(title) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowForward, "رجوع") } }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun SettingToggle(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Medium); Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary) }
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit = {}) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Medium); Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary) }
            Icon(Icons.Outlined.ChevronLeft, null, tint = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp, bottom = 2.dp))
}

@Composable
private fun Avatar(initials: String, size: Int) {
    Box(Modifier.size(size.dp).background(Mist.copy(alpha = 0.75f), CircleShape), contentAlignment = Alignment.Center) {
        Text(initials, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = (size / 3.2f).sp)
    }
}
