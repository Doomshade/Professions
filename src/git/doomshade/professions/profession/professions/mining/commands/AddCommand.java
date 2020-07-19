package git.doomshade.professions.profession.professions.mining.commands;

import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.profession.professions.mining.Ore;
import git.doomshade.professions.profession.utils.SpawnPoint;
import git.doomshade.professions.utils.Permissions;
import git.doomshade.professions.utils.Range;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class AddCommand extends AbstractEditCommand {

    public AddCommand() {
        setCommand("add");
        setDescription("Marks the block you are looking at as an ore");
        setRequiresPlayer(true);
        setArg(true, Arrays.asList("ore", "respawn time (e.g. 4 or 5-8)"));
        addPermission(Permissions.BUILDER);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        Player player = (Player) sender;
        Ore ore = Ore.getOre(args[1]);

        if (ore == null) {
            player.sendMessage("Invalid ore id");
            return true;
        }
        Location lookingAt = Utils.getLookingAt(player);
        if (lookingAt == null) {
            player.sendMessage("You must be looking at some block");
            return true;
        }

        Range respawnTime = null;
        try {
            respawnTime = Range.fromString(args[2]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (respawnTime == null) {
            player.sendMessage("Invalid respawn time");
            return true;
        }

        ore.addSpawnPoint(new SpawnPoint(lookingAt, respawnTime));
        try {
            ore.getLocationOptions(lookingAt).spawn();
        } catch (SpawnException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "add";
    }
}
