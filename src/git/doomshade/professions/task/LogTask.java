package git.doomshade.professions.task;

import git.doomshade.professions.Professions;
import org.bukkit.scheduler.BukkitRunnable;

public class LogTask extends BukkitRunnable {


    @Override
    public void run() {
        if (Professions.fos == null) return;

        Professions.fos.flush();
    }
}
