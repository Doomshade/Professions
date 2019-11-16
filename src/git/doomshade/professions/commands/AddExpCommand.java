package git.doomshade.professions.commands;

import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AddExpCommand extends AbstractCommand {

    public AddExpCommand() {
        // TODO Auto-generated constructor stub
        args = new HashMap<>();
        args.put(true, Arrays.asList("profession", "add/set", "exp"));
        args.put(false, Arrays.asList("player"));
        setArgs(args);
        setCommand("exp");
        setDescription("Gives exp to the player or sets it");
        setRequiresOp(true);
        setRequiresPlayer(false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Profession<? extends IProfessionType> prof = Professions.getProfession(args[1]);
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getID() {
        // TODO Auto-generated method stub
        return "addexp";
    }

}
