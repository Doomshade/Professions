package git.doomshade.professions.profession.types.gathering.herbalism.commands;

import git.doomshade.professions.commands.AbstractCommand;
import git.doomshade.professions.profession.types.gathering.herbalism.Herb;
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

        if (args.length >= 4 && Boolean.parseBoolean(args[3])) {
            herb.forceSpawn(loc);
        } else {
            herb.spawn(loc);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> list = new ArrayList<>();
        List<String> herbs = Herb.HERBS.values().stream().map(Herb::getId).collect(Collectors.toList());
        switch (args.length) {
            case 2:
                list.addAll(herbs);
                break;
            case 3:
                list.addAll(Herb.HERBS.values().stream().filter(x -> x.getId().startsWith(args[2])).map(Herb::getId).collect(Collectors.toList()));
                break;
        }
        return list.isEmpty() ? null : list;
    }

    @Override
    public String getID() {
        return "spawn";
    }
}
