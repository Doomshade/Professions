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
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Command for "defaultconfig.yml" file generation (creates a copy of plugin's config.yml)
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("ALL")
public class GenerateDefaultConfigCommand extends AbstractCommand {

    public GenerateDefaultConfigCommand() {
        setCommand("generate-default-cfg");
        setDescription("Generates a 'defaultconfig.yml' file");
        addPermission(Permissions.ADMIN);
        setRequiresPlayer(false);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        try {
            Professions.getInstance().saveResource("config.yml", "defaultconfig.yml", true);
        } catch (Exception e) {
            sender.sendMessage("Error creating default config file");
            ProfessionLogger.logError(e);
            return;
        }
        sender.sendMessage("Successfully created default config file");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "generate-default-cfg";
    }
}
