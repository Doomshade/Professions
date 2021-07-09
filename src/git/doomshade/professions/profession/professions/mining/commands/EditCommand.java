package git.doomshade.professions.profession.professions.mining.commands;

import git.doomshade.professions.profession.professions.mining.Ore;
import git.doomshade.professions.profession.professions.mining.spawn.OreSpawnPoint;
import git.doomshade.professions.profession.utils.ExtendedLocation;
import git.doomshade.professions.utils.Permissions;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Command for editing mining areas
 *
 * @author Doomshade
 */
public class EditCommand extends AbstractEditCommand {

    private final HashSet<Location> EDITED = new HashSet<>();

    /**
     * Setup defaults for command
     */
    public EditCommand() {
        setCommand("edit");
        setRequiresPlayer(true);
        setArg(false, "allwool/allore");
        addPermission(Permissions.BUILDER);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        final HashMap<String, Ore> ores = Ore.ORES;
        if (args.length >= 2) {

            switch (args[1].toLowerCase()) {
                case "allwool":
                    for (Ore ore : ores.values()) {
                        ore.getSpawnPoints().values().forEach(x -> {
                            x.despawn();
                            Location loc = x.location;
                            loc.getBlock().setType(Material.WHITE_WOOL);
                            EDITED.add(loc);
                        });
                    }
                    break;
                case "allore":
                    for (Ore ore : ores.values()) {
                        ore.getSpawnPoints().values().forEach(x -> {
                            x.despawn();
                            x.scheduleSpawn();
                        });
                    }
                    EDITED.clear();
                    break;
                default:
                    return true;
            }
        } else {
            Player player = (Player) sender;
            Location loc = player.getTargetBlock((Set<Material>) null, 5).getLocation();
            try {
                Ore ore = Utils.findInIterable(ores.values(), x -> x.getSpawnPointLocations().contains(new ExtendedLocation(loc)));
                final OreSpawnPoint locationOptions = ore.getSpawnPoints(loc);
                locationOptions.despawn();

                if (EDITED.remove(loc)) {
                    locationOptions.scheduleSpawn();
                } else {
                    EDITED.add(loc);
                    loc.getBlock().setType(Material.WHITE_WOOL);
                }
            } catch (Utils.SearchNotFoundException e) {
                return true;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "edit";
    }
}
