export class FeatureManager {
    constructor() {
        // قائمة المميزات المتاحة (يتم تحديثها تلقائياً من وضع المطور)
        this.availableFeatures = [
            { id: 'syntax_highlighter', name: 'محلل الأكواد', icon: '💻' },
            { id: 'quick_translate', name: 'الترجمة الفورية', icon: '🌐' },
            { id: 'math_solver', name: 'الحاسبة المتقدمة', icon: '🔢' }
        ];

        // المربعين الديناميكيّين بجانب زر (+)
        this.slots = [
            { slotIndex: 1, activeFeature: 'syntax_highlighter' },
            { slotIndex: 2, activeFeature: null }
        ];
    }

    // إضافة ميزة جديدة برمجياً (تأتي من وضع المطور ذو الخلفية السوداء والنصوص الخضراء)
    addNewFeature(id, name, icon) {
        const exists = this.availableFeatures.find(f => f.id === id);
        if (!exists) {
            this.availableFeatures.push({ id, name, icon });
            console.log(`[وضع المطور]: تمت إضافة الميزة الجديدة "${name}" (${icon}) بنجاح وتحديث القائمة.`);
            return true;
        }
        return false;
    }

    // الضغط العادي: تفعيل الميزة للاستخدام
    onShortPress(slotIndex) {
        const slot = this.slots.find(s => s.slotIndex === slotIndex);
        if (slot && slot.activeFeature) {
            console.log(`[تفعيل ميزة]: تم تفعيل الميزة رقم ${slotIndex} (${slot.activeFeature}) للاستخدام الفوري.`);
            return { action: 'execute', feature: slot.activeFeature };
        } else {
            console.log(`[مربع فارغ]: فتح قائمة اختيار ميزة للمربع ${slotIndex}...`);
            return { action: 'open_list', slotIndex };
        }
    }

    // الضغط المطول: فتح القائمة لتبديل الميزة أو تغييرها
    onLongPress(slotIndex) {
        console.log(`[تبديل ميزة]: ضغطة مطولة على المربع ${slotIndex} - جارٍ فتح قائمة الميزات للاختيار والتبديل...`);
        return { action: 'open_list_for_swap', slotIndex, available: this.availableFeatures };
    }

    // تعيين ميزة في أحد المربعين
    assignFeatureToSlot(slotIndex, featureId) {
        const slot = this.slots.find(s => s.slotIndex === slotIndex);
        const feature = this.availableFeatures.find(f => f.id === featureId);
        if (slot && feature) {
            slot.activeFeature = featureId;
            console.log(`[تحديث الواجهة]: تم وضع الميزة "${feature.name}" (${feature.icon}) في المربع الديناميكي ${slotIndex}.`);
            return true;
        }
        return false;
    }
}
export const featureManager = new FeatureManager();
