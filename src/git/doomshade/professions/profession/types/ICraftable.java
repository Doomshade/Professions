package git.doomshade.professions.profession.types;

import git.doomshade.professions.exceptions.ProfessionInitializationException;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.Requirements;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
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

import static git.doomshade.professions.utils.Strings.ICraftableEnum.*;

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

    static Map<String, Object> serializeCraftable(ICraftable craftable) {
        Map<String, Object> map = new HashMap<>();
        map.put(ITEM_REQUIREMENTS.s, craftable.getCraftingRequirements().serialize());
        map.put(RESULT.s, craftable.getResult().serialize());
        map.put(INVENTORY_REQUIREMENTS.s, craftable.getInventoryRequirements().serialize());
        map.put(CRAFTING_TIME.s, craftable.getCraftingTime());
        return map;
    }

    void setResult(ItemStack result);

    void setCraftingTime(double craftingTime);

    static void deserializeCraftable(Map<String, Object> map, ICraftable craftable) throws ProfessionInitializationException {


        craftable.setCraftingTime((double) map.getOrDefault(CRAFTING_TIME.s, 5d));

        List<String> list = Utils.getMissingKeys(map, Strings.ICraftableEnum.values());
        if (!list.isEmpty()) {
            throw new ProfessionInitializationException(craftable.getClass(), list);
        }

        MemorySection itemReqSection = (MemorySection) map.get(ITEM_REQUIREMENTS.s);
        craftable.setCraftingRequirements(Requirements.deserialize(itemReqSection.getValues(false)));

        MemorySection invReqSection = (MemorySection) map.get(INVENTORY_REQUIREMENTS.s);
        craftable.setInventoryRequirements(Requirements.deserialize(invReqSection.getValues(false)));

        MemorySection itemStackSection = (MemorySection) map.get(RESULT.s);
        craftable.setResult(ItemStack.deserialize(itemStackSection.getValues(false)));


    }

    default ItemStack getIcon(ItemStack icon, UserProfessionData upd) {
        ItemMeta meta = icon.getItemMeta();
        List<String> lore = meta.getLore();

        for (Strings.ICraftableEnum key : Arrays.asList(ITEM_REQUIREMENTS, INVENTORY_REQUIREMENTS)) {
            Pattern regex = Pattern.compile("\\{" + key.s + "\\}");
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

    default String toStringFormat() {
        StringBuilder sb = new StringBuilder()
                .append("\ncrafting time: " + getCraftingTime())
                .append("\ncrafting result: " + getResult())
                .append("\ncrafting reqs: " + getCraftingRequirements())
                .append("\ninv reqs: " + getInventoryRequirements());
        return sb.toString();
    }
}
