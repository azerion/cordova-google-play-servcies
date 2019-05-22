import EventEmitter from 'eventemitter3';
export declare class GooglePlayServices extends EventEmitter {
    private debug;
    constructor(debug?: boolean);
    login(): void;
    isSignedIn(): Promise<boolean>;
    submitScore(leaderBoardId: string, score: number): Promise<any>;
    unlockAchievement(achievementId: string): Promise<any>;
    showLeaderboard(leaderBoardId: string): Promise<any>;
    showAchievements(): Promise<any>;
    getPlayerName(): Promise<string>;
    private promisfyCordovaCall;
}
