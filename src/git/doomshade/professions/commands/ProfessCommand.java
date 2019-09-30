package git.doomshade.professions.commands;

import git.doomshade.professions.Profession;
import git.doomshade.professions.ProfessionManager;
import git.doomshade.professions.Professions;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.enums.Messages.Message;
import git.doomshade.professions.enums.Messages.MessageBuilder;
import git.doomshade.professions.user.User;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ProfessCommand extends AbstractCommand {

    public ProfessCommand() {
        args = new HashMap<>();
        args.put(true, Arrays.asList("profession"));
        args.put(false, Arrays.asList("player"));
        setCommand("profess");
        setDescription("gives a profession");
        setRequiresOp(true);
        setRequiresPlayer(false);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // TODO Auto-generated method stub
        User user = User.getUser((Player) sender);
        Profession<?> prof = Professions.getProfessionManager().fromName(args[1]);
        MessageBuilder builder = Messages.getInstance().MessageBuilder().setPlayer(user).setProfession(prof);
        if (prof == null) {
            user.sendMessage(builder.copy().setMessage(Message.PROFESSION_DOESNT_EXIST).build());
            return true;
        }
        if (user.profess(prof)) {
            user.sendMessage(builder.copy().setMessage(Message.SUCCESSFULLY_PROFESSED).build());
        } else if (user.hasProfession(prof)) {
            user.sendMessage(builder.copy().setMessage(Message.ALREADY_PROFESSED).build());
        } else {
            user.sendMessage(builder.copy().setMessage(Message.ALREADY_PROFESSED_TYPE).build());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        // TODO Auto-generated method stub
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
                if (user.canProfess(x)) {
                    profs.add(x.getID());
                }
            });
        }
        return profs;
    }

    @Override
    public String getID() {
        // TODO Auto-generated method stub
        return "profess";
    }

}
