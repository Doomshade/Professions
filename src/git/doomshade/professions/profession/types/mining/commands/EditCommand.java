package git.doomshade.professions.profession.types.mining.commands;

import git.doomshade.professions.commands.AbstractCommand;
import git.doomshade.professions.profession.types.mining.Ore;
import git.doomshade.professions.profession.types.mining.spawn.OreLocationOptions;
import git.doomshade.professions.profession.types.utils.SpawnPoint;
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
public class EditCommand extends AbstractCommand {

    private final HashSet<Location> EDITED = new HashSet<>();

    /**
     * Setup defaults for command
     */
    public EditCommand() {
        setCommand("edit");
        setRequiresPlayer(true);
        setRequiresOp(true);
        setArg(false, Collections.singletonList("allwool/allore"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        final HashMap<String, Ore> ores = Ore.ORES;
        if (args.length >= 2) {

            switch (args[1].toLowerCase()) {
                case "allwool":
                    for (Ore ore : ores.values()) {
                        ore.getOreLocationOptions().values().forEach(x -> {
                            x.despawn();
                            Location loc = x.location;
                            loc.getBlock().setType(Material.WOOL);
                            EDITED.add(loc);
                        });
                    }
                    break;
                case "allore":
                    for (Ore ore : ores.values()) {
                        ore.getOreLocationOptions().values().forEach(x -> {
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
                Ore ore = Utils.findInIterable(ores.values(), x -> x.getSpawnPoints().contains(new SpawnPoint(loc)));
                final OreLocationOptions locationOptions = ore.getLocationOptions(loc);
                locationOptions.despawn();

                if (EDITED.remove(loc)) {
                    locationOptions.scheduleSpawn();
                } else {
                    EDITED.add(loc);
                    loc.getBlock().setType(Material.WOOL);
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
