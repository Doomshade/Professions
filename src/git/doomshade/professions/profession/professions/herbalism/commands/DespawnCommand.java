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

package git.doomshade.professions.profession.professions.herbalism.commands;

import git.doomshade.professions.api.spawn.ISpawnPoint;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.professions.herbalism.Herb;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings("ALL")
public class DespawnCommand extends AbstractSpawnCommand {

    DespawnCommand() {
        setArg(true, "herb", "all / spawnpoint id");
        setArg(false, "disable further spawn");
        setCommand("despawn");
        setDescription("Despawns a herb");
        setRequiresPlayer(false);
        addPermission(Permissions.HELPER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        boolean disableSpawn = false;

        // TODO redo this
        if (args.length >= 4) {
            try {
                disableSpawn = Boolean.parseBoolean(args[3]);
            } catch (Exception e) {
                sender.sendMessage("Invalid boolean type provided, using false as default.");
            }
        }

        Herb herb = Herb.get(Herb.class, args[1]);
        if (herb == null) {
            sender.sendMessage("Herb with ID " + args[1] + " does not exist.");
            return;
        }
        Object spId;
        try {
            spId = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            if (!args[2].equalsIgnoreCase("all")) {
                sender.sendMessage("Invalid number format.");
                return;
            } else {
                spId = "";
            }
        }

        ISpawnPoint sp = null;
        try {
            if (spId instanceof Integer) {
                sp = herb.getSpawnPoint((Integer) spId);
            }
        } catch (IndexOutOfBoundsException e) {
            sender.sendMessage("Spawn point with ID " + spId + " does not exist.");
            return;
        }


        if (sp == null) {
            for (ISpawnPoint spp : herb.getSpawnPoints()) {
                Location hloLoc = spp.getLocation();
                String locName = String.format("%s: %d,%d,%d", Objects.requireNonNull(hloLoc.getWorld()).getName(),
                        hloLoc.getBlockX(), hloLoc.getBlockY(), hloLoc.getBlockZ());
                try {
                    spp.despawn();
                    sender.sendMessage("Successfully despawned herb at " + locName + ".");
                    if (!disableSpawn) {
                        spp.scheduleSpawn();
                    }
                } catch (Exception e) {
                    sender.sendMessage(
                            "Could not despawn herb at " + locName + ". Check console for error stacktrace.");
                    ProfessionLogger.logError(e);
                }
            }
            return;
        }

        Location loc = sp.getLocation();
        String locName =
                String.format("%s: %d,%d,%d", Objects.requireNonNull(loc.getWorld()).getName(), loc.getBlockX(),
                        loc.getBlockY(), loc.getBlockZ());
        try {
            sp.despawn();
            if (!disableSpawn) {
                sp.scheduleSpawn();
            }
            sender.sendMessage("Successfully despawned herb at " + locName + ".");
        } catch (Exception e) {
            sender.sendMessage("Could not despawn herb at " + locName + ". Check console for error stacktrace.");
            ProfessionLogger.logError(e);
        }

    }

    @Override
    protected Consumer<ISpawnPoint> consumer() {
        return null;
    }

    @Override
    public String getID() {
        return "despawn";
    }
}
