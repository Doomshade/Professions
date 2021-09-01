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

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Reloads the plugin
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("ALL")
public class ReloadCommand extends AbstractCommand {

    private static boolean clear_cache = true;

    public static boolean isClearCache() {
        return clear_cache;
    }

    public ReloadCommand() {
        setCommand("reload");
        setDescription("Reloads plugin");
        setArg(false, "clear cache (true/false)");
        setRequiresPlayer(false);

        // TODO take into consideration
        addPermission(Permissions.HELPER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        clear_cache = false;
        Professions plugin = Professions.getInstance();
        if (args.length > 1) {
            try {
                clear_cache = Boolean.parseBoolean(args[1]);
            } catch (Exception e) {
                sender.sendMessage(ChatColor.BLUE + "Invalid argument. Valid args: (true/false)");
            }
        }
        if (plugin.reload()) {
            sender.sendMessage(ChatColor.GREEN + "Plugin reloaded.");
        } else {
            sender.sendMessage(ChatColor.RED + "Plugin reloaded with errors. Check console for further information.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "reload";
    }

}
