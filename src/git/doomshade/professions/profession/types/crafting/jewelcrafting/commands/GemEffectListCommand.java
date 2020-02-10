package git.doomshade.professions.profession.types.crafting.jewelcrafting.commands;

import git.doomshade.professions.commands.AbstractCommand;
import git.doomshade.professions.profession.types.crafting.jewelcrafting.GemUtils;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class GemEffectListCommand extends AbstractCommand {

    public GemEffectListCommand() {
        setCommand("effects");
        setDescription("Displays all possible gem effects");
        setRequiresPlayer(false);

        addPermission(Permissions.HELPER);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(String.join("\n", GemUtils.IDS.keySet()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "gem-effect-list";
    }
}
