package git.doomshade.professions.commands;

import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ProfessionInfoCommand extends AbstractCommand {

    public ProfessionInfoCommand() {
        args = new HashMap<>();
        args.put(false, Arrays.asList("player"));
        setArgs(args);
        setCommand("info");
        setDescription("Shows all information about a player profession");
        setRequiresOp(false);
        setRequiresPlayer(false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        User user = User.getUser((Player) sender);
        if (user.getProfessions().isEmpty()) {
            user.sendMessage("Nemáš žádné profese");
            return true;
        }
        final List<String> messages = getMessages();
        if (messages.isEmpty()) {
            return true;
        }

        // get a  better way of doing this..
        final String firstMessage = messages.get(0);
        if (!firstMessage.isEmpty())
            user.sendMessage(ChatColor.translateAlternateColorCodes('&', firstMessage).replaceAll("\\{user}", user.getPlayer().getDisplayName()));
        ArrayList<UserProfessionData> profs = new ArrayList<>(user.getProfessions());
        profs.sort(Comparator.comparing(x -> x.getProfession().getProfessionType()));
        for (UserProfessionData prof : profs) {
            for (int i = 1; i < messages.size(); i++) {
                String s = messages.get(i);
                if (i == messages.size() - 1 && s.isEmpty()) {
                    continue;
                }

                for (Regex regex : Regex.values()) {
                    if (s.isEmpty()) {
                        break;
                    }
                    String reg = regex.name().toLowerCase().replaceAll("[_]", "-");
                    String replacement = "";
                    switch (regex) {
                        case EXP:
                            double exp = prof.getExp();
                            if (exp == ((int) exp))
                                replacement = String.valueOf((int) exp);
                            else
                                replacement = String.valueOf(exp);
                            break;
                        case LEVEL:
                            replacement = String.valueOf(prof.getLevel());
                            break;
                        case MAX_LEVEL:
                            replacement = String.valueOf(prof.getLevelCap());
                            break;
                        case PROFESSION:
                            replacement = prof.getProfession().getColoredName();
                            break;
                        case REQ_EXP:
                            replacement = String.valueOf(prof.getRequiredExp());
                            break;
                        case PROFESSION_TYPE:
                            replacement = prof.getProfession().getProfessionType().toString();
                            replacement = replacement.charAt(0) + replacement.toLowerCase().substring(1);
                            break;
                        case USER:
                            replacement = user.getPlayer().getDisplayName();
                            break;
                    }
                    s = ChatColor.translateAlternateColorCodes('&', s.replaceAll("\\{" + reg + "\\}", replacement));
                    s = ChatColor.translateAlternateColorCodes('&', s.replaceAll("\\{" + reg + "-bold\\}", ChatColor.getLastColors(replacement) + ChatColor.BOLD + ChatColor.stripColor(replacement)));
                }
                user.sendMessage(s);
            }
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
        return "professioninfo";
    }

    private enum Regex {
        LEVEL, MAX_LEVEL, EXP, REQ_EXP, PROFESSION, PROFESSION_TYPE, USER
    }

}
