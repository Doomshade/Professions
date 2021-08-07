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
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

/**
 * Adds levels or sets the level or player's profession
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("ALL")
public class LevelCommand extends AbstractCommand {

    public LevelCommand() {
        setArg(true, "profession", "add/set", "level");
        setArg(false, "player");
        setCommand("level");
        setRequiresPlayer(false);
        setDescription("Adds levels or sets the level of the player");
        addPermission(Permissions.HELPER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player target;
        if (!(sender instanceof Player)) {
            if (args.length < 5) {
                return;
            }
            target = Bukkit.getPlayer(args[4]);
        } else {
            if (args.length >= 5) {
                target = Bukkit.getPlayer(args[4]);
            } else {
                target = (Player) sender;
            }
        }
        Optional<Profession> opt = Professions.getProfMan().getProfessionById(args[1]);
        if (!opt.isPresent()) {
            return;
        }
        Profession prof = opt.get();
        User user = User.getUser(target);
        if (!user.hasProfession(prof)) {
            return;
        }
        UserProfessionData upd = user.getProfessionData(prof);

        int level = Integer.parseInt(args[3]);
        switch (args[2].toLowerCase()) {
            case "set":
                upd.setLevel(level);
                break;
            case "add":
                upd.addLevel(level);
                break;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "level";
    }
}
