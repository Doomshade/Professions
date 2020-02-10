package git.doomshade.professions.profession.types.crafting.alchemy.commands;

import git.doomshade.professions.commands.AbstractCommand;
import git.doomshade.professions.profession.types.crafting.alchemy.Potion;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class PotionsCommand extends AbstractCommand {

    public PotionsCommand() {
        setCommand("potions");
        setRequiresPlayer(false);
        addPermission(Permissions.HELPER);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(Potion.getPotions().toString());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "potions";
    }
}
