import EventEmitter from 'eventemitter3';

interface GooglePlayData {
    score?: number;
    achievementId?: string;
    leaderBoardId?: string;
}

class GooglePlayServices extends EventEmitter {
    public constructor() {
        super();
    }

    public login(): Promise<boolean> {
        return this.promisfyCordovaCall('CordovaGooglePlayServices', 'login');
    }

    public submitScore(leaderBoardId: string, score: number): Promise<any> {
        return this.promisfyCordovaCall('CordovaGooglePlayServices', 'submitScore', [{
            leaderBoardId: leaderBoardId,
            score: score
        }]);
    }

    public unlockAchievement(achievementId: string): Promise<any> {
        return this.promisfyCordovaCall('CordovaGooglePlayServices', 'unlockAchievement', [{
            achievementId: achievementId
        }]);
    }

    public showLeaderboard(leaderBoardId: string): Promise<any> {
        return this.promisfyCordovaCall('CordovaGooglePlayServices', 'showLeaderboard', [{
            leaderBoardId: leaderBoardId
        }]);
    }

    public showAchievements(): Promise <any> {
        return this.promisfyCordovaCall('CordovaGooglePlayServices', 'showAchievements');
    }

    private promisfyCordovaCall(service: string, action: string, data?: [GooglePlayData]): Promise<any> {
        return new Promise((resolve, reject) => {
            cordova.exec(
                (result: any) => resolve(result),
                (error: any) => reject(error),
                service, action, data
            );
        });
    }
}

export namespace Azerion {
    export const playServices = new GooglePlayServices();
}