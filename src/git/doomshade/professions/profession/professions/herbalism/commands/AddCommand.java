package git.doomshade.professions.profession.professions.herbalism.commands;

import git.doomshade.professions.commands.AbstractCommand;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.professions.herbalism.Herb;
import git.doomshade.professions.profession.utils.ExtendedLocation;
import git.doomshade.professions.utils.Permissions;
import git.doomshade.professions.utils.Range;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddCommand extends AbstractCommand {
    public AddCommand() {
        setCommand("add");
        setDescription("Marks the block you are looking at as a herb");
        setRequiresPlayer(true);
        setArg(true, "herb", "respawn time (e.g. 4 or 5-8)");
        addPermission(Permissions.BUILDER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Herb herb = Herb.get(Herb.class, args[1]);

        if (herb == null) {
            player.sendMessage("Invalid herb id");
            return;
        }
        Location lookingAt = Utils.getLookingAt(player).getLocation();
        if (lookingAt.getBlock().getBlockData().getMaterial().isAir()) {
            player.sendMessage("You must be looking at some block");
            return;
        }

        Range respawnTime = null;
        try {
            respawnTime = Range.fromString(args[2]);
        } catch (Exception e) {
            ProfessionLogger.logError(e);
        }
        if (respawnTime == null) {
            player.sendMessage("Invalid respawn time");
            return;
        }

        herb.addSpawnPoint(new ExtendedLocation(lookingAt, respawnTime));
        try {
            herb.getSpawnPoints(lookingAt).spawn();
        } catch (SpawnException e) {
            ProfessionLogger.logError(e);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();
        switch (args.length) {
            case 2:
                list.addAll(Herb.getElements(Herb.class).values().stream().filter(x -> x.getId().startsWith(args[1])).map(Herb::getId).collect(Collectors.toList()));
                break;
            case 3:
                Herb herb = Herb.get(Herb.class, args[1].trim());
                if (herb == null) {
                    sender.sendMessage(args[1] + " is an invalid herb id.");
                }
                break;
        }
        return list.isEmpty() ? null : list;
    }

    @Override
    public String getID() {
        return "add";
    }
}
