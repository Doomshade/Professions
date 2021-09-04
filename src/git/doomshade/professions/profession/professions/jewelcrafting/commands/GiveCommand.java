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

package git.doomshade.professions.profession.professions.jewelcrafting.commands;

import git.doomshade.professions.api.item.object.Element;
import git.doomshade.professions.commands.AbstractCommand;
import git.doomshade.professions.profession.professions.jewelcrafting.Gem;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("ALL")
public class GiveCommand extends AbstractCommand {

    public GiveCommand() {


        setCommand("give");
        setDescription("Gives a player a gem");
        setArg(true, "gem id");
        setArg(false, "player");
        setRequiresPlayer(false);
        addPermission(Permissions.HELPER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        final String id = args[1];
        final Gem gem = Element.getElement(Gem.class, id);
        if (gem == null) {
            sender.sendMessage("Gem with " + id + " id does not exist");
            return;
        }

        Player to;
        if (args.length >= 3) {
            to = Bukkit.getPlayer(args[2]);
        } else if (sender instanceof Player) {
            to = (Player) sender;
        } else {
            sender.sendMessage("You must specify a player!");
            return;
        }

        Objects.requireNonNull(to).getInventory().addItem(gem.getGem());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return args.length == 2 ? new ArrayList<>(Element.getElements(Gem.class).keySet()) : null;
    }

    @Override
    public String getID() {
        return "give";
    }
}
