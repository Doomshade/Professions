package git.doomshade.professions.profession.professions.mining.commands;

import git.doomshade.professions.api.spawn.ISpawnPoint;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.professions.mining.Ore;
import git.doomshade.professions.profession.spawn.Spawnable;
import git.doomshade.professions.utils.Permissions;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Command for editing mining areas
 *
 * @author Doomshade
 */
@SuppressWarnings("ALL")
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
    public void onCommand(CommandSender sender, String[] args) {
        final Map<String, Ore> ores = Spawnable.getElements(Ore.class);
        if (args.length >= 2) {

            switch (args[1].toLowerCase()) {
                case "allwool":
                    for (Ore ore : ores.values()) {
                        ore.getSpawnPoints().forEach(x -> {
                            x.despawn();
                            Location loc = x.getLocation();
                            loc.getBlock().setType(Material.WHITE_WOOL);
                            EDITED.add(loc);
                        });
                    }
                    break;
                case "allore":
                    for (Ore ore : ores.values()) {
                        ore.getSpawnPoints().forEach(x -> {
                            x.despawn();
                            try {
                                x.scheduleSpawn();
                            } catch (SpawnException e) {
                                ProfessionLogger.logError(e);
                            }
                        });
                    }
                    EDITED.clear();
                    break;
                default:
                    return;
            }
        } else {
            Player player = (Player) sender;
            Location loc = Utils.getLookingAt(player).getLocation();
            try {
                Ore ore = Utils.findInIterable(ores.values(), x -> x.isSpawnPoint(loc));
                final ISpawnPoint locationOptions = ore.getSpawnPoint(loc);
                locationOptions.despawn();

                if (EDITED.remove(loc)) {
                    locationOptions.scheduleSpawn();
                } else {
                    EDITED.add(loc);
                    loc.getBlock().setType(Material.WHITE_WOOL);
                }
            } catch (SpawnException e) {
                ProfessionLogger.logError(e);
            } catch (Utils.SearchNotFoundException e) {
                return;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "edit";
    }
}
