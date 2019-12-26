package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class LogFilterCommand extends AbstractCommand {

    public LogFilterCommand() {
        setArg(true, Collections.singletonList("regex"));
        setArg(false, Collections.singletonList("log file name"));
        setCommand("log");
        setDescription("Creates a log file in log directory with the searched term");
        setRequiresOp(true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        File logFile;

        if (args.length >= 3) {
            logFile = new File(args[2]);
        } else {
            logFile = Professions.getInstance().getLogFile();
        }

        final Pattern pattern = Pattern.compile(args[1]);

        try (Scanner sc = new Scanner(logFile)) {
            File customLog = new File(Professions.getInstance().getLogsFolder(), args[1].concat("-log-".concat(System.currentTimeMillis() + ".txt")));
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
