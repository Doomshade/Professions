package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.enums.Messages.Global;
import git.doomshade.professions.enums.Messages.MessageBuilder;
import git.doomshade.professions.profession.ProfessionManager;
import git.doomshade.professions.user.User;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Professes a player
 *
 * @author Doomshade
 * @version 1.0
 */
@SuppressWarnings("ALL")
public class ProfessCommand extends AbstractCommand {

    public ProfessCommand() {
        setArg(true, "profession");
        setArg(false, "player");
        setCommand("profess");
        setDescription("Professes a player");
        setRequiresPlayer(false);
        addPermission(Permissions.HELPER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        User user;
        if (args.length >= 3) {
            final Player player = Bukkit.getPlayer(args[2]);
            if (player == null) {
                sender.sendMessage("Invalid user name");
                return;
            }
            user = User.getUser(player);
        } else if (sender instanceof Player) {
            user = User.getUser((Player) sender);
        } else {
            sender.sendMessage("Enter user's name please");
            return;
        }
        Optional<Profession> opt = Professions.getProfMan().getProfession(args[1]);
        MessageBuilder builder = new Messages.MessageBuilder().player(user);
        if (opt.isEmpty()) {
            user.sendMessage(builder.message(Global.PROFESSION_DOESNT_EXIST).build());
            return;
        }
        Profession prof = opt.get();
        builder = builder.profession(prof);
        if (user.profess(prof)) {
            user.sendMessage(builder.message(Global.SUCCESSFULLY_PROFESSED).build());
        } else if (user.hasProfession(prof)) {
            user.sendMessage(builder.message(Global.ALREADY_PROFESSED).build());
        } else {
            user.sendMessage(builder.message(Global.ALREADY_PROFESSED_TYPE).build());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        final List<String> profs = new ArrayList<>();
        User user;
        ProfessionManager profMan = (ProfessionManager) Professions.getProfMan();
        Map<String, Profession> map = profMan.getProfessionsById();
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
        return "profess";
    }

}
