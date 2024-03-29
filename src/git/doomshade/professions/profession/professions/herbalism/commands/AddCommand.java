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

import git.doomshade.professions.api.Range;
import git.doomshade.professions.commands.AbstractCommand;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.professions.herbalism.Herb;
import git.doomshade.professions.api.spawn.ext.SpawnPoint;
import git.doomshade.professions.utils.Permissions;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("ALL")
public class AddCommand extends AbstractCommand {
    public AddCommand() {
        setCommand("add");
        setDescription("Marks the block you are looking at as a herb");
        setRequiresPlayer(true);
        setArg(true, "herb", "respawn time (e.g. 4 or 5-8)");
        addPermission(Permissions.BUILDER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Herb herb = Herb.getElement(Herb.class, args[1]);

        if (herb == null) {
            player.sendMessage("Invalid herb id");
            return;
        }
        Location lookingAt = Utils.getLookingAt(player).getLocation();
        if (lookingAt.getBlock().getBlockData().getMaterial().isAir()) {
            player.sendMessage("You must be looking at some block");
            return;
        }

        Range respawnTime = null;
        try {
            respawnTime = Range.fromString(args[2]).orElseThrow(() -> new IllegalArgumentException(
                    String.format("Could not get " +
                            "range from '%s'", args[2])));
        } catch (Exception e) {
            ProfessionLogger.logError(e);
        }
        if (respawnTime == null) {
            player.sendMessage("Invalid respawn time");
            return;
        }

        // TODO marker set id
        final SpawnPoint sp = new SpawnPoint(lookingAt, respawnTime, herb, "herbs");
        try {
            sp.spawn();
        } catch (SpawnException e) {
            ProfessionLogger.logError(e);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length) {
            case 2:
                list.addAll(Herb.getElements(Herb.class)
                        .values()
                        .stream()
                        .filter(x -> x.getId().startsWith(args[1]))
                        .map(Herb::getId)
                        .collect(Collectors.toList()));
                break;
            case 3:
                Herb herb = Herb.getElement(Herb.class, args[1].trim());
                if (herb == null) {
                    sender.sendMessage(args[1] + " is an invalid herb id.");
                }
                break;
        }
        return list.isEmpty() ? null : list;
    }

    @Override
    public String getID() {
        return "add";
    }
}
