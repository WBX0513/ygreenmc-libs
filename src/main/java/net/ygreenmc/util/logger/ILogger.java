package net.ygreenmc.util.logger;

public interface ILogger {

    void debug(String msg, Object... args);
    void info(String msg, Object... args);
    void success(String msg, Object... args);
    void warning(String msg, Object... args);
    void error(String msg, Object... args);
    void error(String msg, Throwable t, Object... args);
    void exception(String msg, Throwable t, Object... args);
    void critical(String msg, Object... args);
}