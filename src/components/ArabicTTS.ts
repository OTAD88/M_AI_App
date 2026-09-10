export class ArabicTTS {
    private synth: SpeechSynthesis | null = null;

    constructor() {
        if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
            this.synth = window.speechSynthesis;
        }
    }

    public speak(text: string, onEnd?: () => void): void {
        if (!this.synth) {
            console.warn("Speech synthesis not supported.");
            return;
        }

        this.synth.cancel(); // إيقاف أي كلام سابق لتكون الاستجابة فورية
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.lang = 'ar-SA'; // تعيين اللهجة العربية الفصحى الواضحة
        utterance.rate = 1.0; // سرعة طبيعية ومثالية
        utterance.pitch = 1.0; // نبرة صوت طبيعية

        if (onEnd) {
            utterance.onend = onEnd;
        }

        this.synth.speak(utterance);
    }

    public stop(): void {
        if (this.synth) {
            this.synth.cancel();
        }
    }
}
export const arabicTTS = new ArabicTTS();
