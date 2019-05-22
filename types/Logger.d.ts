export declare enum LogLevel {
    DEBUG = 0,
    INFO = 1,
    WARN = 2,
    ERROR = 3
}
/**
 * Logger class with a similar API to Cordova Android log
 * see https://github.com/apache/cordova-android/blob/master/framework/src/org/apache/cordova/LOG.java
 */
export declare class Logger {
    private static started;
    private static level;
    /**
     * Set the visible log level
     *
     * @param level LogLevel
     */
    static setLogLevel(level: LogLevel): void;
    /**
     * Debug logging
     *
     * @param data
     */
    static d(...data: any[]): void;
    /**
     * Default INFO logging
     *
     * @param data
     */
    static i(...data: any[]): void;
    /**
     * warning Logging
     *
     * @param data
     */
    static w(...data: any[]): void;
    /**
     * Error Loggin
     *
     * @param data
     */
    static e(...data: any[]): void;
    private static log;
}
