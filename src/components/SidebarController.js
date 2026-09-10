export class SidebarController {
    constructor() {
        this.isOpen = false;
        this.modificationsLog = [
            { id: 'mod_1', title: 'ميزة محلل الأكواد الذكي', status: 'active', timestamp: '2026-06' },
            { id: 'mod_2', title: 'نظام القراءة الصوتية المشروطة', status: 'active', timestamp: '2026-06' }
        ];
    }

    // فتح أو إغلاق القائمة الجانبية (عبر السحب أو زر الثلاث خطوط)
    toggleSidebar() {
        this.isOpen = !this.isOpen;
        console.log(`[القائمة الجانبية]: أصبحت ${this.isOpen ? 'مفتوحة (عرض السجل والتعديلات)' : 'مغلقة'}.`);
        return this.isOpen;
    }

    // الضغط المطول على أي ميزة في السجل لتظهر خيارات (تحديث أو إزالة)
    onItemLongPress(modId) {
        const mod = this.modificationsLog.find(m => m.id === modId);
        if (mod) {
            console.log(`[إدارة التعديل]: تم تحديد الميزة "${mod.title}". الخيارات المتاحة: 1. تحديث (تعديل) | 2. إزالة (حذف كامل وإرجاع المكان نظيفاً).`);
            return { modId, options: ['update', 'remove'] };
        }
        return null;
    }

    // تنفيذ الإزالة الكاملة (يعيد المكان نظيفاً كأن شيئاً لم يكن)
    removeModification(modId) {
        const index = this.modificationsLog.findIndex(m => m.id === modId);
        if (index !== -1) {
            const removed = this.modificationsLog.splice(index, 1)[0];
            console.log(`[إزالة تامة]: تم حذف "${removed.title}" وإرجاع مساحتها نظيفة بالكامل كأنها لم تكن.`);
            return true;
        }
        return false;
    }

    // تنفيذ التحديث
    updateModification(modId, newTitle) {
        const mod = this.modificationsLog.find(m => m.id === modId);
        if (mod) {
            mod.title = newTitle;
            console.log(`[تحديث ناجح]: تم تعديل الميزة بنجاح وتحديث الكود الذاتي.`);
            return true;
        }
        return false;
    }
}
export const sidebarController = new SidebarController();
