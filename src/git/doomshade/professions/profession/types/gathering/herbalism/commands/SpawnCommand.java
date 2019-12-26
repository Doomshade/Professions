package git.doomshade.professions.profession.types.gathering.herbalism.commands;

import git.doomshade.professions.commands.AbstractCommand;
import git.doomshade.professions.profession.types.gathering.herbalism.Herb;
import git.doomshade.professions.profession.types.gathering.herbalism.HerbLocationOptions;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SpawnCommand extends AbstractCommand {

    public SpawnCommand() {
        setArg(true, Arrays.asList("herb", "spawnpoint id"));
        setArg(false, Collections.singletonList("forcespawn (bypass respawn timer and configuration in itemtype, default: false)"));
        setCommand("spawn");
        setDescription("Spawns a herb");
        setRequiresOp(true);
        setRequiresPlayer(false);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Herb herb = Herb.getHerb(args[1]);
        if (herb == null) {
            sender.sendMessage("Herb with ID " + args[1] + " does not exist.");
            return true;
        }
        Location loc;
        int spId;
        try {
            spId = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid number format.");
            return true;
        }
        try {
            loc = herb.getSpawnPoints().get(spId).location;
        } catch (IndexOutOfBoundsException e) {
            sender.sendMessage("Spawn point with ID " + spId + " does not exist.");
            return true;
        }

        HerbLocationOptions herbLocationOptions = herb.getHerbLocationOptions(loc);
        if (args.length >= 4 && Boolean.parseBoolean(args[3])) {
            herbLocationOptions.forceSpawn();
        } else {
            herbLocationOptions.spawn();
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
                for (int i = 0; i < herb.getSpawnPoints().size(); i++) {
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
