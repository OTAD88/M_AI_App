export class CloudGallery {
    constructor() {
        this.cloudStorage = [];
        this.albums = {};
    }

    // الخيار الأول: نقل الكل تلقائياً للكلود بدون أي تكرار
    syncAllWithoutDuplicates(localFiles) {
        let addedCount = 0;
        localFiles.forEach(file => {
            const exists = this.cloudStorage.some(item => item.hash === file.hash || item.name === file.name);
            if (!exists) {
                this.cloudStorage.push(file);
                addedCount++;
            }
        });
        console.log(`[المعرض السحابي - نقل الكل]: تمت مزامنة ${addedCount} ملفاً جديداً بنجاح (بدون تكرار).`);
        return addedCount;
    }

    // الخيار الثاني: الرفع اليدوي المباشر
    uploadManually(file) {
        const exists = this.cloudStorage.some(item => item.name === file.name);
        if (!exists) {
            this.cloudStorage.push(file);
            console.log(`[المعرض السحابي - رفع يدوي]: تم رفع الملف "${file.name}" بنجاح.`);
            return true;
        }
        console.log(`[تنبيه]: الملف "${file.name}" موجود مسبقاً في السحابة.`);
        return false;
    }

    // الخيار الثالث: الألبومات المخصصة (ترتيب ونقل الصور/الملف حسب الألبوم)
    createCustomAlbum(albumName, files) {
        this.albums[albumName] = files;
        console.log(`[المعرض السحابي - ألبومات مخصصة]: تم إنشـاء الألبوم "${albumName}" ويحتوي على ${files.length} عنصراً.`);
        return true;
    }
}
export const cloudGallery = new CloudGallery();
