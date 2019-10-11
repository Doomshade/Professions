package git.doomshade.professions.task;

import git.doomshade.professions.Professions;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;

public class UserSaveTask extends BukkitRunnable {
    @Override
    public void run() {
        try {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                return;
            }
            Professions.getInstance().saveFiles();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
