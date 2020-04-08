package git.doomshade.professions.task;

import git.doomshade.professions.data.Settings;
import git.doomshade.professions.profession.utils.LocationOptions;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A task for gathering profession type purposes
 *
 * @author Doomshade
 * @version 1.0
 * @see git.doomshade.professions.profession.types.IGathering
 */
public class GatherTask extends BukkitRunnable {

    protected final LocationOptions locationOptions;
    protected final ItemStack result;
    protected final UserProfessionData gatherer;
    protected final Consumer<GatherResult> endResultAction;
    protected final BossBarOptions bossBarOptions;
    private static final HashMap<UUID, BossBar> ACTIVE_BOSSBARS = new HashMap<>();
    private static final HashSet<GatherTask> TASKS = new HashSet<>();
    private Predicate<EntityDamageEvent> damageEventPredicate;

    public GatherTask(LocationOptions locationOptions, UserProfessionData gatherer, ItemStack result, Consumer<GatherResult> endResultAction, BossBarOptions bossBarOptions) {
        this.result = result;
        this.locationOptions = locationOptions;
        this.gatherer = gatherer;
        this.bossBarOptions = bossBarOptions;
        this.endResultAction = endResultAction;
        TASKS.add(this);
    }

    public static void onGathererDamaged(EntityDamageEvent damageEvent) {
        for (GatherTask gatherTask : TASKS) {
            if (gatherTask.damageEventPredicate == null) return;

            if (!gatherTask.damageEventPredicate.test(damageEvent)) {
                gatherTask.setResult(GatherResult.CANCELLED_BY_DAMAGE);
                gatherTask.cancel();
            }
        }
    }

    public void setOnGathererDamaged(Predicate<EntityDamageEvent> damageEventPredicate) {
        this.damageEventPredicate = damageEventPredicate;
    }

    private void setResult(GatherResult gatherResult) {
        endResultAction.accept(gatherResult);
    }

    @Override
    public final void run() {
        Location location = locationOptions.location;
        if (location.getBlock().getType() == Material.AIR) {
            setResult(GatherResult.LOCATION_AIR);
            return;
        }
        final PlayerInventory inventory = gatherer.getUser().getPlayer().getInventory();
        if (inventory.firstEmpty() == -1 && !inventory.contains(result)) {
            setResult(GatherResult.FULL_INVENTORY);
            return;
        }

        try {
            locationOptions.despawn();
            locationOptions.scheduleSpawn();
            inventory.addItem(result);
            setResult(GatherResult.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BossBar getBossBar() {
        return Bukkit.createBossBar(bossBarOptions.title, bossBarOptions.barColor, bossBarOptions.barStyle);
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();

        final UUID uuid = gatherer.getUser().getPlayer().getUniqueId();
        final BossBar bossBar = ACTIVE_BOSSBARS.get(uuid);
        if (bossBar == null) return;

        bossBar.removeAll();
        ACTIVE_BOSSBARS.remove(uuid);
    }

    @Override
    public final synchronized BukkitTask runTaskLater(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        final Player player = gatherer.getUser().getPlayer();
        final BossBar bossBar = getBossBar();

        if (ACTIVE_BOSSBARS.putIfAbsent(player.getUniqueId(), bossBar) != null) {
            return null;
        }

        // check if we use bossbars
        if (bossBarOptions.useBossBar && Settings.isUseBossBar()) {
            // setup bossbar
            bossBar.setProgress(1);
            bossBar.addPlayer(player);
            final int period = 1;

            // create a runnable for the bossbar
            new BukkitRunnable() {
                double currTime = 0;
                boolean cancel = false;

                @Override
                public void run() {

                    if (cancel) {
                        cancel();
                        return;
                    }

                    if (currTime > delay) {
                        currTime = delay;
                        cancel = true;
                    }

                    bossBar.setProgress((delay - currTime) / delay);
                    currTime += period;
                }
            }.runTaskTimer(plugin, 0, period);
        }
        return super.runTaskLater(plugin, delay);
    }

    public enum GatherResult {
        SUCCESS,
        FULL_INVENTORY,
        LOCATION_AIR,
        CANCELLED_BY_DAMAGE,
        UNKNOWN
    }

    public static class BossBarOptions {

        // boolean for the API not the user!
        // this boolean is defined by the person coding this
        public boolean useBossBar = false;
        public BarColor barColor = BarColor.WHITE;
        public BarStyle barStyle = BarStyle.SOLID;
        public String title = "";
    }

    ///////////////////////////////////////////////////////////////////////////
    // Deprecating other tasks
    ///////////////////////////////////////////////////////////////////////////

    @Override
    @Deprecated
    public final synchronized BukkitTask runTaskLaterAsynchronously(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        return null;
    }

    @Override
    @Deprecated
    public synchronized BukkitTask runTask(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        return null;
    }

    @Override
    @Deprecated
    public final synchronized BukkitTask runTaskAsynchronously(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        return null;
    }

    @Override
    @Deprecated
    public final synchronized BukkitTask runTaskTimer(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        return null;
    }

    @Override
    @Deprecated
    public final synchronized BukkitTask runTaskTimerAsynchronously(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        return null;
    }


}
