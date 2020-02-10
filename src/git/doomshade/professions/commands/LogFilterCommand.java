package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LogFilterCommand extends AbstractCommand {

    public LogFilterCommand() {
        setArg(false, Collections.singletonList("log file name"));
        setArg(true, Collections.singletonList("\"regex\""));
        setCommand("log");
        setDescription("Creates a log file in log directory with the searched term");
        addPermission(Permissions.ADMIN);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        File logFile;
        final int[] i = {1};


        String thePattern = Arrays.asList(args).subList(i[0], args.length).stream().map(new Function<String, String>() {
            boolean found = false;

            @Override
            public String apply(String s) {
                if (found) {
                    return "";
                }
                i[0]++;
                if (s.endsWith("\"") || s.endsWith("'")) {
                    found = true;
                }
                return s;
            }
        }).collect(Collectors.joining(" ")).trim().replaceAll("\"", "");

        final Pattern pattern = Pattern.compile(thePattern);
        final Professions instance = Professions.getInstance();
        if (args.length >= 3) {
            String logFileName = String.join(" ", Arrays.asList(args).subList(i[0], args.length));
            logFile = new File(instance.getLogsFolder(), logFileName);
            if (!logFile.exists()) {
                logFile = new File(instance.getFilteredLogsFolder(), logFileName);
            }
        } else {
            logFile = instance.getLogFile();
            i[0]++;
        }
        if (!logFile.exists()) {
            sender.sendMessage(logFile.getName() + " file does not exist!");
            return true;
        }
        sender.sendMessage("Iterating through " + logFile.getName() + " with pattern: " + pattern.pattern());

        try (Scanner sc = new Scanner(logFile)) {
            final File filteredLogsFolder = instance.getFilteredLogsFolder();
            File customLog = new File(filteredLogsFolder, pattern.pattern().concat("-log-".concat(System.currentTimeMillis() + ".txt")));
            if (!customLog.exists()) {
                customLog.createNewFile();
            }
            try (PrintWriter os = new PrintWriter(customLog)) {
                while (sc.hasNextLine()) {
                    String s = sc.nextLine();
                    if (pattern.matcher(s).find())
                        os.println(s);
                }
            }
            sender.sendMessage("Successfully created " + customLog.getName() + " in folder " + filteredLogsFolder.getName());
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            sender.sendMessage("Soubor neexistuje.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "log";
    }
}
