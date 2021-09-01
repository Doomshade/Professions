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

package git.doomshade.professions.trait;

import git.doomshade.professions.commands.CommandHandler;
import git.doomshade.professions.commands.EditTraitCommand;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.Utils;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCTraitCommandAttachEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.logging.Level;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class TrainerListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onRightClick(NPCRightClickEvent e) {
        final NPC npc = e.getNPC();
        if (!npc.hasTrait(TrainerTrait.class)) {
            return;
        }

        TrainerTrait trait = npc.getTrait(TrainerTrait.class);
        final Player player = e.getClicker();

        String trainerId = trait.getTrainerId();
        if (trainerId == null || trainerId.isEmpty()) {
            final String s = "Could not resolve trainer ID, please contact an admin.";
            player.sendMessage(s);
            throw new RuntimeException(s);
        }
        trait.openTrainerGUI(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTraitAdd(NPCTraitCommandAttachEvent e) throws Utils.SearchNotFoundException {
        if (!e.getTraitClass().equals(TrainerTrait.class)) {
            return;
        }
        final NPC npc = e.getNPC();
        final TrainerTrait trait = npc.getTrait(TrainerTrait.class);

        final CommandSender sender = e.getCommandSender();

        if (!(sender instanceof Player)) {
            final CommandHandler handler = CommandHandler.getInstance(CommandHandler.class);
            ProfessionLogger.log("Attached trainer trait to " + npc.getName() + ". Please use " +
                    handler.infoMessage(handler.getCommand(EditTraitCommand.class)) + ChatColor.RESET +
                    " ingame with the NPC selected.", Level.WARNING);
        } else {
            trait.openTrainerChooserGUI((Player) sender);
        }
    }


}
