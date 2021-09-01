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
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.List;

/**
 * Saves current player data from memory to file
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("ALL")
public class SaveCommand extends AbstractCommand {

    public SaveCommand() {
        setCommand("save");
        setDescription("Saves player data");
        setRequiresPlayer(false);

        addPermission(Permissions.HELPER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        try {
            sender.sendMessage("Saving files...");
            IOManager.saveFiles();
            sender.sendMessage(ChatColor.GREEN + "Files saved successfully");
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Error! Check console for error stack trace.");
            ProfessionLogger.logError(e);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "save";
    }

}
