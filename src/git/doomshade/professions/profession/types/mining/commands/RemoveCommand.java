package git.doomshade.professions.profession.types.mining.commands;

import git.doomshade.professions.profession.types.mining.Ore;
import git.doomshade.professions.profession.types.utils.SpawnPoint;
import git.doomshade.professions.utils.Permissions;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class RemoveCommand extends AbstractEditCommand {

    public RemoveCommand() {
        setCommand("remove");
        setDescription("Removes an ore you are currently looking at from spawn points or optionally via args");
        setRequiresPlayer(true);
        setArg(false, Arrays.asList("ore id", "spawnpoint id"));
        addPermission(Permissions.BUILDER);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player hrac = (Player) sender;

        if (args.length == 1) {
            Location loc = Utils.getLookingAt(hrac);
            Ore ore;
            try {
                ore = Utils.findInIterable(Ore.ORES.values(), x -> x.isSpawnPoint(loc));
            } catch (Utils.SearchNotFoundException e) {
                hrac.sendMessage("Block you are looking at is no ore");
                return true;
            }
            ore.removeSpawnPoint(new SpawnPoint(loc));
            sender.sendMessage("Successfully removed spawn point");
        } else {
            if (args.length < 3) {
                hrac.sendMessage("You must enter both ore and spawn point id!");
                return true;
            }

            Ore ore = Ore.getOre(args[1]);

            if (ore == null) {
                hrac.sendMessage("Invalid ore id");
                return true;
            }

            int spawnPointId;

            final String message = "Invalid spawn point id (number required)";
            try {
                spawnPointId = Integer.parseInt(args[2]);
                if (spawnPointId >= ore.getSpawnPoints().size()) {
                    hrac.sendMessage(message);
                    return true;
                }
            } catch (NumberFormatException e) {
                hrac.sendMessage(message);
                return true;
            }

            ore.removeSpawnPoint(spawnPointId);
            sender.sendMessage("Successfully removed spawn point");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "remove";
    }
}
