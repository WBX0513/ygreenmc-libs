package net.ygreenmc.util.logger;

public final class LoggerFactory {

    private LoggerFactory() {
    }

    public static ILogger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz);
    }

    public static ILogger getLogger(String name) {
        return Logger.getLogger(name);
    }

    public static ILogger getColorLogger(Class<?> clazz) {
        return ColorLogger.getLogger(clazz);
    }

    public static ILogger getColorLogger(String name) {
        return ColorLogger.getLogger(name);
    }
}