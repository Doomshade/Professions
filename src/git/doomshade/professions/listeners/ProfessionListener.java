package git.doomshade.professions.listeners;

import git.doomshade.professions.Professions;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.crafting.CustomRecipe;
import git.doomshade.professions.profession.types.crafting.alchemy.Potion;
import git.doomshade.professions.profession.types.crafting.alchemy.PotionItemType;
import git.doomshade.professions.profession.types.crafting.jewelcrafting.Gem;
import git.doomshade.professions.profession.types.enchanting.Enchant;
import git.doomshade.professions.profession.types.enchanting.EnchantedItemItemType;
import git.doomshade.professions.profession.types.enchanting.PreEnchantedItem;
import git.doomshade.professions.profession.types.gathering.herbalism.Herb;
import git.doomshade.professions.profession.types.gathering.herbalism.HerbItemType;
import git.doomshade.professions.profession.types.gathering.herbalism.HerbLocationOptions;
import git.doomshade.professions.profession.types.hunting.Mob;
import git.doomshade.professions.profession.types.hunting.Prey;
import git.doomshade.professions.profession.types.mining.Ore;
import git.doomshade.professions.profession.types.mining.OreItemType;
import git.doomshade.professions.profession.types.mining.spawn.OreLocationOptions;
import git.doomshade.professions.profession.types.utils.SpawnPoint;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
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
 * A listener that calls {@link ProfessionEvent} based on the event called. Called events by this class are further handled in {@link git.doomshade.professions.Profession#onEvent(ProfessionEvent)}.
 *
 * @author Doomshade
 */
public class ProfessionListener extends AbstractProfessionListener {
    private static final HashMap<UUID, Enchant> ENCHANTS = new HashMap<>();

    /**
     * Determines whether the two items are similar based on display name and lore
     *
     * @param one
     * @param two
     * @return
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
            lore = oneMeta.getLore().containsAll(twoMeta.getLore()) && twoMeta.getLore().containsAll(oneMeta.getLore());
        }

        return displayName && lore;
    }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void onMine(BlockBreakEvent e) {

        final Ore ore;
        final Block block = e.getBlock();
        final Location location = block.getLocation();
        try {
            ore = Utils.findInIterable(Ore.ORES.values(), x -> x != null && x.isSpawnPoint(location));
        } catch (Utils.SearchNotFoundException ex) {
            return;
        }

        final Player player = e.getPlayer();
        ProfessionEvent<OreItemType> event = getEvent(player, ore, OreItemType.class);
        if (event == null) {
            return;
        }
        event.addExtra(location);
        callEvent(event);

        // event is cancelled when the player does not meet requirements
        // destroy the block if the requirements are met
        if (event.isCancelled()) {
            e.setCancelled(true);
        } else {
            final OreLocationOptions locationOptions = ore.getLocationOptions(location);
            locationOptions.despawn();

            if (!(Permissions.has(player, Permissions.BUILDER) && player.getGameMode() == GameMode.CREATIVE)) {
                locationOptions.scheduleSpawn();
            }
        }
    }

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

    @Override
    @EventHandler(ignoreCancelled = true)
    public void onGather(PlayerInteractEvent e) {

        // Gathering blocks by right clicking it
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Herb herb;
        try {

            // Checks whether or not the right clicked block is a herb
            herb = Herb.getHerb(e.getClickedBlock().getType(), e.getClickedBlock().getLocation());
        } catch (Utils.SearchNotFoundException ex) {
            return;
        }

        // The block is a herb -> call event with the location
        ProfessionEvent<HerbItemType> event = getEvent(e.getPlayer(), herb, HerbItemType.class);
        if (event == null) {
            return;
        }
        event.addExtra(e.getClickedBlock().getLocation());
        callEvent(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHerbDestroy(BlockBreakEvent e) {
        // For admins due to possible breaking of the herb - need to remove particles from the location

        try {
            final Block block = e.getBlock();
            final Location location = block.getLocation();
            Herb herb = Herb.getHerb(block.getType(), location);

            final HerbLocationOptions herbLocationOptions = herb.getHerbLocationOptions(location);
            herbLocationOptions.despawn();
            final List<SpawnPoint> spawnPoints = herb.getSpawnPoints();
            String message = String.format("You have destroyed a %s%s herb.", herb.getId(), ChatColor.RESET);
            final Player player = e.getPlayer();
            for (int i = 0; i < spawnPoints.size(); i++) {
                if (spawnPoints.get(i).location.equals(location)) {
                    player.sendMessage(message.concat(String.format(" Spawn location ID: %d.", i)));
                    return;
                }
            }
            player.sendMessage(message);
        } catch (Utils.SearchNotFoundException ignored) {
        }

    }

    @EventHandler
    public void onWorldLoad(WorldInitEvent e) {

        // does not work
        Herb.spawnHerbs(e.getWorld());
    }

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

    @EventHandler(ignoreCancelled = true)
    public void onPotionDrink(PlayerItemConsumeEvent e) {
        Potion potion = Potion.getItem(e.getItem());
        if (potion != null) {

            final User user = User.getUser(e.getPlayer());
            if (!user.isActivePotion(potion)) {
                user.applyPotion(potion);
            } else {
                user.sendMessage(new Messages.MessageBuilder(Messages.Message.POTION_ALREADY_ACTIVE).setPlayer(user).setItemType(new PotionItemType(potion, 50)).build());
                e.setCancelled(true);
            }
        }
    }

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
    public void onJoinE(PlayerJoinEvent e) {
        updateLater(e.getPlayer());
    }

    @EventHandler
    public void onLeaveE(PlayerQuitEvent e) {
        Gem.unApplyAll(e.getPlayer());
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

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        //Potion.loadFromCache(e.getPlayer());
    }

    @Override
    @EventHandler
    public void onKill(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        if (entity == null || entity.getKiller() == null) {
            return;
        }
        callEvent(entity.getKiller(), new Mob(entity.getType()), Prey.class);
    }

}
