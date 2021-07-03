package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.user.User;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Adds profession exp to a player
 *
 * @author Doomshade
 * @version 1.0
 */
public class AddExpCommand extends AbstractCommand {

    public AddExpCommand() {
        setArg(true, "profession", "add/set", "exp");
        setArg(false, "player");
        setCommand("exp");
        setDescription("Gives exp to the player or sets it");
        addPermission(Permissions.HELPER);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Profession prof = Professions.getProfession(args[1]);
        if (prof == null) {
            return true;
        }
        double exp = Double.parseDouble(args[3]);

        Player target;
        if (args.length >= 5) {
            target = Bukkit.getPlayer(args[4]);
            if (target == null || !target.isValid() || !target.isOnline()) {
                return true;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            return true;
        }
        User targetUser = User.getUser(target);
        switch (args[2]) {
            case "add":
                targetUser.addExp(exp, prof, null);
                break;
            case "set":
                targetUser.setExp(exp, prof);
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "addexp";
    }

}
