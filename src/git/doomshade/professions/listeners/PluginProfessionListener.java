package git.doomshade.professions.listeners;

import com.sucy.skill.api.event.PlayerExperienceGainEvent;
import git.doomshade.professions.Professions;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.ProfessionExpGainEvent;
import git.doomshade.professions.task.CraftingTask;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class PluginProfessionListener implements Listener {
    public static final HashMap<UUID, CraftingTask> PENDING_REPEAT_AMOUNT = new HashMap<>();
    private static final Random random = new Random();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExpGain(ProfessionExpGainEvent e) {
        e.setExp(Settings.getInstance().getExpSettings().getExpMultiplier() * e.getExp());

        int rand = random.nextInt(100) + 1;
        final double chance = e.getSkillupColor().getChance();

        System.out.println("Random: " + rand);
        System.out.println("Skillup: " + e.getSkillupColor());
        System.out.println("Chance skillup: " + chance);
        e.setCancelled(rand > chance);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSapiExpGain(PlayerExperienceGainEvent e) {
        e.setExp(Settings.getInstance().getExpSettings().getSkillapiExpMultiplier() * e.getExp());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        final Player player = e.getPlayer();
        CraftingTask task = PENDING_REPEAT_AMOUNT.remove(player.getUniqueId());
        if (task != null) {
            e.setCancelled(true);
            try {
                int amount = Integer.parseInt(e.getMessage());
                task.setRepeatAmount(amount);
                task.setRepeat(true);
                Professions.getManager().openGui(task.getGui());
                task.setCurrentItem(task.getGui().getInventory().getContents().get(task.getSlot()).getItemStackCopy());
                task.runTask(Professions.getInstance());
            } catch (NumberFormatException e1) {
                player.sendMessage(new Messages.MessageBuilder(Messages.Message.INVALID_REPEAT_AMOUNT).setPlayer(player).setProfession(task.getUpd().getProfession()).build());
            } catch (Exception e2) {
                player.sendMessage(ChatColor.RED + "Nastala neočekávaná chyba. Kontaktuj prosím admina, napiš mu čas, kdy se stala, a pokus se popsat situaci, která nastala.");
                e2.printStackTrace();
            }
        }
    }
}
