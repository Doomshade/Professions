package git.doomshade.professions.listeners;

import git.doomshade.professions.Professions;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.profession.types.crafting.CustomRecipe;
import git.doomshade.professions.profession.types.enchanting.Enchant;
import git.doomshade.professions.profession.types.enchanting.EnchantedItemType;
import git.doomshade.professions.profession.types.enchanting.PreEnchantedItem;
import git.doomshade.professions.profession.types.gathering.GatherItem;
import git.doomshade.professions.profession.types.hunting.Mob;
import git.doomshade.professions.profession.types.hunting.Prey;
import git.doomshade.professions.profession.types.mining.Ore;
import git.doomshade.professions.profession.types.mining.OreItemType;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftShapedRecipe;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class ProfessionListener extends AbstractProfessionListener {
    private static final HashMap<UUID, Enchant> ENCHANTS = new HashMap<>();

    private static boolean areSimilar(ItemStack one, ItemStack two) {
        if (one == null || two == null) {
            return false;
        }

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
    @EventHandler
    public void onMine(BlockBreakEvent e) {
        if (e.isCancelled()) {
            return;
        }


        OreItemType ott;
        try {
            final Material type = e.getBlock().getType();
            ott = Utils.findInIterable(Professions.getItemTypeHolder(OreItemType.class), x ->
                    x.getObject().getOreMaterial() == type
            );
        } catch (Utils.SearchNotFoundException ex) {
            return;
        }

        Ore ore = ott.getObject();
        ProfessionEvent<OreItemType> event = getEvent(e.getPlayer(), ore, OreItemType.class);
        if (event == null) {
            return;
        }
        event.addExtra(e.getBlock().getLocation().add(new Vector(0.5, 0, 0.5)));
        callEvent(event);
        if (event.isCancelled()) {
            e.setCancelled(true);
        } else {
            e.getBlock().setType(Material.AIR);
        }
    }

    @Override
    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (e.isCancelled()) {
            return;
        }

        HumanEntity he = e.getWhoClicked();
        ItemStack cursor = e.getCursor();

        Recipe recipe = e.getRecipe();

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
    @EventHandler
    public void onGather(PlayerInteractEvent e) {
        // TODO Auto-generated method stub
        if (e.isCancelled() || e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        callEvent(e.getPlayer(), e.getItem(), GatherItem.class);
    }

    @Override
    @EventHandler
    public void onEnchant(PlayerInteractEvent e) {
        if (e.isCancelled()) {
            return;
        }

        Player hrac = e.getPlayer();

        ItemStack mh = hrac.getInventory().getItemInMainHand();
        if (mh == null) {
            return;
        }

        if (ENCHANTS.containsKey(hrac.getUniqueId())) {
            Enchant enchant = ENCHANTS.remove(hrac.getUniqueId());
            ProfessionEvent<EnchantedItemType> event = callEvent(hrac, enchant, EnchantedItemType.class,
                    new PreEnchantedItem(enchant, mh));
            if (!event.isCancelled()) {
                // don't delete the item!
            }
            return;
        }

        for (EnchantedItemType enchItemType : Professions.getItemTypeHolder(EnchantedItemType.class)
                .getRegisteredItemTypes()) {
            Enchant eit = enchItemType.getObject();
            if (areSimilar(eit.getItem(), mh)) {
                ENCHANTS.put(hrac.getUniqueId(), eit);
                break;
            }
        }
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
