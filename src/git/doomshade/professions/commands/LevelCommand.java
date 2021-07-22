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
