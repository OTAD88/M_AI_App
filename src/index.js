import { modeManager } from './modes/ModeManager.js';
import { arabicTTS } from './components/ArabicTTS.js';
import { coachEngine } from './modes/CoachMode/CoachEngine.js';

console.log("M_AI_App initialized successfully with all engines!");
modeManager.setMode('coach');
arabicTTS.speak("أهلاً بك يا مليون، نظام المدرب والألعاب جاهز للعمل.");
const sampleMove = coachEngine.evaluateMove('Nf3', 'brilliant');
console.log(sampleMove.commentary);
