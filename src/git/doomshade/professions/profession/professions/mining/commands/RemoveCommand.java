package git.doomshade.professions.profession.professions.mining.commands;

import git.doomshade.professions.profession.professions.mining.Ore;
import git.doomshade.professions.profession.spawn.Spawnable;
import git.doomshade.professions.utils.Permissions;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings("ALL")
public class RemoveCommand extends AbstractEditCommand {

    public RemoveCommand() {
        setCommand("remove");
        setDescription("Removes an ore you are currently looking at from spawn points or optionally via args");
        setRequiresPlayer(true);
        setArg(false, "ore id", "spawnpoint id");
        addPermission(Permissions.BUILDER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player hrac = (Player) sender;

        if (args.length == 1) {
            Location loc = Utils.getLookingAt(hrac).getLocation();
            Ore ore;
            try {
                ore = Utils.findInIterable(Spawnable.getElements(Ore.class).values(), x -> x.isSpawnPoint(loc));
            } catch (Utils.SearchNotFoundException e) {
                hrac.sendMessage("Block you are looking at is no ore");
                return;
            }
            ore.removeSpawnPoint(loc);
        } else {
            if (args.length < 3) {
                hrac.sendMessage("You must enter both ore and spawn point id!");
                return;
            }

            Ore ore = Ore.get(Ore.class, args[1]);

            if (ore == null) {
                hrac.sendMessage("Invalid ore id");
                return;
            }

            int serialNumber;

            try {
                serialNumber = Integer.parseInt(args[2]);

                if (!ore.isSpawnPoint(serialNumber)) {
                    hrac.sendMessage(String.format("The serial number %d of ore %s does not exist!", serialNumber,
                            ore.getName()));
                    return;
                }
            } catch (NumberFormatException e) {
                hrac.sendMessage("Invalid serial number (number required)");
                return;
            }

            ore.removeSpawnPoint(serialNumber);
        }
        sender.sendMessage("Successfully removed spawn point");

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "remove";
    }
}
