export class ModeManager {
    constructor() {
        this.currentMode = 'normal';
    }

    setMode(mode) {
        this.currentMode = mode;
        console.log(`Mode switched to: ${mode}`);
    }

    getMode() {
        return this.currentMode;
    }
}
export const modeManager = new ModeManager();
