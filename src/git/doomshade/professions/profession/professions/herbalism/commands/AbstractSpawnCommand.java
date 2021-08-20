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
import git.doomshade.professions.commands.AbstractCommand;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.professions.herbalism.Herb;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("ALL")
abstract class AbstractSpawnCommand extends AbstractCommand {
    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Herb herb = Herb.getElement(Herb.class, args[1]);
        if (herb == null) {
            sender.sendMessage("Herb with ID " + args[1] + " does not exist.");
            return;
        }
        Object serialId;
        try {
            serialId = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            if (args[2].equalsIgnoreCase("all")) {
                serialId = "";
            } else {
                sender.sendMessage("Invalid number format.");
                return;
            }
        }

        // try to get the spawn point with serial number
        ISpawnPoint sp = null;
        try {
            if (serialId instanceof Integer) {
                sp = herb.getSpawnPoint((int) serialId);
            }
        } catch (IndexOutOfBoundsException e) {
            sender.sendMessage("Spawn point with ID " + serialId + " does not exist.");
            return;
        }

        // the spawn point with that serial number for given spawnable element does not exist
        if (sp == null) {
            for (ISpawnPoint spp : herb.getSpawnPoints()) {
                Location sppLoc = spp.getLocation();
                String locName = String.format("%s: %d,%d,%d", Objects.requireNonNull(sppLoc.getWorld()).getName(),
                        sppLoc.getBlockX(), sppLoc.getBlockY(), sppLoc.getBlockZ());
                try {
                    consumer().accept(spp);
                    sender.sendMessage("Successfully scheduled spawn of herb at " + locName + ".");
                } catch (Exception e) {
                    sender.sendMessage(
                            "Could not schedule spawn of herb at " + locName + ". Check console for error stacktrace.");
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
            consumer().accept(sp);
            sender.sendMessage("Successfully scheduled spawn of herb at " + locName + ".");
        } catch (Exception e) {
            sender.sendMessage(
                    "Could not schedule spawn of herb at " + locName + ". Check console for error stacktrace.");
            ProfessionLogger.logError(e);
        }

    }

    protected abstract Consumer<ISpawnPoint> consumer();

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
                    break;
                }
                for (int i = 0; i < herb.getSpawnPoints().size(); i++) {
                    String id = String.valueOf(i);
                    if (args[2].startsWith(id)) {
                        list.add(id);
                    } else if (args[2].isEmpty()) {
                        list.add(id);
                    }
                }
                break;
            case 4:
                final List<String> booleans = Arrays.asList("true", "false");
                if (args[3].isEmpty()) {
                    list.addAll(booleans);
                } else {
                    list = booleans.stream().filter(x -> x.startsWith(args[3])).collect(Collectors.toList());
                }
                break;
        }
        return list.isEmpty() ? null : list;
    }
}
