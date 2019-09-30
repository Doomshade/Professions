package git.doomshade.professions.commands;

import git.doomshade.professions.Profession;
import git.doomshade.professions.Profession.ProfessionType;
import git.doomshade.professions.ProfessionManager;
import git.doomshade.professions.Professions;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;

public class ProfessionListCommand extends AbstractCommand {

    public ProfessionListCommand() {
        // TODO Auto-generated constructor stub
        setCommand("list");
        setDescription("shows a sorted list of git.doomshade.professions");
        setRequiresOp(true);
        setRequiresPlayer(false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // TODO Auto-generated method stub
        ProfessionManager profMan = Professions.getProfessionManager();
        Map<ProfessionType, Integer> profTypes = new TreeMap<>();
        List<Profession<?>> profs = new ArrayList<>(profMan.getProfessionsById().values());
        profs.sort((x, y) -> x.compareTo(y));
        List<ProfessionType> pt = Arrays.asList(ProfessionType.values());
        pt.sort((x, y) -> x.compareTo(y));
        pt.forEach(x -> profTypes.put(x, 0));
        profMan.getProfessionsById().forEach((y, x) -> {
            if (x.getProfessionType() != null)
                profTypes.put(x.getProfessionType(), profTypes.get(x.getProfessionType()) + 1);
        });
        profs.forEach(x -> sender.sendMessage(x.getColoredName() + ChatColor.RESET + ", " + x.getProfessionType()));
        profTypes.forEach((x, y) -> sender.sendMessage(x.toString() + ": " + y));
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
        return "professionslist";
    }

}
