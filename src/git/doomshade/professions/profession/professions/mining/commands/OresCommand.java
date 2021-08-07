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

package git.doomshade.professions.profession.professions.mining.commands;

import git.doomshade.guiapi.GUI;
import git.doomshade.professions.Professions;
import git.doomshade.professions.gui.mining.OreGUI;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("ALL")
public class OresCommand extends AbstractEditCommand {

    public OresCommand() {
        setCommand("ores");
        setDescription("Opens a GUI with available ores. Place the ore block to register the position of an ore.");
        setRequiresPlayer(true);
        addPermission(Permissions.BUILDER);
        setArg(false, "ignore range (true/false, default=false)");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        final Player player = (Player) sender;
        final Optional<? extends GUI> opt = Professions.getGUIManager().getGui(OreGUI.class, player);
        if (opt.isEmpty()) {
            return;
        }

        GUI gui = opt.get();

        boolean ignore = false;

        if (args.length >= 2) {

            try {
                ignore = Boolean.parseBoolean(args[1]);
            } catch (Exception ignored) {
            }
        }
        gui.getContext().addContext("ignore", ignore);
        Professions.getGUIManager().openGui(gui);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "place";
    }
}
