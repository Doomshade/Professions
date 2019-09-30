package git.doomshade.professions.listeners;

import com.sucy.skill.api.event.PlayerExperienceGainEvent;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.event.ProfessionExpGainEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PluginProfessionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExpGain(ProfessionExpGainEvent e) {
        e.setExp(Settings.getInstance().getExpSettings().getExpMultiplier() * e.getExp());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSapiExpGain(PlayerExperienceGainEvent e) {
        e.setExp(Settings.getInstance().getExpSettings().getSkillapiExpMultiplier() * e.getExp());
    }
}
