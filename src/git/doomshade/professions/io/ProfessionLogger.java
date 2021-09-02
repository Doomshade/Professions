/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jakub Šmrha
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package git.doomshade.professions.io;

import git.doomshade.professions.Professions;
import org.bukkit.ChatColor;
import org.fusesource.jansi.Ansi;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public final class ProfessionLogger {
    private static final int GREEN = 500;
    private static final int RED = 900;
    private static LogLevel logLevel = LogLevel.ALL;

    private ProfessionLogger() {
    }

    public static void setLogLevel(int level) {
        logLevel = LogLevel.fromLevel(level);
    }

    /**
     * Logs an object using {@link Object#toString()} method. Use {@link Level#CONFIG} to log into log file.
     *
     * @param object the object to log
     * @param level  the level
     *
     * @see ProfessionLogger#log(String, Level)
     */
    public static void log(Object object, Level level) {
        log(object == null ? "null" : object.toString(), level);
    }

    /**
     * Logs a message. Levels {@literal >}= {@link Level#CONFIG} (excluding {@link Level#INFO}) will be logged to file.
     * Levels {@literal >}=900 will be displayed in red. Levels {@literal <}=500 will be displayed in green. Calls
     * {@link ProfessionLogger#log(String, Level, boolean)} with {@code false} as the third argument
     *
     * @param message the message to display
     * @param level   the log level
     *
     * @see ProfessionLogger#log(String, Level, boolean)
     */
    public static void log(String message, Level level) {
        log(message, level, false);
    }

    /**
     * Logs a message. Levels {@literal >}= {@link Level#CONFIG} (excluding {@link Level#INFO}) will be logged to file.
     * Levels {@literal >}=900 will be displayed in red. Levels {@literal <}=500 will be displayed in green.
     *
     * @param message       the message to display
     * @param level         the log level
     * @param logToFileOnly whether to log to file only
     */
    public static void log(String message, Level level, boolean logToFileOnly) {
        if (message == null || level == null) {
            return;
        }

        // don't log empty messages
        if (message.isEmpty()) {
            return;
        }

        // log only with proper level
        final int leveli = level.intValue();
        if (logLevel.level.intValue() >= leveli) {
            return;
        }


        final String infoWithPadding = String.format("[%-7s] ", level.getName());
        try {
            IOManager.logToFile(infoWithPadding.concat(ChatColor.stripColor(message)));
        } catch (IOException e) {
            Professions.getInstance()
                    .getLogger()
                    .log(Level.WARNING, "Could not create the log file!", e);
        }
        Ansi.Color color = Ansi.Color.WHITE;

        if (leveli >= RED) {
            color = Ansi.Color.RED;
        } else if (leveli <= GREEN) {
            color = Ansi.Color.GREEN;
        }

        Ansi ansi = Ansi.ansi().boldOff();

        // don't log to console if the logToFileOnly is true
        // log only info, warnings and severe messages
        if (!logToFileOnly && leveli >= Level.INFO.intValue()) {
            final String info = String.format("[%s] ", level.getName());
            Professions.getInstance()
                    .getLogger()
                    .log(level, ansi.fg(color).toString() + info.concat(message) + ansi.fg(Ansi.Color.WHITE));

        }
    }

    /**
     * Logs a message to console with {@link Level#INFO} level.
     *
     * @param message the message to display
     */
    public static void log(String message) {
        log(message, Level.INFO);
    }

    /**
     * Calls {@link ProfessionLogger#logError(Throwable, boolean)} with {@code true} argument
     *
     * @param e the throwable
     */
    public static void logError(Throwable e) {
        logError(e, true);
    }

    public static void logError(Throwable e, boolean pluginError) {
        final Level level = pluginError ? Level.SEVERE : Level.WARNING;

        // log that an error has occurred
        log((pluginError ? "Internal" : "External") + " plugin error" + (!pluginError ?
                ", please check logs for further information." :
                ", please contact author with the stack trace from your log file."), level);

        // log the stacktrace to file only
        if (e != null && e.getStackTrace() != null) {
            final String ss = e + "\n" + Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining(",\n"));
            if (e.getMessage() != null) {
                log(e.getMessage().replaceAll("<br>", "\n") + "\n" + ss, level, true);
            } else {
                log(ss, level, true);
            }
        }
    }

    private enum LogLevel {
        ALL(Level.ALL),
        FINE(Level.FINE),
        CONFIG(Level.CONFIG),
        INFO(Level.INFO),
        WARNING(Level.WARNING),
        SEVERE(Level.SEVERE);

        private final int levelId;
        private final Level level;

        LogLevel(Level lvl) {
            this.level = lvl;
            this.levelId = ordinal();
        }

        static LogLevel fromLevel(int level) {
            return Arrays.stream(values()).filter(ll -> ll.levelId == level).findFirst().orElse(LogLevel.ALL);
        }
    }
}
