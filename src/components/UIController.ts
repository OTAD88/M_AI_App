export type IconShape = 'sharp' | 'smooth' | 'circle';

export class UIController {
    private currentIconShape: IconShape = 'smooth';

    public setIconShape(shape: IconShape): void {
        this.currentIconShape = shape;
    }

    private getShapeClass(isSpecialVoiceButton: boolean = false): string {
        if (isSpecialVoiceButton) {
            return 'rounded-full'; // زر المكالمة والكاميرا بجانب الإرسال يبقى دائرياً حصراً
        }
        switch (this.currentIconShape) {
            case 'sharp': return 'rounded-none';
            case 'smooth': return 'rounded-xl';
            case 'circle': return 'rounded-full';
            default: return 'rounded-xl';
        }
    }

    public renderMainInterface(): string {
        const standardShape = this.getShapeClass(false);
        const voiceButtonShape = this.getShapeClass(true);

        return `
            <div id="m-ai-interface" class="flex flex-col h-full justify-between p-4 bg-gray-900 text-white">
                <!-- شريط علوي لتبديل الأوضاع -->
                <div class="flex justify-around bg-gray-800 p-2 ${standardShape} shadow-md">
                    <button id="mode-normal" class="px-3 py-1 ${standardShape} bg-blue-600 font-bold">الوضع العادي</button>
                    <button id="mode-teacher" class="px-3 py-1 ${standardShape} bg-gray-700">وضع المعلم</button>
                    <button id="mode-coach" class="px-3 py-1 ${standardShape} bg-gray-700">وضع المدرب 🎮</button>
                </div>

                <!-- منطقة المحادثة -->
                <div id="chat-stream" class="flex-1 overflow-y-auto my-4 p-2 space-y-2">
                    <div class="text-center text-gray-400 text-sm">أهلاً بك يا مليون، رفيقك الذكي والألعاب جاهز!</div>
                </div>

                <!-- شريط الإرسال والأزرار -->
                <div class="flex items-center gap-2 bg-gray-800 p-2 ${standardShape}">
                    <input type="text" id="user-input" placeholder="اكتب رسالتك أو ابدأ التدريب..." class="flex-1 bg-transparent px-3 text-white outline-none" />
                    
                    <!-- زر المكالمة/الكاميرا الدائري المستقل بجانب زر الإرسال -->
                    <button id="voice-vision-btn" class="w-10 h-10 ${voiceButtonShape} bg-blue-600 flex items-center justify-center shadow-lg active:scale-95 transition-transform" title="محادثة صوتية ومرئية">
                        🎥🎙️
                    </button>

                    <!-- زر الإرسال العادي بتنسيق الشكل المختار -->
                    <button id="send-btn" class="w-10 h-10 ${standardShape} bg-green-600 flex items-center justify-center shadow-lg">
                        ⬆️
                    </button>
                </div>
            </div>
        `;
    }
}
export const uiController = new UIController();
