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

import git.doomshade.professions.trait.TrainerTrait;
import git.doomshade.professions.utils.Permissions;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command for editing the trainer. Basically opens a trainer chooser GUI.
 *
 * @author Doomshade
 * @version 1.0
 */
@SuppressWarnings("ALL")
public class EditTraitCommand extends AbstractCommand {

    public EditTraitCommand() {
        setCommand("edit-trait");
        setDescription("Edits the trait of the selected NPC");
        setRequiresPlayer(true);
        addPermission(Permissions.BUILDER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        final NPC selected = CitizensAPI.getDefaultNPCSelector().getSelected(sender);
        if (selected == null) {
            sender.sendMessage("You must have an NPC selected to edit the trainer");
            return;
        }

        if (!selected.hasTrait(TrainerTrait.class)) {
            sender.sendMessage(
                    selected.getName() + ChatColor.RESET + " does not have the trainer trait (professiontrainer)!");
            return;
        }

        final TrainerTrait trait = selected.getTrait(TrainerTrait.class);
        trait.openTrainerChooserGUI((Player) sender);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "traitedit";
    }
}
