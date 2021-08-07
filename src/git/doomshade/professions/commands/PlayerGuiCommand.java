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
import git.doomshade.professions.gui.player.PlayerProfessionsGUI;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Opens profession GUI with help of GUIApi framework
 *
 * @author Doomshade
 * @version 1.0
 * @see git.doomshade.guiapi.GUI
 * @see git.doomshade.guiapi.GUIApi
 */
@SuppressWarnings("ALL")
public class PlayerGuiCommand extends AbstractCommand {

    public PlayerGuiCommand() {
        setCommand("gui");
        setDescription("Opens the professions GUI");
        setRequiresPlayer(true);
        addPermission(Permissions.DEFAULT_COMMAND_USAGE);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
//		GUI.getGui((Player) sender, MainGui.class).openGui();
        Professions.getGUIManager().openGui(PlayerProfessionsGUI.class, (Player) sender);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "playergui";
    }

}
