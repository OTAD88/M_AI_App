export class VoiceVisionButton {
    public render(): string {
        return `
            <button id="voice-vision-btn" class="rounded-full w-10 h-10 flex items-center justify-center bg-blue-600 text-white shadow-lg active:scale-95 transition-transform" title="محادثة صوتية ومرئية">
                <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
                </svg>
            </button>
        `;
    }
}
