package git.doomshade.professions.commands;

import git.doomshade.professions.user.User;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class BypassCommand extends AbstractCommand {

    public BypassCommand() {
        setArg(false, Arrays.asList("player", "suppress exp event (true/false)"));
        setArg(true, Arrays.asList("true/false"));
        setCommand("bypass");
        setDescription("Allows user to bypass level restrictions");
        setRequiresPlayer(false);
        addPermission(Permissions.ADMIN);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayer(args[2]);
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            return false;
        }
        User user = User.getUser(target);
        boolean bypass = Boolean.parseBoolean(args[1]);
        user.setBypass(bypass);
        String message = "Hraci " + target.getDisplayName() + " nastaven bypass na " + bypass;
        if (args.length >= 4) {
            boolean suppress = Boolean.parseBoolean(args[3]);
            user.setSuppressExpEvent(suppress);
            message += " a suppress event na " + suppress;
        }
        sender.sendMessage(message);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "bypass";
    }

}
