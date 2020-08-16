package git.doomshade.professions.task;

import git.doomshade.professions.Professions;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.profession.utils.LocationOptions;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * A task for gathering profession type purposes
 *
 * @author Doomshade
 * @version 1.0
 */
public class GatherTask extends BukkitRunnable {

    /**
     * The tasks for event calling. A task is added in {@link #runTaskLater(Plugin, long)} and removed in {@link #cancel()}
     */
    private static final HashMap<UUID, GatherTask> TASKS = new HashMap<>();

    // let these be protected for further polymorphism purposes
    protected final LocationOptions locationOptions;
    protected final ItemStack result;
    protected final UserProfessionData gatherer;
    protected final Consumer<GatherResult> endResultAction;
    protected final BossBarOptions bossBarOptions;
    // create a reference for the player so the gatherer.getUser().getPlayer() is not called so often
    protected final Player player;
    protected BossBar bossBar;

    private Predicate<EntityDamageEvent> damageEventPredicate;
    private Predicate<Double> movePredicate;

    /**
     * @param locationOptions the location options containing location and spawn methods
     * @param gatherer        the gathering player to call this task upon
     * @param result          the expected result of the gathering task
     * @param endResultAction the action to be performed depending on the gather result
     * @param bossBarOptions  the bossbar options of gathering task
     */
    public GatherTask(LocationOptions locationOptions, UserProfessionData gatherer, ItemStack result, Consumer<GatherResult> endResultAction, BossBarOptions bossBarOptions) {
        this.result = result;
        this.locationOptions = locationOptions;
        this.gatherer = gatherer;
        this.bossBarOptions = bossBarOptions;
        this.endResultAction = endResultAction;
        this.player = gatherer.getUser().getPlayer();
    }

    /**
     * Calls the event for the gatherer's current task if there's one
     * <p>Note that We let the argument be an {@link EntityDamageEvent} so that the predicate has more options</p>
     *
     * @param damageEvent the damage event
     */
    public static void onGathererDamaged(EntityDamageEvent damageEvent) {

        // check if the damaged entity was actually a player
        final Entity entity = damageEvent.getEntity();
        if (!(entity instanceof Player)) return;
        Player damagedPlayer = (Player) entity;

        GatherTask gatherTask = TASKS.get(damagedPlayer.getUniqueId());

        // check for gather task
        if (gatherTask == null) return;

        // check for damage event
        final Predicate<EntityDamageEvent> predicate = gatherTask.damageEventPredicate;
        if (predicate == null) return;

        // there is a damage event and the gatherer of this task was damaged -> check
        gatherTask.onEvent(predicate, damageEvent, GatherResult.CANCELLED_BY_DAMAGE);
    }

    /**
     * Calls the event for the gatherer's current task if there's one
     *
     * @param movedPlayer the gatherer
     * @param length      the length walked from the gathering point
     */
    public static void onGathererMoved(Player movedPlayer, double length) {

        GatherTask gatherTask = TASKS.get(movedPlayer.getUniqueId());

        // check for gather task
        if (gatherTask == null) return;

        // check for move event
        final Predicate<Double> predicate = gatherTask.movePredicate;
        if (predicate == null) return;

        // there is a move event and the gatherer of this task moved -> check
        gatherTask.onEvent(predicate, length, GatherResult.CANCELLED_BY_MOVE);
    }

    public static boolean isActive(Player player) {
        return TASKS.containsKey(player.getUniqueId());
    }

    /**
     * Calls the given predicate, set's the gather tasks result if the predicate returns {@code true} and cancel's the event.
     *
     * @param predicate the predicate
     * @param testedArg the tested arg in the predicate
     * @param result    the result if the predicate returns {@code true}
     * @param <T>       the predicate type
     */
    private <T> void onEvent(Predicate<T> predicate, T testedArg, GatherResult result) {
        if (predicate.test(testedArg)) {
            setResult(result);
            cancel();
        }
    }

    /**
     * Sets the action to be performed when the gatherer is damaged.<br>
     * The predicate should return {@code true} should the event be cancelled, {@code false} otherwise.<br>
     * This allows you to perform an action before the task is cancelled such as cleanup or, for example, check for the damage amount and cancel the event based on that.
     *
     * @param damageEventPredicate the action to be performed
     */
    public void setOnGathererDamaged(Predicate<EntityDamageEvent> damageEventPredicate) {
        this.damageEventPredicate = damageEventPredicate;
    }

    /**
     * Sets the action to be performed when the gatherer moves.<br>
     * The predicate should return {@code true} should the event be cancelled, {@code false} otherwise.
     * <p>Note that the parameter could be a double, but for the API's reasons it shall remain a {@link Predicate}</p>
     *
     * @param movePredicate the acceptable distance
     */
    public void setOnMoved(Predicate<Double> movePredicate) {
        this.movePredicate = movePredicate;
    }

    /**
     * Calls the result action to be performed once the task is done. If the result is unknown, a message is logged and sent to the gatherer.
     *
     * @param gatherResult the result of this task
     */
    private void setResult(GatherResult gatherResult) {
        if (endResultAction != null && gatherResult != null)
            endResultAction.accept(gatherResult);
        if (gatherResult == null || gatherResult == GatherResult.UNKNOWN) {
            final String msg = ChatColor.RED + "The block was not gathered for unknown reasons. Contact the admins and try to describe the problem thoroughly.";
            player.sendMessage(msg);
            Professions.log(msg, Level.CONFIG);
        }
    }

