package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.profession.Profession;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Adds levels or sets the level or player's profession
 *
 * @author Doomshade
 * @version 1.0
 */
public class LevelCommand extends AbstractCommand {

    public LevelCommand() {
        setArg(true, Arrays.asList("profession", "add/set", "level"));
        setArg(false, Arrays.asList("player"));
        setCommand("level");
        setRequiresPlayer(false);
        setDescription("Adds levels or sets the level of the player");
        addPermission(Permissions.HELPER);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player target;
        if (!(sender instanceof Player)) {
            if (args.length < 5) {
                return false;
            }
            target = Bukkit.getPlayer(args[4]);
        } else {
            if (args.length >= 5) {
                target = Bukkit.getPlayer(args[4]);
            } else {
                target = (Player) sender;
            }
        }
        Profession prof = Professions.getProfession(args[1]);
        User user = User.getUser(target);
        if (!user.hasProfession(prof)) {
            return false;
        }
        UserProfessionData upd = user.getProfessionData(prof);

        // can't happen, but IDE won't stfu
        if (upd == null) {
            return false;
        }

        int level = Integer.parseInt(args[3]);
        switch (args[2].toLowerCase()) {
            case "set":
                upd.setLevel(level);
                break;
            case "add":
                upd.addLevel(level);
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
        return "level";
    }
}
