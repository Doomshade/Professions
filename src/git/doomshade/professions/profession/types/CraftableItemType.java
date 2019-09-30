package git.doomshade.professions.profession.types;

import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.Requirements;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CraftableItemType<T> extends ItemType<T> {
    private Requirements craftingRequirements = new Requirements(new ArrayList<ItemStack>());
    private Requirements inventoryRequirements = new Requirements(new ArrayList<ItemStack>());
    private ItemStack result = new ItemStack(Material.GLASS);
    private double craftingTime = 5d;

    {
        ItemMeta meta = result.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Result item name");
        meta.setLore(Arrays.asList(ChatColor.RED + "Result", ChatColor.BLUE + "item", ChatColor.AQUA + "lore"));
        result.setItemMeta(meta);
    }

    /**
     * {@inheritDoc}
     */
    protected CraftableItemType(Map<String, Object> map, int id) {
        super(map, id);
        MemorySection itemReqSection = (MemorySection) map.get(Key.ITEM_REQUIREMENTS.toString());
        if (itemReqSection != null)
            this.craftingRequirements = Requirements.deserialize(itemReqSection.getValues(false));

        MemorySection invReqSection = (MemorySection) map.get(Key.ITEM_REQUIREMENTS.toString());
        if (invReqSection != null)
            this.inventoryRequirements = Requirements.deserialize(invReqSection.getValues(false));

        this.result = ItemStack.deserialize(((MemorySection) map.get(Key.RESULT.toString())).getValues(true));
        this.craftingTime = (double) map.get(Key.CRAFTING_TIME.toString());

    }

    /**
     * {@inheritDoc}
     */
    protected CraftableItemType(T t, int exp) {
        super(t, exp);
    }

    /**
     * {@inheritDoc}
     */
    protected CraftableItemType() {
        super();
    }

    public final Requirements getInventoryRequirements() {
        return inventoryRequirements;
    }

    public final Requirements getCraftingRequirements() {
        return craftingRequirements;
    }

    public final void addCraftRequirement(ItemStack item) {
        craftingRequirements.addRequirement(item);
    }

    public final void addInventoryRequirement(ItemStack item) {
        inventoryRequirements.addRequirement(item);
    }

    public final boolean meetsInventoryRequirements(Player player) {
        return inventoryRequirements.meetsCraftingRequirements(player);
    }

    public final boolean meetsCraftingRequirements(Player player) {
        return craftingRequirements.meetsCraftingRequirements(player);
    }

    public final void removeCraftingRequirements(Player player) {
        craftingRequirements.removeRequiredItems(player);
    }

    public final void removeInventoryRequirements(Player player) {
        inventoryRequirements.removeRequiredItems(player);
    }

    @Override
    public ItemStack getIcon(UserProfessionData upd) {
        // TODO Auto-generated method stub
        ItemStack icon = super.getIcon(upd);
        if (!icon.hasItemMeta()) {
            return icon;
        }
        ItemMeta meta = icon.getItemMeta();
        if (!meta.hasLore()) {
            return icon;
        }
        List<String> lore = meta.getLore();

        for (Key key : Arrays.asList(Key.ITEM_REQUIREMENTS, Key.INVENTORY_REQUIREMENTS)) {
            Pattern regex = Pattern.compile("\\{" + key + "\\}");
            for (int i = 0; i < lore.size(); i++) {
                String s = lore.get(i);
                Matcher m = regex.matcher(s);
                if (!m.find()) {
                    continue;
                }
                s = s.replaceAll(regex.pattern(),
                        craftingRequirements.toString(upd.getUser().getPlayer(), ChatColor.DARK_GREEN, ChatColor.RED));
                lore.set(i, s);
            }
        }
        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    @Override
    public Map<String, Object> serialize() {
        // TODO Auto-generated method stub
        Map<String, Object> map = super.serialize();
        map.put(Key.ITEM_REQUIREMENTS.toString(), craftingRequirements.serialize());
        map.put(Key.RESULT.toString(), result.serialize());
        map.put(Key.INVENTORY_REQUIREMENTS.toString(), inventoryRequirements.serialize());
        map.put(Key.CRAFTING_TIME.toString(), craftingTime);
        return map;
    }

    public final ItemStack getResult() {
        return result.clone();
    }

    public final void setResult(ItemStack result) {
        this.result = result;
    }

    public double getCraftingTime() {
        return craftingTime;
    }

    public void setCraftingTime(double craftingTime) {
        this.craftingTime = craftingTime;
    }
}
