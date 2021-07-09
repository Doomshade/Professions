package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.Profession;
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
    public void onCommand(CommandSender sender, String[] args) {
        Optional<Profession> opt = Professions.getProfessionById(args[2]);
        if (!opt.isPresent()) {
            return;
        }

        Profession prof = opt.get();

        User user = User.getUser(Bukkit.getPlayer(args[1]));
        HashSet<String> extras = new HashSet<>(Arrays.asList(args).subList(3, args.length));

        UserProfessionData upd = user.getProfessionData(prof);
        String extra = extras.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("[,]", "");
        upd.addExtra(extra);
        try {
            user.save();
        } catch (IOException e) {
            Professions.logError(e);
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
