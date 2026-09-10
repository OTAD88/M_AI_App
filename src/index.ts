import { modeManager } from './modes/ModeManager';
import { arabicTTS } from './components/ArabicTTS';
import { coachEngine } from './modes/CoachMode/CoachEngine';
import { uiController } from './components/UIController';

export class M_AI_AppCore {
    constructor() {
        console.log("M_AI_App initialized successfully with all engines!");
    }

    public startSystem() {
        modeManager.setMode('coach');
        arabicTTS.speak("أهلاً بك يا مليون، نظام المدرب والألعاب جاهز للعمل.");
        const sampleMove = coachEngine.evaluateMove('Nf3', 'brilliant');
        console.log(sampleMove.commentary);
    }
}

const app = new M_AI_AppCore();
app.startSystem();
