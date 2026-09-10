export class SettingsController {
    constructor() {
        this.settings = {
            fontFamily: 'Cairo',
            language: 'ar',
            assistantVoice: 'male',          // 'male' أو 'female'
            uiStyle: 'smooth',               // أشكال الأزرار والشريط
            iconColors: 'random_safe',       // ألوان عشوائية مريحة للعين
            themeMode: 'auto',               // 'white', 'black', 'auto' (نهار/ليل), 'custom'
            customColor: {                   // في حال اختيار مخصص عبر عجلة الألوان
                bubbleColor: '#00ff66',
                shadeLevel: 'dark'
            }
        };
    }

    // تحديث أي إعداد بدقة
    updateSetting(key, value) {
        if (this.settings.hasOwnProperty(key)) {
            this.settings[key] = value;
            console.log(`[إعدادات الترس ⚙️]: تم تحديث (${key}) بنجاح.`);
            return true;
        }
        return false;
    }

    // تعيين الثيم المخصص مع عجلة الألوان والغمق
    setCustomTheme(bubbleColor, shadeLevel) {
        this.settings.themeMode = 'custom';
        this.settings.customColor = { bubbleColor, shadeLevel };
        console.log(`[إعدادات المظهر]: تم تطبيق الثيم المخصص بلون الفقاعة (${bubbleColor}) ومستوى الغمق (${shadeLevel}).`);
    }

    getSettings() {
        return this.settings;
    }
}
export const settingsController = new SettingsController();
