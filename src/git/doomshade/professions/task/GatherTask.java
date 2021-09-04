/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jakub Šmrha
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package git.doomshade.professions.task;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.item.object.spawn.ISpawnPoint;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.ExtendedBukkitRunnable;
import git.doomshade.professions.utils.Utils;
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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * A task for gathering profession type purposes
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class GatherTask extends ExtendedBukkitRunnable {

    /**
     * The tasks for event calling. A task is added in {@link #onStart()} and removed in {@link #onCancel()}
     */
    private static final Map<UUID, GatherTask> TASKS = new HashMap<>();
    private static final int PICKUP_DELAY = 5000;

    // let these be protected for further polymorphism purposes
    private final ISpawnPoint spawnPoint;
    private final ItemStack result;
    private final Consumer<GatherResult> endResultAction;
    private final BossBarOptions bossBarOptions;

    // create a reference for the player so the gatherer.getUser().getPlayer() is not called so often
    private final UUID player;
    private final long gatherTime;
    private BossBar bossBar;
    private Predicate<EntityDamageEvent> damageEventPredicate;
    private Predicate<Double> movePredicate;

    /**
     * @param spawnPoint      the location options containing location and spawn methods
     * @param gatherer        the gathering player to call this task upon
     * @param result          the expected result of the gathering task
     * @param endResultAction the action to be performed depending on the gather result
     * @param bossBarOptions  the bossbar options of gathering task
     */
    public GatherTask(ISpawnPoint spawnPoint, UserProfessionData gatherer, ItemStack result,
                      Consumer<GatherResult> endResultAction, BossBarOptions bossBarOptions, long gatherTime) {
        this.spawnPoint = spawnPoint;
        this.result = result;
        this.endResultAction = endResultAction;
        this.bossBarOptions = bossBarOptions;
        this.player = gatherer.getUser().getPlayer().getUniqueId();
        this.gatherTime = gatherTime;
    }

    /**
     * Calls the event for the gatherer's current task if there's one
     * <p>Note that We let the argument be an {@link EntityDamageEvent} so that the predicate has more options</p>
     *
     * @param damageEvent the damage event
     */
    public static void onGathererDamaged(EntityDamageEvent damageEvent) {
        if (damageEvent == null) {
            return;
        }

        // check if the damaged entity was actually a player
        final Entity entity = damageEvent.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player damagedPlayer = (Player) entity;

        GatherTask gatherTask = TASKS.get(damagedPlayer.getUniqueId());

        // check for gather task
        if (gatherTask == null) {
            return;
        }

        // check for damage event
        final Predicate<EntityDamageEvent> predicate = gatherTask.damageEventPredicate;
        if (predicate == null) {
            return;
        }

        // there is a damage event and the gatherer of this task was damaged -> check
        gatherTask.onEvent(predicate, damageEvent, GatherResult.CANCELLED_BY_DAMAGE);
    }

    /**
     * Calls the given predicate, sets the gather tasks result if the predicate returns {@code true} and cancels the
     * event.
     *
     * @param predicate the predicate
     * @param testedArg the tested arg in the predicate
     * @param result    the result if the predicate returns {@code true}
     * @param <T>       the predicate type
     */
    private <T> void onEvent(Predicate<T> predicate, T testedArg, GatherResult result) {
        if (predicate != null && predicate.test(testedArg)) {
            setResult(result);
            cancel();
        }
    }

    /**
     * Calls the result action to be performed once the task is done. If the result is unknown, a message is logged and
     * sent to the gatherer.
     *
     * @param gatherResult the result of this task
     */
    private void setResult(GatherResult gatherResult) {
        if (endResultAction != null && gatherResult != null) {
            endResultAction.accept(gatherResult);
        }
        if (gatherResult == null || gatherResult == GatherResult.UNKNOWN) {
            final String msg = ChatColor.RED +
                    "The block was not gathered for unknown reasons. Contact the admins and try to describe the " +
                    "problem thoroughly.";
            final Player player = Bukkit.getPlayer(this.player);

            if (player != null) {
                player.sendMessage(msg);
            }
            ProfessionLogger.log(msg, Level.CONFIG);
        }
    }

    /**
     * Calls the event for the gatherer's current task if there's one
     *
     * @param movedPlayer the gatherer
     * @param length      the length walked from the gathering point
     */
    public static void onGathererMoved(Player movedPlayer, double length) {

        final GatherTask gatherTask = TASKS.get(movedPlayer.getUniqueId());

        // check for gather task
        if (gatherTask == null) {
            return;
        }

        // check for move event
        final Predicate<Double> predicate = gatherTask.movePredicate;
        if (predicate == null) {
            return;
        }

        // there is a move event and the gatherer of this task moved -> check
        gatherTask.onEvent(predicate, length, GatherResult.CANCELLED_BY_MOVE);
    }

    @Override
    protected final void onStart() {
        // if ANY gather task is active on this player, do not run the task
        final Player player = Bukkit.getPlayer(this.player);
        if (player == null || isActive(player)) {
            cancel();
            return;
        }

        // creates a bossbar with the bossbar options provided
        final BarFlag[] barFlags = bossBarOptions.barFlags;
        bossBar = Bukkit.createBossBar(bossBarOptions.title, bossBarOptions.barColor, bossBarOptions.barStyle,
                Arrays.stream(barFlags).filter(Objects::nonNull).toArray(BarFlag[]::new));

        // check if we use bossbars
        // normally with the settings there should be 2 tasks - one that uses bossbars and one that does not
        // (so we don't call the Settings.isUseBossBar() repeatedly; this is not a big performance issue so i'll
        // leave it at that)
        if (bossBarOptions.useBossBar && Settings.isUseBossBar()) {
            setupBossBar(Professions.getInstance(), delay());
        }
        TASKS.put(this.player, this);
    }

    @Override
    protected final void onCancel() {
        cleanup();
    }

    @Override
    protected final long delay() {
        return gatherTime;
    }

    @Override
    protected final long period() {
        return 0;
    }

    /**
     * Cleans up after the task
     */
    private void cleanup() {
        TASKS.remove(player);

        if (bossBar == null) {
            return;
        }
        bossBar.removeAll();
        bossBar = null;
    }

    public static boolean isActive(Player player) {
        return TASKS.containsKey(player.getUniqueId());
    }

    /**
     * Sets up the bossbar. The bossbar is not bound to the task, but is pretty much completely synced with it
     *
     * @param plugin the plugin for the runnable
     * @param delay  the delay
     */
    private void setupBossBar(Plugin plugin, long delay) {
        final Player player = Bukkit.getPlayer(this.player);
        if (player == null) {
            return;
        }
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
     * Sets the action to be performed when the gatherer is damaged.<br> The predicate should return {@code true} should
     * the event be cancelled, {@code false} otherwise.<br> This allows you to perform an action before the task is
     * cancelled such as cleanup or, for example, check for the damage amount and cancel the event based on that.
     *
     * @param damageEventPredicate the action to be performed
     */
    public void setOnGathererDamaged(Predicate<EntityDamageEvent> damageEventPredicate) {
        this.damageEventPredicate = damageEventPredicate;
    }

    /**
     * Sets the action to be performed when the gatherer moves.<br> The predicate should return {@code true} should the
     * event be cancelled, {@code false} otherwise.
     * <p>Note that the parameter could be a double, but for the API's reasons it shall remain a {@link Predicate}</p>
     *
     * @param movePredicate the acceptable distance
     */
    public void setOnMoved(Predicate<Double> movePredicate) {
        this.movePredicate = movePredicate;
    }

    @Override
    public final void run() {
        cleanup();
        Location location = spawnPoint.getLocation();

        // someone has already gathered the expected block
        // this does not fully prevent the duplication of gathering but should suffice
        if (location.getBlock().getType() == Material.AIR) {
            setResult(GatherResult.LOCATION_AIR);
            return;
        }

        // the player has no empty space in inventory but the task was successful, so drop the item on the ground
        final PlayerInventory inventory = Bukkit.getPlayer(player).getInventory();
        final Item item = Objects.requireNonNull(location.getWorld())
                .dropItemNaturally(location.clone().add(0.5, 0.5, 0.5), result);
        if (inventory.firstEmpty() == -1 && !inventory.contains(result)) {
            try {
                spawnPoint.despawn();
                spawnPoint.scheduleSpawn();
                setResult(GatherResult.FULL_INVENTORY);
            } catch (Exception e) {
                setResult(GatherResult.UNKNOWN);
                ProfessionLogger.logError(e);
            }
            return;
        }

        try {
            ProfessionLogger.log("Gather task success " + this);
            spawnPoint.despawn();
            spawnPoint.scheduleSpawn();
            inventory.addItem(result);

            // set the pickup delay of the item on the ground so the player can't duplicate the herbs
            item.setPickupDelay(PICKUP_DELAY);
            new BukkitRunnable() {

                @Override
                public void run() {
                    item.remove();
                }
            }.runTaskLater(Professions.getInstance(), Utils.TICKS);

            setResult(GatherResult.SUCCESS);
        } catch (Exception e) {
            setResult(GatherResult.UNKNOWN);
            ProfessionLogger.logError(e);
        }
    }

    @Override
    public String toString() {
        return "GatherTask{" +
                "result=" + result +
                ", player=" + player +
                ", gatherTime=" + gatherTime +
                '}';
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
         * boolean for the API (coder) not the user! <br> this boolean is defined by the person coding this
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
}
