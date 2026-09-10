import { engine } from '../core/Engine.js';

export class UIController {
    constructor() {
        this.currentMode = 'coach';
    }

    renderMainInterface() {
        return `
            <div id="m-ai-app" class="flex flex-col h-screen w-screen bg-[#0d1117] text-gray-100 font-sans select-none overflow-hidden">
                <div class="flex items-center justify-between px-4 py-3 bg-[#161b22] border-b border-gray-800">
                    <div class="flex items-center gap-2">
                        <span class="w-3 h-3 rounded-full bg-emerald-500 animate-pulse"></span>
                        <span class="text-xs font-bold text-emerald-400 tracking-wider">M_AI_CHESS_COACH</span>
                    </div>
                    <div class="flex items-center gap-3">
                        <button id="toggle-speech-btn" onclick="window.toggleSpeech()" class="text-xs px-2.5 py-1 rounded-full bg-blue-600/20 text-blue-400 border border-blue-500/30 font-medium active:scale-95 transition-all">
                            🔊 القراءة: مفعلة
                        </button>
                    </div>
                </div>

                <div class="flex items-center gap-3 p-3 bg-[#161b22]/60 border-b border-gray-800/60">
                    <div class="relative w-12 h-12 rounded-full bg-gradient-to-tr from-blue-600 to-emerald-500 p-0.5 shadow-lg flex-shrink-0 animate-bounce">
                        <div class="w-full h-full bg-[#0d1117] rounded-full flex items-center justify-center text-xl">
                            🧙‍♂️
                        </div>
                        <span class="absolute bottom-0 right-0 w-3.5 h-3.5 bg-emerald-500 border-2 border-[#0d1117] rounded-full"></span>
                    </div>
                    
                    <div id="coach-speech-bubble" class="flex-1 bg-[#21262d] px-3.5 py-2.5 rounded-2xl border border-gray-700/60 text-xs text-gray-200 shadow-sm leading-relaxed relative">
                        أهلاً بك يا مليون! أنا جاهز لتحليل نقلاتك في الشطرنج. العب النقلة الأولى ودعني أراقبك! 👑🔥
                    </div>
                </div>

                <div class="flex-1 flex flex-col items-center justify-center p-4 bg-[#0d1117]">
                    <div id="board-container" class="w-full max-w-sm aspect-square bg-[#161b22] rounded-2xl border border-gray-800 flex items-center justify-center shadow-2xl relative">
                        <div class="text-center text-gray-500 text-sm">
                            [ رقعة الشطرنج التفاعلية ]
                        </div>
                    </div>
                </div>

                <div class="grid grid-cols-4 gap-2 p-3 bg-[#161b22] border-t border-gray-800">
                    <button onclick="window.simulateMove()" class="flex flex-col items-center justify-center py-2 rounded-xl bg-[#21262d] text-gray-300 hover:text-white text-xs active:scale-95 transition-all">
                        <span>↩️</span>
                        <span class="mt-1 text-[10px]">تراجع</span>
                    </button>
                    <button class="flex flex-col items-center justify-center py-2 rounded-xl bg-[#21262d] text-gray-300 hover:text-white text-xs active:scale-95 transition-all">
                        <span>💡</span>
                        <span class="mt-1 text-[10px]">تلميح</span>
                    </button>
                    <button class="flex flex-col items-center justify-center py-2 rounded-xl bg-[#21262d] text-gray-300 hover:text-white text-xs active:scale-95 transition-all">
                        <span>🏳️</span>
                        <span class="mt-1 text-[10px]">إلغاء</span>
                    </button>
                    <button class="flex flex-col items-center justify-center py-2 rounded-xl bg-[#21262d] text-gray-300 hover:text-white text-xs active:scale-95 transition-all">
                        <span>⚙️</span>
                        <span class="mt-1 text-[10px]">الخيارات</span>
                    </button>
                </div>
            </div>
        `;
    }
}
export const uiController = new UIController();
