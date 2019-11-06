package git.doomshade.professions.profession.types;

import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.Requirements;
import org.bukkit.ChatColor;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ICraftable {

    double getCraftingTime();

    ItemStack getResult();

    Requirements getInventoryRequirements();

    Requirements getCraftingRequirements();

    void setCraftingRequirements(Requirements craftingRequirements);

    void setInventoryRequirements(Requirements inventoryRequirements);

    default boolean meetsInventoryRequirements(Player player) {
        return getInventoryRequirements().meetsRequirements(player);
    }

    default boolean meetsCraftingRequirements(Player player) {
        return getCraftingRequirements().meetsRequirements(player);
    }

    default void removeCraftingRequirements(Player player) {
        getCraftingRequirements().removeRequiredItems(player);
    }

    default void addCraftingRequirement(ItemStack item) {
        getCraftingRequirements().addRequirement(item);
    }

    default void addInventoryRequirement(ItemStack item) {
        getInventoryRequirements().addRequirement(item);
    }

    default ItemStack getIcon(ItemStack icon, UserProfessionData upd) {
        ItemMeta meta = icon.getItemMeta();
        List<String> lore = meta.getLore();

        for (ItemType.Key key : Arrays.asList(ItemType.Key.ITEM_REQUIREMENTS, ItemType.Key.INVENTORY_REQUIREMENTS)) {
            Pattern regex = Pattern.compile("\\{" + key + "\\}");
            for (int i = 0; i < lore.size(); i++) {
                String s = lore.get(i);
                Matcher m = regex.matcher(s);
                if (!m.find()) {
                    continue;
                }
                s = s.replaceAll(regex.pattern(),
                        getCraftingRequirements().toString(upd.getUser().getPlayer(), ChatColor.DARK_GREEN, ChatColor.RED));
                lore.set(i, s);
            }
        }
        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    void setResult(ItemStack result);

    void setCraftingTime(double craftingTime);

    static Map<String, Object> serializeCraftable(ICraftable craftable) {
        Map<String, Object> map = new HashMap<>();
        map.put(ItemType.Key.ITEM_REQUIREMENTS.toString(), craftable.getCraftingRequirements().serialize());
        map.put(ItemType.Key.RESULT.toString(), craftable.getResult().serialize());
        map.put(ItemType.Key.INVENTORY_REQUIREMENTS.toString(), craftable.getInventoryRequirements().serialize());
        map.put(ItemType.Key.CRAFTING_TIME.toString(), craftable.getCraftingTime());
        return map;
    }

    static void deserializeCraftable(Map<String, Object> map, ICraftable craftable) {
        MemorySection itemReqSection = (MemorySection) map.get(ItemType.Key.ITEM_REQUIREMENTS.toString());

        if (itemReqSection != null)
            craftable.setCraftingRequirements(Requirements.deserialize(itemReqSection.getValues(false)));

        MemorySection invReqSection = (MemorySection) map.get(ItemType.Key.ITEM_REQUIREMENTS.toString());
        if (invReqSection != null)
            craftable.setInventoryRequirements(Requirements.deserialize(invReqSection.getValues(false)));

        MemorySection itemStackSection = (MemorySection) map.get(ItemType.Key.RESULT.toString());
        if (itemStackSection != null)
            craftable.setResult(ItemStack.deserialize(itemStackSection.getValues(false)));
        craftable.setCraftingTime((double) map.get(ItemType.Key.CRAFTING_TIME.toString()));
    }

    default String toStringFormat() {
        StringBuilder sb = new StringBuilder()
                .append("\ncrafting time: " + getCraftingTime())
                .append("\ncrafting result: " + getResult())
                .append("\ncrafting reqs: " + getCraftingRequirements())
                .append("\ninv reqs: " + getInventoryRequirements());
        return sb.toString();
    }
}
