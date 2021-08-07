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

import git.doomshade.professions.profession.professions.mining.Ore;
import git.doomshade.professions.api.spawn.ext.Spawnable;
import git.doomshade.professions.utils.Permissions;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("ALL")
public class RemoveCommand extends AbstractEditCommand {

    public RemoveCommand() {
        setCommand("remove");
        setDescription("Removes an ore you are currently looking at from spawn points or optionally via args");
        setRequiresPlayer(true);
        setArg(false, "ore id", "spawnpoint id");
        addPermission(Permissions.BUILDER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player hrac = (Player) sender;

        if (args.length == 1) {
            Location loc = Utils.getLookingAt(hrac).getLocation();
            Ore ore;
            try {
                ore = Utils.findInIterable(Spawnable.getElements(Ore.class).values(), x -> x.isSpawnPoint(loc));
            } catch (Utils.SearchNotFoundException e) {
                hrac.sendMessage("Block you are looking at is no ore");
                return;
            }
            ore.removeSpawnPoint(loc);
        } else {
            if (args.length < 3) {
                hrac.sendMessage("You must enter both ore and spawn point id!");
                return;
            }

            Ore ore = Ore.get(Ore.class, args[1]);

            if (ore == null) {
                hrac.sendMessage("Invalid ore id");
                return;
            }

            int serialNumber;

            try {
                serialNumber = Integer.parseInt(args[2]);

                if (!ore.isSpawnPoint(serialNumber)) {
                    hrac.sendMessage(String.format("The serial number %d of ore %s does not exist!", serialNumber,
                            ore.getName()));
                    return;
                }
            } catch (NumberFormatException e) {
                hrac.sendMessage("Invalid serial number (number required)");
                return;
            }

            ore.removeSpawnPoint(serialNumber);
        }
        sender.sendMessage("Successfully removed spawn point");

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "remove";
    }
}
