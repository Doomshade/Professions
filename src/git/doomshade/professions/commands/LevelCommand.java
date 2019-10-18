package git.doomshade.professions.commands;

import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class LevelCommand extends AbstractCommand {

    public LevelCommand() {
        setArg(true, Arrays.asList("profession", "add/set", "level"));
        setArg(false, Arrays.asList("player"));
        setRequiresOp(true);
        setCommand("level");
        setRequiresPlayer(false);
        setDescription("Adds levels or sets the level of the player");
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
        Profession<?> prof = Professions.fromName(args[1]);
        User user = User.getUser(target);
        if (!user.hasProfession(prof)) {
            return false;
        }
        UserProfessionData upd = user.getProfessionData(prof);
        int level = Integer.parseInt(args[3]);
        switch (args[2]) {
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
