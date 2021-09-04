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
import git.doomshade.professions.api.profession.Profession;
import git.doomshade.professions.api.profession.ProfessionType;
import git.doomshade.professions.profession.ProfessionManager;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Prints the list of all professions
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("ALL")
public class ProfessionListCommand extends AbstractCommand {

    public ProfessionListCommand() {
        setCommand("list");
        setDescription("Shows a list of professions");
        setRequiresPlayer(false);
        addPermission(Permissions.HELPER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        ProfessionManager profMan = Professions.getProfMan();
        Map<ProfessionType, Integer> profTypes;
        List<Profession> profs = new ArrayList<>(profMan.getProfessionsById().values());
        profs.sort(Comparator.naturalOrder());
        List<ProfessionType> pt = Arrays.asList(ProfessionType.values());
        pt.sort(Comparator.naturalOrder());
        profTypes = pt.stream().collect(Collectors.toMap(x -> x, x -> 0, (a, b) -> b, TreeMap::new));
        profMan.getProfessionsById().forEach((y, x) -> {
            if (x.getProfessionType() != null) {
                profTypes.put(x.getProfessionType(), profTypes.get(x.getProfessionType()) + 1);
            }
        });
        profs.forEach(x -> sender.sendMessage(x.getColoredName() + ChatColor.RESET + ", " + x.getProfessionType()));
        profTypes.forEach((x, y) -> sender.sendMessage(x.toString() + ": " + y));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "professionslist";
    }

}
