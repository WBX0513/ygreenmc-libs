package net.ygreenmc.util.logger;

public enum LogLevel {
    DEBUG(1, "\u001B[90m"),
    INFO(2, "\u001B[36m"),
    SUCCESS(3, "\u001B[32m"),
    WARNING(4, "\u001B[33m"),
    ERROR(5, "\u001B[31m"),
    EXCEPTION(5, "\u001B[35m"),
    CRITICAL(6, "\u001B[41;97m");

    private final int severity;
    private final String color;

    LogLevel(int severity, String color) {
        this.severity = severity;
        this.color = color;
    }

    public int getSeverity() {
        return severity;
    }

    public String getColor() {
        return color;
    }

    public boolean isEnabled(LogLevel threshold) {
        return this.severity >= threshold.severity;
    }
}