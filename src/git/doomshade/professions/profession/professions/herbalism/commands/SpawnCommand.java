package git.doomshade.professions.profession.professions.herbalism.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.commands.AbstractCommand;
import git.doomshade.professions.profession.professions.herbalism.Herb;
import git.doomshade.professions.profession.professions.herbalism.HerbSpawnPoint;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

public class SpawnCommand extends AbstractCommand {

    public SpawnCommand() {
        setArg(true, "herb", "all / spawnpoint id");
        setArg(false, "forcespawn (bypass respawn timer and configuration in itemtype, default: false)");
        setCommand("spawn");
        setDescription("Spawns a herb");
        setRequiresPlayer(false);
        addPermission(Permissions.HELPER);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Herb herb = Herb.getHerb(args[1]);
        if (herb == null) {
            sender.sendMessage("Herb with ID " + args[1] + " does not exist.");
            return true;
        }
        Object spId;
        try {
            spId = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            if (args[2].equalsIgnoreCase("all")) {
                spId = "";
            } else {
                sender.sendMessage("Invalid number format.");
                return true;
            }
        }
        Location loc = null;
        try {
            if (spId instanceof Integer)
                loc = herb.getSpawnPointLocations().get((Integer) spId);
        } catch (IndexOutOfBoundsException e) {
            sender.sendMessage("Spawn point with ID " + spId + " does not exist.");
            return true;
        }

        boolean force = false;

        if (args.length >= 5) {
            try {
                force = Boolean.parseBoolean(args[4]);
            } catch (Exception ignored) {
            }
        }

        if (loc == null) {
            for (Map.Entry<Location, HerbSpawnPoint> entry : herb.getSpawnPoints().entrySet()) {
                final HerbSpawnPoint hlo = entry.getValue();
                Location hloLoc = hlo.location;
                String locName = String.format("%s: %d,%d,%d", hloLoc.getWorld().getName(), hloLoc.getBlockX(), hloLoc.getBlockY(), hloLoc.getBlockZ());
                try {
                    if (force) hlo.forceSpawn();
                    else hlo.spawn();

                    sender.sendMessage("Successfully spawned herb at " + locName + ".");
                } catch (Exception e) {
                    sender.sendMessage("Could not spawn herb at " + locName + ". Check console for error stacktrace.");
                    Professions.logError(e);
                }
            }
        } else {
            String locName = String.format("%s: %d,%d,%d", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            try {
                final HerbSpawnPoint hlo = herb.getSpawnPoints(loc);
                if (force) hlo.forceSpawn();
                else hlo.spawn();
                sender.sendMessage("Successfully spawned herb at " + locName + ".");
            } catch (Exception e) {
                sender.sendMessage("Could not spawn herb at " + locName + ". Check console for error stacktrace.");
                Professions.logError(e);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length) {
            case 2:
                list.addAll(Herb.HERBS.values().stream().filter(x -> x.getId().startsWith(args[1])).map(Herb::getId).collect(Collectors.toList()));
                break;
            case 3:
                Herb herb = Herb.getHerb(args[1].trim());
                if (herb == null) {
                    sender.sendMessage(args[1] + " is an invalid herb id.");
                    break;
                }
                for (int i = 0; i < herb.getSpawnPointLocations().size(); i++) {
                    String id = String.valueOf(i);
                    if (args[2].startsWith(id)) {
                        list.add(id);
                    } else if (args[2].isEmpty()) {
                        list.add(id);
                    }
                }
                break;
            case 4:
                final List<String> booleans = Arrays.asList("true", "false");
                if (args[3].isEmpty()) {
                    list.addAll(booleans);
                } else {
                    booleans.forEach(x -> {
                        if (x.startsWith(args[3])) {
                            list.add(x);
                        }
                    });
                }
                break;
        }
        return list.isEmpty() ? null : list;
    }

    @Override
    public String getID() {
        return "spawn";
    }
}
