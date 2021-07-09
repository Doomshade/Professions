package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.user.User;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

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
    public void onCommand(CommandSender sender, String[] args) {
        Optional<Profession> opt = Professions.getProfessionById(args[1]);
        if (!opt.isPresent()) {
            return;
        }

        Profession prof = opt.get();
        double exp = Double.parseDouble(args[3]);

        Player target;
        if (args.length >= 5) {
            target = Bukkit.getPlayer(args[4]);
            if (target == null || !target.isValid() || !target.isOnline()) {
                return;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            return;
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
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "addexp";
    }

}
