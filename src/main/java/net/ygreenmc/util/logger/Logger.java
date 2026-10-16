package net.ygreenmc.util.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Logger implements ILogger {

    private static volatile LogLevel threshold = LogLevel.INFO;
    private static PrintWriter fileWriter;

    protected static final Object LOCK = new Object();

    private static final Map<String, Logger> CACHE = new ConcurrentHashMap<>();
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("'['yyyy-MM-dd HH:mm:ss.SSS']'");

    protected final String name;

    protected Logger(String name) {
        this.name = name;
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static Logger getLogger(String name) {
        return CACHE.computeIfAbsent(name, Logger::new);
    }

    public static void setLevel(LogLevel level) {
        if (level == null) {
            throw new IllegalArgumentException("level cannot be null");
        }
        threshold = level;
    }

    public static LogLevel getLevel() {
        return threshold;
    }

    public static void setFile(String path) {
        synchronized (LOCK) {
            try {
                File file = new File(path);
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }

                PrintWriter newWriter =
                        new PrintWriter(new FileWriter(file, true), true);

                if (fileWriter != null) {
                    fileWriter.flush();
                    fileWriter.close();
                }
                fileWriter = newWriter;
            } catch (IOException e) {
                System.err.println("Unable to open the log file: " + path);
                e.printStackTrace();
            }
        }
    }

    public static void shutdown() {
        synchronized (LOCK) {
            if (fileWriter != null) {
                fileWriter.flush();
                fileWriter.close();
                fileWriter = null;
            }
        }
    }

    @Override
    public void debug(String msg, Object... args) {
        log(LogLevel.DEBUG, msg, args, null);
    }

    @Override
    public void info(String msg, Object... args) {
        log(LogLevel.INFO, msg, args, null);
    }

    @Override
    public void success(String msg, Object... args) {
        log(LogLevel.SUCCESS, msg, args, null);
    }

    @Override
    public void warning(String msg, Object... args) {
        log(LogLevel.WARNING, msg, args, null);
    }

    @Override
    public void error(String msg, Object... args) {
        log(LogLevel.ERROR, msg, args, null);
    }

    @Override
    public void error(String msg, Throwable t, Object... args) {
        log(LogLevel.EXCEPTION, msg, args, t);
    }

    @Override
    public void exception(String msg, Throwable t, Object... args) {
        log(LogLevel.EXCEPTION, msg, args, t);
    }

    @Override
    public void critical(String msg, Object... args) {
        log(LogLevel.CRITICAL, msg, args, null);
    }

    private void log(LogLevel level, String msg, Object[] args, Throwable t) {
        if (!level.isEnabled(threshold)) {
            return;
        }

        String message = format(msg, args);
        String line = formatLine(level, message, t);

        synchronized (LOCK) {
            writeToConsole(level, line);

            if (fileWriter != null) {
                fileWriter.println(line);
            }
        }
    }

    protected void writeToConsole(LogLevel level, String line) {
        if (level.getSeverity() >= LogLevel.ERROR.getSeverity()) {
            System.err.println(line);
        } else {
            System.out.println(line);
        }
    }

    private static String format(String fmt, Object[] args) {
        if (fmt == null) {
            return "null";
        }

        if (args == null || args.length == 0) {
            return fmt;
        }

        StringBuilder sb = new StringBuilder();
        int argIndex = 0;
        int start = 0;
        int idx;

        while (argIndex < args.length && (idx = fmt.indexOf("{}", start)) != -1) {
            sb.append(fmt, start, idx);
            sb.append(args[argIndex++]);
            start = idx + 2;
        }

        sb.append(fmt.substring(start));
        return sb.toString();
    }

    private String formatLine(LogLevel level, String message, Throwable t) {
        StringBuilder sb = new StringBuilder();

        sb.append(LocalDateTime.now().format(TIME_FMT))
                .append(" [").append(Thread.currentThread().getName()).append(" / ")
                .append(level.name()).append(" at ")
                .append(name).append("] ")
                .append(message);

        if (t != null) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            sb.append(System.lineSeparator()).append(sw);
        }

        return sb.toString();
    }
}
