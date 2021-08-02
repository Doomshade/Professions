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

package git.doomshade.professions.commands;

import git.doomshade.professions.io.IOManager;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A command to create log files
 *
 * @author Doomshade
 * @version 1.0
 */
@SuppressWarnings("ALL")
public class LogFilterCommand extends AbstractCommand {

    public LogFilterCommand() {
        setArg(false, "log file name");
        setArg(true, "\"regex\"");
        setCommand("log");
        setDescription("Creates a log file in log directory with the searched term");
        addPermission(Permissions.ADMIN);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
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
        if (args.length >= 3) {
            String logFileName = String.join(" ", Arrays.asList(args).subList(i[0], args.length));
            logFile = new File(IOManager.getLogsFolder(), logFileName);
            if (!logFile.exists()) {
                logFile = new File(IOManager.getFilteredLogsFolder(), logFileName);
            }
        } else {
            logFile = IOManager.getLogFile();
            i[0]++;
        }
        if (!logFile.exists()) {
            sender.sendMessage(logFile.getName() + " file does not exist!");
            return;
        }
        sender.sendMessage("Iterating through " + logFile.getName() + " with pattern: " + pattern.pattern());

        try (Scanner sc = new Scanner(logFile)) {
            final File filteredLogsFolder = IOManager.getFilteredLogsFolder();
            File customLog = new File(filteredLogsFolder,
                    pattern.pattern().concat("-log-".concat(System.currentTimeMillis() + ".txt")));
            if (!customLog.exists()) {
                customLog.createNewFile();
            }
            try (PrintWriter os = new PrintWriter(customLog)) {
                while (sc.hasNextLine()) {
                    String s = sc.nextLine();
                    if (pattern.matcher(s).find()) {
                        os.println(s);
                    }
                }
            }
            sender.sendMessage(
                    "Successfully created " + customLog.getName() + " in folder " + filteredLogsFolder.getName());
        } catch (FileNotFoundException ex) {
            sender.sendMessage("File does not exist.");
            ProfessionLogger.logError(ex);
        } catch (IOException e) {
            ProfessionLogger.logError(e);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "log";
    }
}
