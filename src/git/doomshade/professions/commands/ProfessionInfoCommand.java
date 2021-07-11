package git.doomshade.professions.commands;

import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p> Prints information to the sender about a profession. The "message" list must not be an empty array in prof.yml, otherwise this prints nothing.
 * <p> Available arguments:
 * <ul>
 *     <li>level</li>
 *     <li>max-level</li>
 *     <li>exp</li>
 *     <li>req-exp</li>
 *     <li>profession</li>
 *     <li>profession-type</li>
 *     <li>user</li>
 *     <li></li>
 *     <li>level-bold</li>
 *     <li>max-level-bold</li>
 *     <li>exp-bold</li>
 *     <li>req-exp-bold</li>
 *     <li>profession-bold</li>
 *     <li>profession-type-bold</li>
 *     <li>user-bold</li>
 * </ul>
 *
 * @author Doomshade
 * @version 1.0
 */
public class ProfessionInfoCommand extends AbstractCommand {

    public ProfessionInfoCommand() {
        setArg(false, "player");
        setCommand("info");
        setDescription("Shows all information about a player profession");
        setRequiresPlayer(false);
        addPermission(Permissions.DEFAULT_COMMAND_USAGE);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        final User user;

        if (args.length >= 2) {
            user = User.getUser(Bukkit.getPlayer(args[1]));
        } else if (sender instanceof Player) {
            user = User.getUser((Player) sender);
        } else {
            return;
        }

        if (user == null) {
            sender.sendMessage("No user named " + args[1] + " is currently on the server.");
            return;
        }
        if (user.getProfessions().isEmpty()) {
            sender.sendMessage(user.getPlayer().getName() + " has no professions");
            return;
        }
        final List<String> messages = getMessages();
        if (messages.isEmpty()) {
            return;
        }

        // get a  better way of doing this..
        final String firstMessage = messages.get(0);
        if (!firstMessage.isEmpty())
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', firstMessage).replaceAll("\\{user}", user.getPlayer().getDisplayName()));
        Collection<UserProfessionData> profs = user.getProfessions()
                .stream()
                .map(x -> (UserProfessionData) x)
                .sorted(Comparator.comparing(x -> x.getProfession().getProfessionType()))
                .collect(Collectors.toList());
        for (UserProfessionData prof : profs) {
            for (int i = 1; i < messages.size(); i++) {
                String s = messages.get(i);
                sender.sendMessage(Regex.replaceAll(s, prof));
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "professioninfo";
    }

    public enum Regex {
        LEVEL(UserProfessionData::getLevel),
        MAX_LEVEL(UserProfessionData::getLevelCap),
        EXP(x -> (int) x.getExp()),
        REQ_EXP(UserProfessionData::getRequiredExp),
        PROFESSION(x -> x.getProfession().getColoredName()),
        PROFESSION_TYPE(x -> x.getProfession().getProfessionType().toString()),
        USER(x -> x.getUser().getPlayer().getDisplayName());

        private final Function<UserProfessionData, Object> fun;

        Regex(Function<UserProfessionData, Object> fun) {
            this.fun = fun;
        }

        static String replaceAll(String s, UserProfessionData prof) {
            for (Regex regex : values()) {
                if (s.isEmpty()) {
                    break;
                }
                String reg = regex.name().toLowerCase().replaceAll("[_]", "-");
                String replacement = regex.getReplacement(prof);
                s = ChatColor.translateAlternateColorCodes('&', s.replaceAll("\\{" + reg + "}", replacement));
                s = ChatColor.translateAlternateColorCodes('&', s.replaceAll("\\{" + reg + "-bold}", ChatColor.getLastColors(replacement) + ChatColor.BOLD + ChatColor.stripColor(replacement)));
            }
            return s;
        }

        private String getReplacement(UserProfessionData prof) {
            return fun.apply(prof).toString();
        }
    }

}
