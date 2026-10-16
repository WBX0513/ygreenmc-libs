package net.ygreenmc.util.logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ColorLogger extends Logger {

    private static final String RESET = "\u001B[0m";

    private static final Map<String, ColorLogger> CACHE = new ConcurrentHashMap<>();

    protected ColorLogger(String name) {
        super(name);
    }

    public static ColorLogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static ColorLogger getLogger(String name) {
        return CACHE.computeIfAbsent(name, ColorLogger::new);
    }

    @Override
    protected void writeToConsole(LogLevel level, String line) {
        String out = level.getColor() + line + RESET;

        if (level.getSeverity() >= LogLevel.ERROR.getSeverity()) {
            System.err.println(out);
        } else {
            System.out.println(out);
        }
    }
}