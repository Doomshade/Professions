package git.doomshade.professions.commands;

import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
        // TODO Auto-generated method stub
        User user = User.getUser((Player) sender);
        if (user.getProfessions().isEmpty()) {
            user.sendMessage("Nemáš žádné profese");
            return true;
        }
        for (UserProfessionData prof : user.getProfessions()) {
            for (int i = 0; i < getMessages().size(); i++) {
                String s = getMessages().get(i);

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
                    }
                    s = ChatColor.translateAlternateColorCodes('&', s.replaceAll("\\{" + reg + "\\}", replacement));
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
        LEVEL, MAX_LEVEL, EXP, REQ_EXP, PROFESSION
    }

}
