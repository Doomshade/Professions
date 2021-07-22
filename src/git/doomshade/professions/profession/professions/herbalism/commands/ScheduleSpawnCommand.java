package git.doomshade.professions.profession.professions.herbalism.commands;

import git.doomshade.professions.api.spawn.ISpawnPoint;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.Permissions;

import java.util.function.Consumer;

/**
 * TODO: Make a /prof-herbalism herb subcommand and add an arguemnt of spawn/despawn/schedule
 */
public class ScheduleSpawnCommand extends AbstractSpawnCommand {

    public ScheduleSpawnCommand() {
        setArg(true, "herb", "all / spawnpoint id");
        setArg(false, "forcespawn (bypass respawn timer and configuration in itemtype, default: false)");
        setCommand("schedulespawn");
        setDescription("Schedules a spawn of a herb");
        setRequiresPlayer(false);
        addPermission(Permissions.HELPER);
    }

    @Override
    public String getID() {
        return "schedulespawn";
    }

    @Override
    protected Consumer<ISpawnPoint> consumer() {
        return x -> {
            try {
                x.scheduleSpawn();
            } catch (SpawnException e) {
                ProfessionLogger.logError(e);
            }
        };
    }
}
