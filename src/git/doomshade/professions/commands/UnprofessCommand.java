package git.doomshade.professions.commands;

import git.doomshade.professions.Profession;
import git.doomshade.professions.ProfessionManager;
import git.doomshade.professions.Professions;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class UnprofessCommand extends AbstractCommand {

    public UnprofessCommand() {
        args = new HashMap<>();
        args.put(true, Arrays.asList("profession"));
        args.put(false, Arrays.asList("player"));
        setArgs(args);
        setCommand("unprofess");
        setDescription("Unprofesses a player");
        setRequiresOp(true);
        setRequiresPlayer(false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        User user = User.getUser((Player) sender);
        Profession<?> prof = Professions.getProfessionManager().getProfession(args[1]);
        Messages.MessageBuilder builder = new Messages.MessageBuilder().setPlayer(user);
        if (prof == null) {
            user.sendMessage(builder.setMessage(Messages.Message.PROFESSION_DOESNT_EXIST).build());
            return true;
        }
        builder = builder.setProfession(prof);
        if (user.unprofess(prof)) {
            user.sendMessage(builder.setMessage(Messages.Message.SUCCESSFULLY_UNPROFESSED).build());
        } else {
            user.sendMessage(builder.setMessage(Messages.Message.ALREADY_PROFESSED).build());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        final List<String> profs = new ArrayList<>();
        User user;
        ProfessionManager profMan = Professions.getProfessionManager();
        Map<String, Profession<?>> map = profMan.getProfessionsById();
        map.forEach((y, x) -> profs.add(x.getID()));
        if (args.length >= 3) {
            user = User.getUser(Bukkit.getPlayer(args[2]));
        } else if (sender instanceof Player) {
            user = User.getUser((Player) sender);
        } else {
            user = null;
        }
        if (user != null) {
            profs.clear();
            map.forEach((y, x) -> {
                if (user.hasProfession(x)) {
                    profs.add(x.getID());
                }
            });
        }
        return profs;
    }

    @Override
    public String getID() {
        return "unprofess";
    }

}
