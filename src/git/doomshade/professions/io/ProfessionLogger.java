package git.doomshade.professions.io;

import git.doomshade.professions.Professions;
import org.bukkit.ChatColor;
import org.fusesource.jansi.Ansi;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ProfessionLogger {
    private static final DateTimeFormatter DF = DateTimeFormatter
            .ofLocalizedTime(FormatStyle.MEDIUM)
            .withLocale(Locale.GERMAN);
    private static final int GREEN = 500;
    private static final int RED = 900;

    private ProfessionLogger() {
    }

    /**
     * Logs an object using {@link Object#toString()} method. Use {@link Level#CONFIG} to log into log file.
     *
     * @param object the object to log
     * @param level  the level
     */
    public static void log(Object object, Level level) {
        log(object == null ? "null" : object.toString(), level);
    }

    /**
     * Logs an object using {@link Object#toString()} method to console with {@link Level#INFO} level.
     *
     * @param object the object to log
     */
    public static void log(Object object) {
        log(object, Level.INFO);
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
        log((pluginError ? "Internal" : "External") + " plugin error" + (!pluginError ? ", please check logs for further information." : ", please contact author with the stack trace from your log file."), Level.WARNING);
        if (e != null && e.getStackTrace() != null) {
            final String ss = e.toString() + "\n" + Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining(",\n"));
            if (e.getMessage() != null) {
                log(e.getMessage().replaceAll("<br>", "\n") + "\n" + ss, Level.CONFIG);
            } else {
                log(ss, Level.CONFIG);
            }
        }
    }

    /**
     * Logs a message. Levels >= {@link Level#CONFIG} (excluding {@link Level#INFO}) will be logged to file.
     * Levels >=900 will be displayed in red. Levels <=500 will be displayed in green.
     *
     * @param message the message to display
     * @param level   the log level
     */
    public static void log(String message, Level level) {
        if (message.isEmpty()) {
            return;
        }

        final int leveli = level.intValue();
        if (leveli >= Level.CONFIG.intValue() && level != Level.INFO) {
            String time = String.format("[%s] ", LocalTime.now().format(DF));

            if (IOManager.fos == null) {
                try {
                    IOManager.fos = new PrintStream(IOManager.getLogFile());
                } catch (FileNotFoundException e) {
                    // DONT CALL #logError HERE!
                    e.printStackTrace();
                    return;
                }
            }

            IOManager.fos.println(time.concat(ChatColor.stripColor(message)));

            if (level == Level.CONFIG)
                return;
        }
        Ansi.Color color = Ansi.Color.WHITE;

        if (leveli >= RED) {
            color = Ansi.Color.RED;
        } else if (leveli <= GREEN) {
            color = Ansi.Color.GREEN;
        }

        Ansi ansi = Ansi.ansi().boldOff();

        Professions.getInstance().getLogger().log(level, ansi.fg(color).toString() + message + ansi.fg(Ansi.Color.WHITE));

    }
}
