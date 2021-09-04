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
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Adds an "extra" (a string, like a flag) for requirements purposes such as letting the player craft some item only
 * under a circumstance (the "extra")
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("ALL")
public class AddExtraCommand extends AbstractCommand {

    public AddExtraCommand() {
        setDescription("Adds an \"extra\" to a profession for requirement purposes");
        setArg(true, "user", "profession", "extra");
        setCommand("extra");
        setRequiresPlayer(false);
        addPermission(Permissions.HELPER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Optional<Profession> opt = Professions.getProfMan().getProfessionById(args[2]);
        if (!opt.isPresent()) {
            return;
        }

        Profession prof = opt.get();

        User user = User.getUser(Bukkit.getPlayer(args[1]));
        HashSet<String> extras = new HashSet<>(Arrays.asList(args).subList(3, args.length));

        UserProfessionData upd = user.getProfessionData(prof);
        String extra = extras.toString().replaceAll("\\[", "").replaceAll("]", "").replaceAll("[,]", "");
        upd.addExtra(extra);
        try {
            user.save();
        } catch (IOException e) {
            ProfessionLogger.logError(e);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "addextra";
    }

}
