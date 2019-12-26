package git.doomshade.professions.commands;

import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class AddExtraCommand extends AbstractCommand {

    public AddExtraCommand() {
        setDescription("Adds an \"extra\" to a profession for requirement purposes");
        setArg(true, Arrays.asList("user", "profession", "extra"));
        setCommand("extra");
        setRequiresOp(true);
        setRequiresPlayer(false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        User user = User.getUser(Bukkit.getPlayer(args[1]));
        Profession<?> prof = Professions.getProfession(args[2]);
        HashSet<String> extras = new HashSet<>();
        for (int i = 3; i < args.length; i++) {
            extras.add(args[i]);
        }

        UserProfessionData upd = user.getProfessionData(prof);
        String extra = extras.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("[,]", "");
        upd.addExtra(extra);
        try {
            user.save();
        } catch (IOException e) {
            e.printStackTrace();
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
