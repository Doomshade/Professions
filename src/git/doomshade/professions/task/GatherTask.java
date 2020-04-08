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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

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

    public GatherTask(LocationOptions locationOptions, UserProfessionData gatherer, ItemStack result, Consumer<GatherResult> endResultAction, BossBarOptions bossBarOptions) {
        this.result = result;
        this.locationOptions = locationOptions;
        this.gatherer = gatherer;
        this.bossBarOptions = bossBarOptions;
        this.endResultAction = endResultAction;
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
        return Bukkit.createBossBar(locationOptions.element.getName(), bossBarOptions.barColor, bossBarOptions.barStyle);
    }

    @Override
    public final synchronized BukkitTask runTaskLater(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {

        // check if we use bossbars
        if (bossBarOptions.useBossBar && Settings.isUseBossBar()) {
            // setup bossbar
            BossBar bossBar = getBossBar();
            bossBar.setProgress(1);
            bossBar.addPlayer(gatherer.getUser().getPlayer());
            final int period = 20;

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
            }.runTaskTimer(plugin, period, period);
        }
        return super.runTaskLater(plugin, delay);
    }

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

    ///////////////////////////////////////////////////////////////////////////
    // Deprecating other tasks
    ///////////////////////////////////////////////////////////////////////////

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

    public enum GatherResult {
        SUCCESS,
        FULL_INVENTORY,
        LOCATION_AIR,
        UNKNOWN
    }

    public static class BossBarOptions {

        // boolean for the API not the user!
        // this boolean is defined by the person coding this
        public boolean useBossBar = false;
        public BarColor barColor = BarColor.WHITE;
        public BarStyle barStyle = BarStyle.SOLID;
    }
}
