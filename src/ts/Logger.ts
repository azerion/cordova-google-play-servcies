export enum LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR
}

/**
 * Logger class with a similar API to Cordova Android log
 * see https://github.com/apache/cordova-android/blob/master/framework/src/org/apache/cordova/LOG.java
 */
export class Logger {

    private static started = Date.now();
    private static level: LogLevel = LogLevel.INFO;

    /**
     * Set the visible log level
     *
     * @param level LogLevel
     */
    public static setLogLevel(level: LogLevel): void {
        this.level = level;
    }

    /**
     * Debug logging
     *
     * @param data
     */
    public static d(...data: any[]): void {
        if (LogLevel.DEBUG >= Logger.level) {
            Logger.log(LogLevel.DEBUG, data);
        }
    }

    /**
     * Default INFO logging
     *
     * @param data
     */
    public static i(...data: any[]): void {
        if (LogLevel.INFO >= Logger.level) {
            Logger.log(LogLevel.INFO, data);
        }
    }

    /**
     * warning Logging
     *
     * @param data
     */
    public static w(...data: any[]): void {
        if (LogLevel.WARN >= Logger.level) {
            Logger.log(LogLevel.WARN, data);
        }
    }

    /**
     * Error Loggin
     *
     * @param data
     */
    public static e(...data: any[]): void {
        if (LogLevel.ERROR >= Logger.level) {
            Logger.log(LogLevel.ERROR, data);
        }
    }

    private static log(level: LogLevel, data: any[]): void {
        console.log([
            '[' + ((Date.now() - Logger.started) / 1000) + 's] CordovaGooglePlay | '+ LogLevel[level] +' | '
        ].concat(data));
        // @ts-ignore
        console.log.apply(console, [
            '[' + ((Date.now() - Logger.started) / 1000) + 's] CordovaGooglePlay | '+ LogLevel[level] +' | '
        ].concat(data));
    }
}

