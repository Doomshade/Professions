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

package git.doomshade.professions.listeners;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.item.ext.ItemType;
import git.doomshade.professions.api.spawn.ISpawnPoint;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.exceptions.SpawnException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.profession.professions.alchemy.Potion;
import git.doomshade.professions.profession.professions.alchemy.PotionItemType;
import git.doomshade.professions.profession.professions.crafting.CustomRecipe;
import git.doomshade.professions.profession.professions.enchanting.Enchant;
import git.doomshade.professions.profession.professions.enchanting.EnchantedItemItemType;
import git.doomshade.professions.profession.professions.enchanting.PreEnchantedItem;
import git.doomshade.professions.profession.professions.herbalism.Herb;
import git.doomshade.professions.profession.professions.herbalism.HerbItemType;
import git.doomshade.professions.profession.professions.jewelcrafting.Gem;
import git.doomshade.professions.profession.professions.mining.Ore;
import git.doomshade.professions.profession.professions.mining.OreItemType;
import git.doomshade.professions.profession.professions.skinning.Mob;
import git.doomshade.professions.profession.professions.skinning.PreyItemType;
import git.doomshade.professions.api.spawn.ext.Spawnable;
import git.doomshade.professions.task.GatherTask;
import git.doomshade.professions.user.User;
import git.doomshade.professions.utils.Permissions;
import git.doomshade.professions.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * A listener that calls {@link ProfessionEvent} based on the event called. Called events by this class are further
 * handled in {@link Profession#onEvent(ProfessionEventWrapper)}.
 * <p>Note that this will most likely be separated for each profession later on if this class gets too big</p>
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class ProfessionListener extends AbstractProfessionListener {

    /**
     * Hashmap for enchants
     */
    private static final HashMap<UUID, Enchant> ENCHANTS = new HashMap<>();
    private static final HashMap<UUID, PlayerMove> MOVE_LEN = new HashMap<>();

    ///////////////////////////////////////////////////////////////////////////
    // Mining
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The crafitng event for future crafting professions, currently creating {@link CustomRecipe}
     *
     * @param e the craft item event
     */
    @Override
    @EventHandler(ignoreCancelled = true)
    public void onCraft(CraftItemEvent e) {

        HumanEntity he = e.getWhoClicked();
        ItemStack cursor = e.getCursor();

        Recipe recipe = e.getRecipe();

        // some fuckery, basically makes sure the crafting goes well
        if (Objects.requireNonNull(cursor).getType() != Material.AIR && !recipe.getResult().isSimilar(cursor) &&
                !e.isShiftClick()) {
            return;
        }

        if (!(he instanceof Player)) {
            return;
        }
        Player hrac = (Player) he;

        if (recipe instanceof ShapedRecipe) {

            // get amount of items that are crafted (it won't call that amount of events, we
            // have to handle it)
            int amountOfItems = getAmountOfItems(recipe.getResult(), hrac, e);

            // get the event to modify it before calling it
            /*ProfessionEvent<CustomRecipe> event = getEvent(hrac, ((ShapedRecipe) recipe), CustomRecipe.class);
            if (event == null) {
                return;
            }

            // set result exp * amount of items crafted
            event.setExp(event.getExp() * amountOfItems);

            // call event afterwards
            callEvent(event);

            // check if event was cancelled, cancel the craft event if so
            e.setCancelled(event.isCancelled());*/

        }
    }

    /**
     * Gets the maximum amount of items that can be crafted
     *
     * @param result the result to check for
     * @param hrac   the player to check the inventory for
     * @param e      the event to check in
     *
     * @return ItemStack amount * Math#min(possible craftings, possible creations)
     */
    private int getAmountOfItems(ItemStack result, Player hrac, CraftItemEvent e) {
        int possibleCraftings = 1;
        int possibleCreations = 0;
        if (e.isShiftClick()) {
            int itemsChecked = 0;
            for (ItemStack item : e.getInventory().getMatrix()) {
                if (!item.getType().equals(Material.AIR)) {
                    if (itemsChecked == 0) {
                        possibleCraftings = item.getAmount();
                    } else {
                        possibleCraftings = Math.min(possibleCraftings, item.getAmount());
                    }
                    itemsChecked++;
                }
            }
        }
        for (ItemStack itemm : hrac.getInventory().getStorageContents()) {
            if (itemm.isSimilar(result)) {
                possibleCreations += result.getMaxStackSize() - itemm.getAmount();
            }
        }
        return result.getAmount() * Math.min(possibleCraftings, possibleCreations);
    }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void onEnchant(PlayerInteractEvent e) {

        Player player = e.getPlayer();

        ItemStack mh = player.getInventory().getItemInMainHand();

        if (ENCHANTS.containsKey(player.getUniqueId())) {
            Enchant enchant = ENCHANTS.remove(player.getUniqueId());
            ProfessionEvent<EnchantedItemItemType> event = callEvent(player, enchant, EnchantedItemItemType.class,
                    new PreEnchantedItem(enchant, mh));
            if (event != null && !event.isCancelled()) {
                // don't delete the item!
            }
            return;
        }

        for (EnchantedItemItemType enchItemType : Professions.getProfMan()
                .getItemTypeHolder(EnchantedItemItemType.class)) {
            Enchant eit = enchItemType.getObject();
            if (eit != null && areSimilar(eit.getItem(), mh)) {
                ENCHANTS.put(player.getUniqueId(), eit);
                break;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Herbalism
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Determines whether the two items are similar based on display name and lore
     *
     * @param one the first one
     * @param two the second one
     *
     * @return {@code true} if they are similar
     */
    private static boolean areSimilar(ItemStack one, ItemStack two) {

        // either one of them null -> don't compare
        if (one == null || two == null) {
            return false;
        }

        // both no metas -> return the base ItemStack #isSimilar method
        if (!one.hasItemMeta() && !two.hasItemMeta()) {
            return one.isSimilar(two);
        }
        ItemMeta oneMeta = one.getItemMeta();
        ItemMeta twoMeta = two.getItemMeta();

        boolean displayName = true;
        boolean lore = true;

        if (Objects.requireNonNull(oneMeta).hasDisplayName() && Objects.requireNonNull(twoMeta).hasDisplayName()) {
            displayName = oneMeta.getDisplayName().equals(twoMeta.getDisplayName());
        }

        if (oneMeta.hasLore() && Objects.requireNonNull(twoMeta).hasLore()) {
            final List<String> lore1 = oneMeta.getLore();
            final List<String> lore2 = twoMeta.getLore();

            lore = Objects.requireNonNull(lore1).size() == Objects.requireNonNull(lore2).size() && lore1.equals(lore2);
        }

        return displayName && lore;
    }

    /**
     * The gathering event for {@link git.doomshade.professions.profession.professions.herbalism.HerbalismProfession}
     *
     * @param e the gather event
     */
    @Override
    @EventHandler(ignoreCancelled = true)
    public void onGather(PlayerInteractEvent e) {

        // Gathering blocks by right clicking it
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        final Herb herb;
        final Block block = e.getClickedBlock();
        try {

            // Checks whether or not the right clicked block is a herb
            herb = Spawnable.of(block, Herb.class);
        } catch (Utils.SearchNotFoundException ex) {
            return;
        }

        // The block is a herb -> call event with the location
        final Player player = e.getPlayer();
        ProfessionEvent<HerbItemType> event = getEvent(player, herb, HerbItemType.class);
        if (event == null) {
            return;
        }
        event.addExtra(Objects.requireNonNull(block).getLocation());
        ProfessionLogger.log("Calling gather event...");
        callEvent(event);
        MOVE_LEN.put(player.getUniqueId(), new PlayerMove(player, block.getLocation()));
    }

    @Override
    @EventHandler
    public void onKill(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        if (entity.getKiller() == null) {
            return;
        }
        callEvent(entity.getKiller(), new Mob(entity.getType()), PreyItemType.class);
    }

    /**
     * Mine event for {@link git.doomshade.professions.profession.professions.mining.MiningProfession}
     *
     * @param e the block break event
     */
    @Override
    @EventHandler(ignoreCancelled = true)
    public void onMine(BlockBreakEvent e) {

        final Block block = e.getBlock();
        final Location location = block.getLocation();

        final Ore ore;
        final Player player = e.getPlayer();
        // is the destroyed block an ore?
        try {

            // yes, continue
            ore = Spawnable.of(block, Ore.class);
        } catch (Utils.SearchNotFoundException ex) {

            // no, if the world is a mining world AND also if the plugin should handle the event, cancel the event if
            // the player is not ranked >=builder
            final boolean world = Settings.getMiningWorlds().contains(
                    Objects.requireNonNull(location.getWorld()).getName().toLowerCase());

            final boolean permission = Permissions.has(e.getPlayer(), Permissions.BUILDER);
            final boolean handleEvents = Settings.isHandleMineEvents();

            if (world && !permission && handleEvents) {

                // keep the e.setCancelled(true) because we ONLY want to cancel the event if the requirements are met
                e.setCancelled(true);
            }
            return;
        }

        // ore found, let's call the event and let the profession handle it
        ProfessionEvent<OreItemType> event = getEvent(player, ore, OreItemType.class);
        if (event == null) {
            return;
        }

        // add the location of ore because of its spawn point
        event.addExtra(location);

        ProfessionLogger.log("Called event");
        callEvent(event);
        // event is cancelled when the player does not meet requirements
        if (event.isCancelled()) {
            ProfessionLogger.log("Cancelling event");
            e.setCancelled(true);
        }
        // destroy the block and disable particles if the requirements are met
        else {
            final ISpawnPoint spawnPoint = ore.getSpawnPoint(location);
            ProfessionLogger.log("Despawning... " + ore.getName() + " with location " + location);
            spawnPoint.despawn();

            // do not schedule spawn if the player is ranked >=builder AND is in creative mode, schedule otherwise
            if (player.getGameMode() != GameMode.CREATIVE) {
                try {
                    spawnPoint.scheduleSpawn();
                } catch (SpawnException e1) {
                    ProfessionLogger.logError(e1);
                }
            }
        }
    }

    /**
     * Spawnable destroy event for admins due to possible breaking of the herb - need to remove particles from the
     * location
     *
     * @param e the block break event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onSpawnableDestroy(BlockBreakEvent e) {

        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            return;
        }

        try {
            final Block block = e.getBlock();
            final Location location = block.getLocation();

            Spawnable spawnableElement = Spawnable.of(block);

            final ISpawnPoint spawnPoint = spawnableElement.getSpawnPoint(location);

            // log
            ProfessionLogger.log(spawnPoint);

            final Collection<ISpawnPoint> spawnPointLocations = spawnableElement.getSpawnPoints();

            // log
            ProfessionLogger.log(spawnPointLocations.contains(spawnPoint));
            String message =
                    String.format("%sYou have destroyed %s%s (id = %s).", ChatColor.GRAY, spawnableElement.getName(),
                            ChatColor.GRAY, spawnableElement.getId());

            final Player player = e.getPlayer();
            try {
                // MUST BE new SpawnPoint BECAUSE OF EQUALS!!!!
                final ISpawnPoint sp = Utils.findInIterable(spawnPointLocations, x -> x.equals(spawnPoint));
                player.sendMessage(message.concat(" Removed spawn point."));
                spawnableElement.removeSpawnPoint(sp);
            } catch (Utils.SearchNotFoundException ignored) {
                player.sendMessage(message);
            } finally {
                e.setCancelled(true);
                spawnPoint.despawn();
            }
        } catch (Utils.SearchNotFoundException ex) {
            //Professions.logError(ex);
        }
    }

    /**
     * Spawns herbs in the world during initialization
     *
     * @param e the world init event
     */
    @EventHandler
    public void onWorldLoad(WorldInitEvent e) {
        Spawnable.scheduleSpawnAll(x -> Objects.equals(x.getLocation().getWorld(), e.getWorld()));
        Herb.spawnHerbs(e.getWorld());
    }

    /**
     * Calls an event to gather task to further handle. The damage event is handled inside gather task, so no need to do
     * anything but call it with the right parameter
     *
     * @param event the damage event
     */
    @EventHandler(ignoreCancelled = true)
    public void onGathererDamaged(EntityDamageEvent event) {
        GatherTask.onGathererDamaged(event);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Enchanting
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Calls an even to gather task to further handle. The move event is handled inside gather task.
     *
     * @param event the move event
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerMoveFromHerb(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        // the player has no task active, do not add to MOVE_LEN hashmap for performance reasons and remove if there
        // was a player move
        if (!GatherTask.isActive(player)) {
            MOVE_LEN.remove(uuid);
            return;
        }

        final PlayerMove playerMove = MOVE_LEN.get(uuid);

        // we call Map#put in the onGather method
        // this should not happen just to make sure no errors are thrown
        if (playerMove == null) {
            return;
        }

        final double length = playerMove.computeLength();
        GatherTask.onGathererMoved(player, length);
        MOVE_LEN.put(uuid, playerMove);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Alchemy
    ///////////////////////////////////////////////////////////////////////////

    @EventHandler(ignoreCancelled = true)
    public void onPotionDrink(PlayerItemConsumeEvent e) {
        Potion potion = Potion.getItem(e.getItem());
        if (potion != null) {

            final User user = User.getUser(e.getPlayer());
            if (!user.isActivePotion(potion)) {
                user.applyPotion(potion);
            } else {
                user.sendMessage(new Messages.MessageBuilder(
                        Messages.AlchemyMessages.POTION_ALREADY_ACTIVE)
                        .player(user)
                        .itemType(ItemType.getExampleItemType(PotionItemType.class, potion))
                        .build()
                );
                e.setCancelled(true);
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Gem updates
    ///////////////////////////////////////////////////////////////////////////

    @EventHandler(ignoreCancelled = true)
    public void onGemInsert(PlayerInteractEvent e) {

        /*final ItemStack item = e.getItem();
        Optional<Gem> optGem = Gem.getGem(item);

        if (optGem.isPresent()) {
            Gem gem = optGem.get();
            GetSet<ItemStack> gs = new GetSet<>(item);

            if (gem.insert(gs, true)) {
                e.getPlayer().getInventory().setItemInMainHand(gs.t);
            }
        }
         */
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) {
            return;
        }
        update((Player) e.getPlayer());
    }

    private void update(Player player) {
        Gem.update(player);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        updateLater((Player) e.getWhoClicked());
    }

    private void updateLater(Player player) {
        new BukkitRunnable() {

            @Override
            public void run() {
                update(player);
            }
        }.runTaskLater(Professions.getInstance(), 1L);
    }

    @EventHandler
    public void onHotbarScroll(PlayerItemHeldEvent e) {
        updateLater(e.getPlayer());
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player) {
            updateLater((Player) e.getEntity());
        }
    }

    @EventHandler
    public void onThrow(PlayerDropItemEvent e) {
        update(e.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        //Potion.cache(e.getPlayer());

        // unapplies gem effects
        Gem.unApplyAll(e.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        //Potion.loadFromCache(e.getPlayer());

        // updates gems
        updateLater(e.getPlayer());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Skinning
    ///////////////////////////////////////////////////////////////////////////

    private static class PlayerMove {
        private final Player movingPlayer;
        private final Location start;

        private PlayerMove(Player movingPlayer, Location herbLocation) {
            this.movingPlayer = movingPlayer;
            this.start = herbLocation;
        }

        private double computeLength() {
            return start.distance(movingPlayer.getLocation());
        }
    }

}
