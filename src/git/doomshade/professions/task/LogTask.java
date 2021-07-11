package git.doomshade.professions.task;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.ExtendedBukkitRunnable;

import java.time.Clock;
import java.time.Duration;

public class LogTask extends ExtendedBukkitRunnable {

    // 10 minutes
    private static final long LOG_DELAY = Duration.ofMinutes(10).getSeconds();


    @Override
    public void run() {
        Professions.saveLogFile();
    }

    @Override
    protected long delay() {
        return LOG_DELAY * 20L;
    }

    @Override
    protected long period() {
        return LOG_DELAY * 20L;
    }
}
