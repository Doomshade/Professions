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

import git.doomshade.professions.user.User;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

/**
 * Allows a player to bypass level requirement restrictions
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("ALL")
public class BypassCommand extends AbstractCommand {

    public BypassCommand() {
        setArg(false, "player", "suppress exp event (true/false)");
        setArg(true, "true/false");
        setCommand("bypass");
        setDescription("Allows user to bypass level restrictions");
        setRequiresPlayer(false);
        addPermission(Permissions.ADMIN);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayer(args[2]);
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            return;
        }
        User user = User.getUser(target);
        boolean bypass = Boolean.parseBoolean(args[1]);
        user.setBypass(bypass);
        String message = "Hraci " + Objects.requireNonNull(target).getDisplayName() + " nastaven bypass na " + bypass;
        if (args.length >= 4) {
            boolean suppress = Boolean.parseBoolean(args[3]);
            user.setSuppressExpEvent(suppress);
            message += " a suppress event na " + suppress;
        }
        sender.sendMessage(message);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "bypass";
    }

}
