package git.doomshade.professions.profession.professions.jewelcrafting.commands;

import git.doomshade.professions.commands.AbstractCommand;
import git.doomshade.professions.profession.professions.jewelcrafting.Gem;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GiveCommand extends AbstractCommand {

    public GiveCommand() {


        setCommand("give");
        setDescription("Gives a player a gem");
        setArg(true, "gem id");
        setArg(false, "player");
        setRequiresPlayer(false);
        addPermission(Permissions.HELPER);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Optional<Gem> opt = Gem.getGem(args[1]);
        if (!opt.isPresent()) {
            sender.sendMessage("Gem with " + args[1] + " id does not exist");
            return true;
        }
        Gem gem = opt.get();

        Player to;
        if (args.length >= 3) {
            to = Bukkit.getPlayer(args[2]);
        } else if (sender instanceof Player) {
            to = (Player) sender;
        } else {
            sender.sendMessage("You must specify a player!");
            return true;
        }

        to.getInventory().addItem(gem.getGem());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return args.length == 2 ? new ArrayList<>(Gem.GEMS.keySet()) : null;
    }

    @Override
    public String getID() {
        return "give";
    }
}
