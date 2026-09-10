export type AppMode = 'normal' | 'teacher' | 'coach';

export class ModeManager {
    private currentMode: AppMode = 'normal';

    public setMode(mode: AppMode): void {
        this.currentMode = mode;
        console.log(`Switched to mode: ${this.currentMode}`);
    }

    public getMode(): AppMode {
        return this.currentMode;
    }
}
export const modeManager = new ModeManager();
