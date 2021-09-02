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

import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Undoes latest edit to a file. Can be executed as long as there were edits
 *
 * @author Doomshade
 * @version 1.0
 * @see git.doomshade.professions.profession.professions.mining.commands.EditCommand
 * @since 1.0
 */
@SuppressWarnings("ALL")
public class UndoEditCommand extends AbstractCommand {

    public UndoEditCommand() {
        setArg(true, EditItemTypeCommand.ARG_FILE);
        setCommand("undo");
        setDescription("Undoes previous actions made to an item type file");
        setRequiresPlayer(false);

        addPermission(Permissions.ADMIN);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        File file = EditItemTypeCommand.getFile(args);
        if (!file.exists()) {
            sender.sendMessage(file.getName() + " does not exist!");
            return;
        }
        FileConfiguration loader = EditItemTypeCommand.getAndRemoveLastUndo(file);
        if (loader != null) {
            try {
                loader.save(file);
                sender.sendMessage("Undid previous action made to " + file.getName());
            } catch (IOException e) {
                sender.sendMessage("Could not undo previous actions! Check console for more output.");
                ProfessionLogger.logError(e);
            }
        } else {
            sender.sendMessage("Nothing to undo");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        EditItemTypeCommand.getFile(args);
        List<String> list = EditItemTypeCommand.FILES.stream()
                .filter(x -> x.startsWith(args[EditItemTypeCommand.I[0] - 1]))
                .distinct()
                .collect(Collectors.toList());
        return list.isEmpty() ? null : list;
    }

    @Override
    public String getID() {
        return "undoedit";
    }
}
