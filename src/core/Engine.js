export class Engine {
    constructor() {
        this.speechMode = 'auto'; // 'auto' (تلقائي) أو 'manual' (يدوي)
        this.activeMode = null;   // 'teacher' أو 'coach' أو null
    }

    // تعيين نمط القراءة العام من إعدادات الترس ⚙️
    setSpeechMode(mode) {
        if (mode === 'auto' || mode === 'manual') {
            this.speechMode = mode;
            console.log(`[إعدادات النظام]: نمط القراءة أصبح (${mode === 'auto' ? 'تلقائي' : 'يدوي'}).`);
        }
    }

    // تبديل الأوضاع (المعلم والمدرب لا يجتمعان، تشغيل أحدهما يلغي الآخر)
    setMode(modeName) {
        if (this.activeMode === modeName) {
            this.activeMode = null;
            console.log("[النظام]: تم إيقاف الوضع المخصص والعودة للوضع العادي.");
            return { mode: null, status: "disabled" };
        }
        
        this.activeMode = modeName;
        if (modeName === 'teacher') {
            console.log("[النظام]: تم تفعيل [وضع المعلم] الشبور والمبسط (وإغلاق المدرب تلقائياً).");
        } else if (modeName === 'coach') {
            console.log("[النظام]: تم تفعيل [وضع المدرب] للشطرنج والألعاب (وإغلاق المعلم تلقائياً).");
        }
        return { mode: this.activeMode, status: "enabled" };
    }

    // التعامل مع طريقة نطق رسالة المساعد بناءً على الاختيار (تلقائي أو يدوي)
    handleSpeechOutput(message) {
        if (this.speechMode === 'auto') {
            console.log(`🔊 [قراءة تلقائية مفعلة]: المساعد ينطق الآن: "${message}"`);
        } else {
            console.log(`🔇 [قراءة يدوية]: تم عرض النص بصرياً في الفقاعة بانتظار ضغطك للاستماع.`);
        }
    }

    // معالجة المدخلات حسب الوضع النشط
    processInput(input) {
        let response = "";
        
        if (this.activeMode === 'teacher') {
            response = `[وضع المعلم - الشرح المبسط لوالدتك]: أهلاً بك. لتفهمي درس "${input}" ببساطة شديدة كأنكِ تسمعينه للمرة الأولى...`;
        } 
        else if (this.activeMode === 'coach') {
            response = `[وضع المدرب - الشطرنج]: تحليل النقلة لـ "${input}": وضعك على الرقعة ممتاز، واصل الهجوم! 👑🔥`;
        }
        else {
            response = `[الدردشة العادية]: تم تنفيذ طلبك لـ "${input}" فوراً وبدون أي اعتذارات.`;
        }

        this.handleSpeechOutput(response);
        return response;
    }
}
export const engine = new Engine();
