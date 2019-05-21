import EventEmitter from 'eventemitter3';
import {Logger, LogLevel} from './Logger';

interface GooglePlayData {
    score?: number;
    achievementId?: string;
    leaderBoardId?: string;
}

class GooglePlayServices extends EventEmitter {
    private debug: boolean;

    public constructor(debug: boolean = false) {
        super();

        this.debug = debug;

        if (this.debug) {
            Logger.setLogLevel(LogLevel.DEBUG);
        }
    }

    public login(): void {
        cordova.exec(
            (result: any) => {
                Logger.d('Cordova connection event: ', result);
                this.emit('EVENT', result);
            },
            (error: any) => {
                Logger.d('Cordova connection error: ', error);
                this.emit('ERROR', error);
            },
            'CordovaGooglePlayServices', 'login', [{}]
        );
    }

    public isSignedIn(): Promise<boolean> {
        return this.promisfyCordovaCall('CordovaGooglePlayServices', 'isSignedIn').then((result: string) => {
            return result === "true";
        });
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

    public showAchievements(): Promise<any> {
        return this.promisfyCordovaCall('CordovaGooglePlayServices', 'showAchievements');
    }

    public getPlayerName(): Promise<string> {
        return this.promisfyCordovaCall('CordovaGooglePlayServices', 'getDisplayName');
    }

    private promisfyCordovaCall(service: string, action: string, data: [GooglePlayData] = [{}]): Promise<any> {
        console.log('setting up new promis!', service, action, data);
        return new Promise((resolve, reject) => {
            cordova.exec(
                (result: any) => {
                    Logger.d('Cordova Promise Result', result);
                    resolve(result);
                },
                (error: any) => {
                    Logger.d('Cordova Promise Error', error);

                    reject(error);
                },
                service, action, data
            );
        });
    }
}

export namespace Azerion {
    export const playServices = new GooglePlayServices(true);
}

(window as any).cpgps = Azerion.playServices;
