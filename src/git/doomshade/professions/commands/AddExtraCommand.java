package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.user.User;
import git.doomshade.professions.api.user.UserProfessionData;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Adds an "extra" (a string, like a flag) for requirements purposes such as letting the player craft some item only under a circumstance (the "extra")
 *
 * @author Doomshade
 * @version 1.0
 */
public class AddExtraCommand extends AbstractCommand {

    public AddExtraCommand() {
        setDescription("Adds an \"extra\" to a profession for requirement purposes");
        setArg(true, "user", "profession", "extra");
        setCommand("extra");
        setRequiresPlayer(false);
        addPermission(Permissions.HELPER);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        User user = User.getUser(Bukkit.getPlayer(args[1]));
        Profession prof = Professions.getProfession(args[2]);
        HashSet<String> extras = new HashSet<>(Arrays.asList(args).subList(3, args.length));

        UserProfessionData upd = user.getProfessionData(prof);
        String extra = extras.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("[,]", "");
        if (upd != null) {
            upd.addExtra(extra);
        }
        try {
            user.save();
        } catch (IOException e) {
            Professions.logError(e);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "addextra";
    }

}
