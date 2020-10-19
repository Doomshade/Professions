package git.doomshade.professions.listeners;

import git.doomshade.professions.Professions;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.profession.Profession;
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
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.utils.LocationOptions;
import git.doomshade.professions.profession.utils.MarkableLocationOptions;
import git.doomshade.professions.profession.utils.SpawnPoint;
import git.doomshade.professions.profession.utils.SpawnableElement;
import git.doomshade.professions.task.GatherTask;
import git.doomshade.professions.user.User;
import git.doomshade.professions.utils.Permissions;
import git.doomshade.professions.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftShapedRecipe;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * A listener that calls {@link ProfessionEvent} based on the event called. Called events by this class are further handled in {@link Profession#onEvent(ProfessionEventWrapper)}.
 * <p>Note that this will most likely be separated for each profession later on if this class gets too big</p>
 *
 * @author Doomshade
 * @version 1.0
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
     * Determines whether the two items are similar based on display name and lore
     *
     * @param one the first one
     * @param two the second one
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

        if (oneMeta.hasDisplayName() && twoMeta.hasDisplayName()) {
            displayName = oneMeta.getDisplayName().equals(twoMeta.getDisplayName());
        }

        if (oneMeta.hasLore() && twoMeta.hasLore()) {
            final List<String> lore1 = oneMeta.getLore();
            final List<String> lore2 = twoMeta.getLore();

            lore = lore1.size() == lore2.size() && lore1.equals(lore2);
        }

        return displayName && lore;
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
            ore = SpawnableElement.of(block, Ore.class);
        } catch (Utils.SearchNotFoundException ex) {

            // no, if the world is a mining world AND also if the plugin should handle the event, cancel the event if the player is not ranked >=builder
            final boolean world = Settings.getMiningWorlds().contains(location.getWorld().getName().toLowerCase());

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
        callEvent(event);

        // event is cancelled when the player does not meet requirements
        if (event.isCancelled()) {
            e.setCancelled(true);
        }
        // destroy the block and disable particles if the requirements are met
        else {
            final LocationOptions locationOptions = ore.getLocationOptions(location);
            locationOptions.despawn();

            // do not schedule spawn if the player is ranked >=builder AND is in creative mode, schedule otherwise
            if (!Permissions.has(player, Permissions.BUILDER) && player.getGameMode() != GameMode.CREATIVE) {
                locationOptions.scheduleSpawn();
            }
        }
    }

    /**
     * Gets the maximum amount of items that can be crafted
     *
     * @param result the result to check for
     * @param hrac   the player to check the inventory for
     * @param e      the event to check in
     * @return ItemStack amount * Math#min(possible craftings, possible creations)
     */
    private int getAmountOfItems(ItemStack result, Player hrac, CraftItemEvent e) {
        int possibleCraftings = 1;
        int possibleCreations = 0;
        if (e.isShiftClick()) {
            int itemsChecked = 0;
            for (ItemStack item : e.getInventory().getMatrix()) {
                if (item != null && !item.getType().equals(Material.AIR)) {
                    if (itemsChecked == 0)
                        possibleCraftings = item.getAmount();
                    else
                        possibleCraftings = Math.min(possibleCraftings, item.getAmount());
                    itemsChecked++;
                }
            }
        }
        for (ItemStack itemm : hrac.getInventory().getStorageContents()) {
            if (itemm == null) {
                possibleCreations += result.getMaxStackSize();
                continue;
            }
            if (itemm.isSimilar(result)) {
                possibleCreations += result.getMaxStackSize() - itemm.getAmount();
            }
        }
        return result.getAmount() * Math.min(possibleCraftings, possibleCreations);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Herbalism
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
        if (cursor.getType() != Material.AIR && !recipe.getResult().isSimilar(cursor) && !e.isShiftClick()) {
            return;
        }

        if (!(he instanceof Player)) {
            return;
        }
        Player hrac = (Player) he;

        if (recipe instanceof CraftShapedRecipe) {

            // get amount of items that are crafted (it won't call that amount of events, we
            // have to handle it)
            int amountOfItems = getAmountOfItems(recipe.getResult(), hrac, e);

            // get the event to modify it before calling it
            ProfessionEvent<CustomRecipe> event = getEvent(hrac, ((CraftShapedRecipe) recipe), CustomRecipe.class);
            if (event == null) {
                return;
            }

            // set result exp * amount of items crafted
            event.setExp(event.getExp() * amountOfItems);

            // call event afterwards
            callEvent(event);

            // check if event was cancelled, cancel the craft event if so
            e.setCancelled(event.isCancelled());

        }
    }

    /**
     * Spawnable destroy event for admins due to possible breaking of the herb - need to remove particles from the location
     *
     * @param e the block break event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onSpawnableDestroy(BlockBreakEvent e) {

        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) return;

        try {
            final Block block = e.getBlock();
            final Location location = block.getLocation();

            SpawnableElement<? extends LocationOptions> spawnableElement = SpawnableElement.of(block);

            final LocationOptions locationOptions = spawnableElement.getLocationOptions(location);

            // log
            Professions.log(locationOptions);

            final List<SpawnPoint> spawnPoints = spawnableElement.getSpawnPoints();

            // log
            Professions.log(spawnPoints.contains(new SpawnPoint(location)));
            String message = String.format("%sYou have destroyed %s%s (id = %s).", ChatColor.GRAY, spawnableElement.getName(), ChatColor.GRAY, spawnableElement.getId());

            final Player player = e.getPlayer();
            try {
                // MUST BE new SpawnPoint BECAUSE OF EQUALS!!!!
                final SpawnPoint sp = Utils.findInIterable(spawnPoints, x -> x.equals(new SpawnPoint(location)));
                player.sendMessage(message.concat(" Removed spawn point."));
                spawnableElement.removeSpawnPoint(sp);
            } catch (Utils.SearchNotFoundException ignored) {
                player.sendMessage(message);
            } finally {
                e.setCancelled(true);
                if (locationOptions instanceof MarkableLocationOptions) {
                    ((MarkableLocationOptions) locationOptions).despawn(true);
                } else {
                    locationOptions.despawn();
                }
            }
        } catch (Utils.SearchNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Spawns herbs in the world during initialization
     *
     * @param e the world init event
     */
    @EventHandler
    public void onWorldLoad(WorldInitEvent e) {
        Herb.spawnHerbs(e.getWorld());
    }

    /**
     * Calls an event to gather task to further handle. The damage event is handled inside gather task, so no need to do anything but call it with the right parameter
     *
     * @param event the damage event
     */
    @EventHandler(ignoreCancelled = true)
    public void onGathererDamaged(EntityDamageEvent event) {
        GatherTask.onGathererDamaged(event);
    }

    /**
     * Calls an even to gather task to further handle. The move event is handled inside gather task.
     *
     * @param event the move event
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerMoveFromHerb(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        // the player has no task active, do not add to MOVE_LEN hashmap for performance reasons and remove if there was a player move
        if (!GatherTask.isActive(player)) {
            MOVE_LEN.remove(uuid);
            return;
        }

        final PlayerMove playerMove = MOVE_LEN.get(uuid);

        // we call Map#put in the onGather method
        // this should not happen just to make sure no errors are thrown
        if (playerMove == null) return;

        final double length = playerMove.computeLength();
        GatherTask.onGathererMoved(player, length);
        MOVE_LEN.put(uuid, playerMove);
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

        Herb herb;
        final Block block = e.getClickedBlock();
        try {

            // Checks whether or not the right clicked block is a herb
            herb = SpawnableElement.of(block, Herb.class);
        } catch (Utils.SearchNotFoundException ex) {
            return;
        }

        // The block is a herb -> call event with the location
        final Player player = e.getPlayer();
        ProfessionEvent<HerbItemType> event = getEvent(player, herb, HerbItemType.class);
        if (event == null) {
            return;
        }
        event.addExtra(block.getLocation());
        callEvent(event);
        MOVE_LEN.put(player.getUniqueId(), new PlayerMove(player, block.getLocation()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionDrink(PlayerItemConsumeEvent e) {
        Potion potion = Potion.getItem(e.getItem());
        if (potion != null) {

            final User user = User.getUser(e.getPlayer());
            if (!user.isActivePotion(potion)) {
                user.applyPotion(potion);
            } else {
                user.sendMessage(new Messages.MessageBuilder(Messages.AlchemyMessages.POTION_ALREADY_ACTIVE).setPlayer(user).setItemType(ItemType.getExampleItemType(PotionItemType.class, potion)).build());
                e.setCancelled(true);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Enchanting
    ///////////////////////////////////////////////////////////////////////////

    @Override
    @EventHandler(ignoreCancelled = true)
    public void onEnchant(PlayerInteractEvent e) {

        Player hrac = e.getPlayer();

        ItemStack mh = hrac.getInventory().getItemInMainHand();
        if (mh == null) {
            return;
        }

        if (ENCHANTS.containsKey(hrac.getUniqueId())) {
            Enchant enchant = ENCHANTS.remove(hrac.getUniqueId());
            ProfessionEvent<EnchantedItemItemType> event = callEvent(hrac, enchant, EnchantedItemItemType.class,
                    new PreEnchantedItem(enchant, mh));
            if (event != null && !event.isCancelled()) {
                // don't delete the item!
            }
            return;
        }

        for (EnchantedItemItemType enchItemType : Professions.getProfessionManager().getItemTypeHolder(EnchantedItemItemType.class)
                .getRegisteredItemTypes()) {
            Enchant eit = enchItemType.getObject();
            if (eit != null && areSimilar(eit.getItem(), mh)) {
                ENCHANTS.put(hrac.getUniqueId(), eit);
                break;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Alchemy
    ///////////////////////////////////////////////////////////////////////////

    @Override
    @EventHandler
    public void onKill(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        if (entity == null || entity.getKiller() == null) {
            return;
        }
        callEvent(entity.getKiller(), new Mob(entity.getType()), PreyItemType.class);
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

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        updateLater((Player) e.getWhoClicked());
    }

    @EventHandler
    public void onHotbarScroll(PlayerItemHeldEvent e) {
        updateLater(e.getPlayer());
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        updateLater(e.getPlayer());
    }

    @EventHandler
    public void onThrow(PlayerDropItemEvent e) {
        update(e.getPlayer());
    }

    private void update(Player player) {
        Gem.update(player);
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
