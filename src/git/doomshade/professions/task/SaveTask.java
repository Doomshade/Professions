package git.doomshade.professions.task;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.ExtendedBukkitRunnable;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.time.Duration;

/**
 * User saving task.
 *
 * @author Doomshade
 */
public class SaveTask extends ExtendedBukkitRunnable {
    // 5 minutes
    private static final long SAVE_DELAY = Duration.ofMinutes(5).getSeconds();

    @Override
    public void run() {
        try {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return;
            }
            Professions.saveFiles();

        } catch (IOException e) {
            Professions.logError(e);
        }
    }

    @Override
    protected long delay() {
        return SAVE_DELAY * 20L;
    }

    @Override
    protected long period() {
        return SAVE_DELAY * 20L;
    }
}
