package git.doomshade.professions.profession.professions.herbalism.commands;

import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.profession.professions.herbalism.HerbSpawnPoint;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.CommandSender;

import java.util.function.Consumer;

public class SpawnCommand extends AbstractSpawnCommand {

    private boolean force = false;

    public SpawnCommand() {
        setArg(true, "herb", "all / spawnpoint id");
        setArg(false, "forcespawn (bypass respawn timer and configuration in itemtype, default: false)");
        setCommand("spawn");
        setDescription("Spawns a herb");
        setRequiresPlayer(false);
        addPermission(Permissions.HELPER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {

        if (args.length >= 5) {
            try {
                this.force = Boolean.parseBoolean(args[4]);
            } catch (Exception ignored) {
            }
        }
        super.onCommand(sender, args);
        this.force = false;
    }

    @Override
    protected Consumer<HerbSpawnPoint> consumer() {
        return x -> {
            try {
                if (force) {
                    x.forceSpawn();
                } else {
                    x.spawn();
                }
            } catch (SpawnException e) {
                e.printStackTrace();
            }
        };
    }

    @Override
    public String getID() {
        return "spawn";
    }
}
