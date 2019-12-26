package git.doomshade.professions.profession.types.gathering.herbalism.commands;

import git.doomshade.professions.commands.AbstractCommand;
import git.doomshade.professions.profession.types.gathering.herbalism.Herb;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class DespawnCommand extends AbstractCommand {

    public DespawnCommand() {
        setArg(true, Arrays.asList("herb id", "all / ID"));
        setCommand("despawn");
        setDescription("Despawns a herb");
        setRequiresOp(true);
        setRequiresPlayer(false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Herb herb = Herb.getHerb(args[1]);
        if (herb == null) {
            sender.sendMessage("Herb with that id does not exist!");
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "despawn";
    }
}
