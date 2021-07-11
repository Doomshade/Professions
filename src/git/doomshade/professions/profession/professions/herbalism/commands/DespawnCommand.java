package git.doomshade.professions.profession.professions.herbalism.commands;

import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.professions.herbalism.Herb;
import git.doomshade.professions.profession.professions.herbalism.HerbSpawnPoint;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.function.Consumer;

public class DespawnCommand extends AbstractSpawnCommand {

    DespawnCommand() {
        setArg(true, "herb", "all / spawnpoint id");
        setArg(false, "disable further spawn");
        setCommand("despawn");
        setDescription("Despawns a herb");
        setRequiresPlayer(false);
        addPermission(Permissions.HELPER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        boolean disableSpawn = false;

        if (args.length >= 4) {
            try {
                disableSpawn = Boolean.parseBoolean(args[3]);
            } catch (Exception e) {
                sender.sendMessage("Invalid boolean type provided, using false as default.");
            }
        }

        Herb herb = Herb.get(Herb.class, args[1]);
        if (herb == null) {
            sender.sendMessage("Herb with ID " + args[1] + " does not exist.");
            return;
        }
        Object spId;
        try {
            spId = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            if (!args[2].equalsIgnoreCase("all")) {
                sender.sendMessage("Invalid number format.");
                return;
            } else {
                spId = "";
            }
        }

        Location loc = null;
        try {
            if (spId instanceof Integer) {
                loc = herb.getSpawnPointLocations().get((Integer) spId);
            }
        } catch (IndexOutOfBoundsException e) {
            sender.sendMessage("Spawn point with ID " + spId + " does not exist.");
            return;
        }



        if (loc == null) {
            for (Map.Entry<Location, HerbSpawnPoint> entry : herb.getSpawnPoints().entrySet()) {
                final HerbSpawnPoint hlo = entry.getValue();
                Location hloLoc = hlo.location;
                String locName = String.format("%s: %d,%d,%d", hloLoc.getWorld().getName(), hloLoc.getBlockX(), hloLoc.getBlockY(), hloLoc.getBlockZ());
                try {
                    hlo.despawn();
                    sender.sendMessage("Successfully despawned herb at " + locName + ".");
                    if (!disableSpawn) {
                        hlo.scheduleSpawn();
                    }
                } catch (Exception e) {
                    sender.sendMessage("Could not despawn herb at " + locName + ". Check console for error stacktrace.");
                    ProfessionLogger.logError(e);
                }
            }
        } else {
            String locName = String.format("%s: %d,%d,%d", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            try {
                final HerbSpawnPoint hlo = herb.getSpawnPoints(loc);
                hlo.despawn();
                if (!disableSpawn) {
                    hlo.scheduleSpawn();
                }
                sender.sendMessage("Successfully despawned herb at " + locName + ".");
            } catch (Exception e) {
                sender.sendMessage("Could not despawn herb at " + locName + ". Check console for error stacktrace.");
                ProfessionLogger.logError(e);
            }
        }
    }

    @Override
    protected Consumer<HerbSpawnPoint> consumer() {
        return null;
    }

    @Override
    public String getID() {
        return "despawn";
    }
}
