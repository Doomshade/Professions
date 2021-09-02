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

import git.doomshade.professions.api.spawn.ISpawnPoint;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.professions.mining.Ore;
import git.doomshade.professions.api.spawn.ext.Spawnable;
import git.doomshade.professions.utils.Permissions;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Command for editing mining areas
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("ALL")
public class EditCommand extends AbstractEditCommand {

    private final Set<Location> edited = new HashSet<>();

    /**
     * Setup defaults for command
     */
    public EditCommand() {
        setCommand("edit");
        setRequiresPlayer(true);
        setArg(false, "allwool/allore");
        addPermission(Permissions.BUILDER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        final Map<String, Ore> ores = Spawnable.getElements(Ore.class);
        if (args.length >= 2) {

            switch (args[1].toLowerCase()) {
                case "allwool":
                    for (Ore ore : ores.values()) {
                        ore.getSpawnPoints().forEach(x -> {
                            x.despawn();
                            Location loc = x.getLocation();
                            loc.getBlock().setType(Material.WHITE_WOOL);
                            edited.add(loc);
                        });
                    }
                    break;
                case "allore":
                    for (Ore ore : ores.values()) {
                        ore.getSpawnPoints().forEach(x -> {
                            x.despawn();
                            try {
                                x.scheduleSpawn();
                            } catch (SpawnException e) {
                                ProfessionLogger.logError(e);
                            }
                        });
                    }
                    edited.clear();
                    break;
                default:
                    return;
            }
        } else {
            Player player = (Player) sender;
            Location loc = Utils.getLookingAt(player).getLocation();
            try {
                Ore ore = Utils.findInIterable(ores.values(), x -> x.isSpawnPoint(loc));
                final ISpawnPoint locationOptions = ore.getSpawnPoint(loc);
                locationOptions.despawn();

                if (edited.remove(loc)) {
                    locationOptions.scheduleSpawn();
                } else {
                    edited.add(loc);
                    loc.getBlock().setType(Material.WHITE_WOOL);
                }
            } catch (SpawnException e) {
                ProfessionLogger.logError(e);
            } catch (Utils.SearchNotFoundException e) {
                return;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "edit";
    }
}
