package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.Profession.ProfessionType;
import git.doomshade.professions.profession.ProfessionManager;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * Prints the list of all professions
 *
 * @author Doomshade
 * @version 1.0
 */
public class ProfessionListCommand extends AbstractCommand {

    public ProfessionListCommand() {
        setCommand("list");
        setDescription("Shows a list of professions");
        setRequiresPlayer(false);
        addPermission(Permissions.HELPER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        ProfessionManager profMan = Professions.getProfMan();
        Map<ProfessionType, Integer> profTypes = new TreeMap<>();
        List<Profession> profs = new ArrayList<>(profMan.getProfessionsById().values());
        profs.sort(Comparator.naturalOrder());
        List<ProfessionType> pt = Arrays.asList(ProfessionType.values());
        pt.sort(Comparator.naturalOrder());
        pt.forEach(x -> profTypes.put(x, 0));
        profMan.getProfessionsById().forEach((y, x) -> {
            if (x.getProfessionType() != null)
                profTypes.put(x.getProfessionType(), profTypes.get(x.getProfessionType()) + 1);
        });
        profs.forEach(x -> sender.sendMessage(x.getColoredName() + ChatColor.RESET + ", " + x.getProfessionType()));
        profTypes.forEach((x, y) -> sender.sendMessage(x.toString() + ": " + y));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "professionslist";
    }

}
