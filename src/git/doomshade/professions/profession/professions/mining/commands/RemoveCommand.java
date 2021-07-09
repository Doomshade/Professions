package git.doomshade.professions.profession.professions.mining.commands;

import git.doomshade.professions.profession.professions.mining.Ore;
import git.doomshade.professions.profession.utils.ExtendedLocation;
import git.doomshade.professions.utils.Permissions;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

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
                ore = Utils.findInIterable(Ore.ORES.values(), x -> x.isSpawnPointLocation(loc));
            } catch (Utils.SearchNotFoundException e) {
                hrac.sendMessage("Block you are looking at is no ore");
                return;
            }
            ore.removeSpawnPoint(new ExtendedLocation(loc));
        } else {
            if (args.length < 3) {
                hrac.sendMessage("You must enter both ore and spawn point id!");
                return;
            }

            Ore ore = Ore.getOre(args[1]);

            if (ore == null) {
                hrac.sendMessage("Invalid ore id");
                return;
            }

            int spawnPointId;

            final String message = "Invalid spawn point id (number required)";
            try {
                spawnPointId = Integer.parseInt(args[2]);
                if (spawnPointId >= ore.getSpawnPointLocations().size()) {
                    hrac.sendMessage(message);
                    return;
                }
            } catch (NumberFormatException e) {
                hrac.sendMessage(message);
                return;
            }

            ore.removeSpawnPoint(spawnPointId);
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
