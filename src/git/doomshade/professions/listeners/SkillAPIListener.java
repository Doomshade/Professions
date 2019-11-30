package git.doomshade.professions.listeners;

import com.sucy.skill.api.event.PlayerExperienceGainEvent;
import git.doomshade.professions.data.ExpSettings;
import git.doomshade.professions.data.Settings;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SkillAPIListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSapiExpGain(PlayerExperienceGainEvent e) {
        e.setExp(Settings.getSettings(ExpSettings.class).getSkillapiExpMultiplier() * e.getExp());
    }


}
