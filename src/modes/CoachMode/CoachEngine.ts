export interface ChessMoveResult {
    move: string;
    evaluation: 'brilliant' | 'good' | 'blunder' | 'mistake';
    commentary: string;
}

export class CoachEngine {
    private playerStats = { wins: 0, losses: 0, draws: 0, currentLevel: 'المبتدئ الطموح' };

    public evaluateMove(moveName: string, quality: 'brilliant' | 'good' | 'blunder' | 'mistake'): ChessMoveResult {
        let commentary = '';
        switch (quality) {
            case 'brilliant':
            commentary = 'يا سلام! نقلة عبقرية كأنك غاري كاسباروف بعصره الذهبي، الملك المنافس يبكي في الزاوية! 👑🔥';
            break;
            case 'good':
            commentary = 'حركة جيدة ومحترمة، يبدو أنك قرأت كتاب شطرنج واحد على الأقل في حياتك! 😉';
            break;
            case 'mistake':
            commentary = 'أففف، هذه النقلة متوسطة الذكاء.. هل قطعت تدفق الدم لدماغك لتأتي بهذه الفكرة؟ 🤨';
            break;
            case 'blunder':
            commentary = 'كارثة! فادحة كبرى! لقد قدمت حصانك على طبق من ذهب للخصم.. هل تشجع الخصم أم تلعب ضده يا رجل؟! 😂💀';
            break;
        }
        return { move: moveName, evaluation: quality, commentary };
    }

    public getStats() {
        return this.playerStats;
    }
}
export const coachEngine = new CoachEngine();
