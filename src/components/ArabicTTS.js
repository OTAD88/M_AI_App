export class ArabicTTS {
    constructor() {
        this.synth = typeof window !== 'undefined' && 'speechSynthesis' in window ? window.speechSynthesis : null;
    }

    speak(text, onEnd) {
        if (!this.synth) {
            console.log("TTS Simulation (Termux Console):", text);
            if (onEnd) onEnd();
            return;
        }
        this.synth.cancel();
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.lang = 'ar-SA';
        utterance.rate = 1.0;
        utterance.pitch = 1.0;
        if (onEnd) utterance.onend = onEnd;
        this.synth.speak(utterance);
    }
}
export const arabicTTS = new ArabicTTS();