    @Override
    public final void run() {
        cancelTask();
        Location location = locationOptions.location;

        // someone has already gathered the expected block
        // this does not fully prevent the duplication of gathering but should suffice
        if (location.getBlock().getType() == Material.AIR) {
            setResult(GatherResult.LOCATION_AIR);
            return;
        }

        // the player has no empty space in inventory but the task was successful, so drop the item on the ground
        final PlayerInventory inventory = player.getInventory();
        final Item item = location.getWorld().dropItemNaturally(location.clone().add(0.5, 0.5, 0.5), result);
        if (inventory.firstEmpty() == -1 && !inventory.contains(result)) {
            try {
                locationOptions.despawn();
                locationOptions.scheduleSpawn();
                setResult(GatherResult.FULL_INVENTORY);
            } catch (Exception e) {
                setResult(GatherResult.UNKNOWN);
                e.printStackTrace();
            }
            return;
        }

        try {
            locationOptions.despawn();
            locationOptions.scheduleSpawn();
            inventory.addItem(result);

            // set the pickup delay of the item on the ground so the player can't duplicate the herbs
            item.setPickupDelay(5000);
            new BukkitRunnable() {

                @Override
                public void run() {
                    item.remove();
                }
            }.runTaskLater(Professions.getInstance(), 20L);

            setResult(GatherResult.SUCCESS);
        } catch (Exception e) {
            setResult(GatherResult.UNKNOWN);
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        // cancel the task
        super.cancel();
        cancelTask();
    }

    /**
     * Cleans up after the task
     */
    private void cancelTask() {
        TASKS.remove(player.getUniqueId());

        if (bossBar == null) return;
        bossBar.removeAll();
        bossBar = null;
    }

    @Override
    public final synchronized BukkitTask runTaskLater(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {

        // if ANY gather task is active on this player, do not run the task
        if (isActive(player)) return null;

        // creates a bossbar with the bossbar options provided
        final BarFlag[] barFlags = bossBarOptions.barFlags;
        final ArrayList<BarFlag> barFlagsList = new ArrayList<>();
        for (final BarFlag barFlag : barFlags) {
            if (barFlag != null) {
                barFlagsList.add(barFlag);
            }
        }
        bossBar = Bukkit.createBossBar(bossBarOptions.title, bossBarOptions.barColor, bossBarOptions.barStyle, barFlagsList.toArray(new BarFlag[0]));

        // check if we use bossbars
        // normally with the settings there should be 2 tasks - one that uses bossbars and one that does not
        // (so we don't call the Settings.isUseBossBar() repeatedly; this is not a big performance issue so i'll leave it at that)
        if (bossBarOptions.useBossBar && Settings.isUseBossBar()) {
            setupBossBar(plugin, delay);
        }
        TASKS.put(player.getUniqueId(), this);
        return super.runTaskLater(plugin, delay);
    }

    /**
     * Sets up the bossbar. The bossbar is not bound to the task, but is pretty much completely synced with it
     *
     * @param plugin the plugin for the runnable
     * @param delay  the delay
     */
    private void setupBossBar(Plugin plugin, long delay) {
        bossBar.setProgress(1);
        bossBar.addPlayer(player);
        final int period = 1;

        // create a runnable for the bossbar
        new BukkitRunnable() {
            double currTime = 0;
            boolean cancel = false;

            @Override
            public void run() {

                // if this timer should be cancelled, cancel this task
                if (cancel) {
                    cancel();
                    return;
                }

                if (currTime > delay) {
                    currTime = delay;
                    cancel = true;
                }

                // no bossbar -> cancel this task
                if (bossBar == null) {
                    cancel();
                    return;
                }
                bossBar.setProgress((delay - currTime) / delay);
                currTime += period;
            }
        }.runTaskTimer(plugin, 0, period);
    }

    /**
     * The possible gather results
     */
    public enum GatherResult {
        /**
         * If the gathering of the block was successful
         */
        SUCCESS,

        /**
         * If the player had full inventory. The item is still dropped beneath the player.
         */
        FULL_INVENTORY,

        /**
         * If the block, that was supposed to be gathered, was no longer there
         */
        LOCATION_AIR,

        /**
         * If the gathering was cancelled by damage
         */
        CANCELLED_BY_DAMAGE,

        /**
         * If the gathering was cancelled by moving too far away
         */
        CANCELLED_BY_MOVE,

        /**
         * If the gathering was cancelled for unknown reasons
         */
        UNKNOWN
    }

    /**
     * The options for a bossbar
     */
    public static class BossBarOptions {

        /**
         * boolean for the API (coder) not the user! <br>
         * this boolean is defined by the person coding this
         */
        public boolean useBossBar = false;

        /**
         * the color of the bossbar
         */
        public BarColor barColor = BarColor.WHITE;

        /**
         * the style of the bossbar
         */
        public BarStyle barStyle = BarStyle.SOLID;

        /**
         * The title of the bossbar
         */
        public String title = "The gathered block";

        /**
         * The bar flags. Initial array length is 0.
         */
        public BarFlag[] barFlags = new BarFlag[0];
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
