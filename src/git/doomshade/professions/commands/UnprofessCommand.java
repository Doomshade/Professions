package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.profession.ProfessionManager;
import git.doomshade.professions.user.User;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Unprofesses a player
 *
 * @author Doomshade
 * @version 1.0
 */
@SuppressWarnings("ALL")
public class UnprofessCommand extends AbstractCommand {

    public UnprofessCommand() {
        setArg(true, "profession");
        setArg(false, "player");
        setCommand("unprofess");
        setDescription("Unprofesses a player");
        setRequiresPlayer(false);

        addPermission(Permissions.HELPER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        User user = User.getUser((Player) sender);
        Optional<Profession> opt = Professions.getProfMan().getProfession(args[1]);
        Messages.MessageBuilder builder = new Messages.MessageBuilder().player(user);
        if (opt.isEmpty()) {
            user.sendMessage(builder.message(Messages.Global.PROFESSION_DOESNT_EXIST).build());
            return;
        }
        Profession prof = opt.get();
        builder = builder.profession(prof);
        if (user.unprofess(prof)) {
            user.sendMessage(builder.message(Messages.Global.SUCCESSFULLY_UNPROFESSED).build());
        } else {
            user.sendMessage(builder.message(Messages.Global.NOT_PROFESSED).build());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        final List<String> profs = new ArrayList<>();
        User user;
        ProfessionManager profMan = Professions.getProfMan();
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
