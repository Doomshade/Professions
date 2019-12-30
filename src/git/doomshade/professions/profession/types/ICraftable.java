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

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static git.doomshade.professions.utils.Strings.ICraftableEnum.*;

/**
 * Interface for craftable {@link ItemType}s. Implement this in a class extending {@link ItemType},
 * then override {@link ItemType#deserialize(Map)} and call {@link #deserializeCraftable(Map, ICraftable)}
 * with the map and {@code this} argument inside the {@link ItemType#deserialize(Map)} method.
 * Override {@link ItemType#serialize()} as well and call {@link Map#putAll(Map)} on a {@code super.}{@link ItemType#serialize()} {@link Map} variable with an argument
 * of {@link #serializeCraftable(ICraftable)} and return the map.
 *
 * @author Doomshade
 * @see git.doomshade.professions.profession.types.enchanting.EnchantedItemType on GitHub for an example
 */
public interface ICraftable {

    /**
     * Make sure to override the {@link ItemType#serialize()} method and call and call {@link Map#putAll(Map)} of this map.
     *
     * @param craftable the craftable item
     * @return the serialized form of this class
     */
    static Map<String, Object> serializeCraftable(ICraftable craftable) {
        Map<String, Object> map = new HashMap<>();
        map.put(ITEM_REQUIREMENTS.s, craftable.getCraftingRequirements().serialize());
        map.put(RESULT.s, craftable.getResult().serialize());
        map.put(INVENTORY_REQUIREMENTS.s, craftable.getInventoryRequirements().serialize());
        map.put(CRAFTING_TIME.s, craftable.getCraftingTime());
        map.put(SOUND_CRAFTED.s, craftable.getSounds().get(Sound.ON_CRAFT));
        map.put(SOUND_CRAFTING.s, craftable.getSounds().get(Sound.CRAFTING));
        return map;
    }

    /**
     * Make sure to override the {@link ItemType#deserialize(Map)} method and call this method.
     *
     * @param map       the serialized version of this class
     * @param craftable the craftable item
     * @throws ProfessionInitializationException if the deserialization was unsuccessful
     */
    static void deserializeCraftable(Map<String, Object> map, ICraftable craftable) throws ProfessionInitializationException {

        craftable.setCraftingTime((double) map.getOrDefault(CRAFTING_TIME.s, 5d));

        craftable.setSounds(new HashMap<Sound, String>() {
            {
                put(Sound.CRAFTING, (String) map.getOrDefault(SOUND_CRAFTING.s, "block.fire.extinguish"));
                put(Sound.ON_CRAFT, (String) map.getOrDefault(SOUND_CRAFTED.s, "block.fire.ambient"));
            }
        });

        Set<String> list = Utils.getMissingKeys(map, Strings.ICraftableEnum.values());
        if (!list.isEmpty()) {
            throw new ProfessionInitializationException((Class<? extends ItemType>) craftable.getClass(), list);
        }

        MemorySection itemReqSection = (MemorySection) map.get(ITEM_REQUIREMENTS.s);
        craftable.setCraftingRequirements(Requirements.deserialize(itemReqSection.getValues(false)));

        MemorySection invReqSection = (MemorySection) map.get(INVENTORY_REQUIREMENTS.s);
        craftable.setInventoryRequirements(Requirements.deserialize(invReqSection.getValues(false)));

        MemorySection itemStackSection = (MemorySection) map.get(RESULT.s);
        craftable.setResult(ItemStack.deserialize(itemStackSection.getValues(false)));


    }

    /**
     * @return the crafting time of the item type
     */
    double getCraftingTime();

    /**
     * Sets the crafting time of the craft
     *
     * @param craftingTime the crafting time
     */
    void setCraftingTime(double craftingTime);

    /**
     * @return the result of crafting
     */
    ItemStack getResult();

    /**
     * Sets the result of the the craft
     *
     * @param result the result
     */
    void setResult(ItemStack result);

    /**
     * @return the inventory requirements
     */
    Requirements getInventoryRequirements();

    /**
     * Sets the inventory requirements
     *
     * @param inventoryRequirements the inventory requirements to set
     */
    void setInventoryRequirements(Requirements inventoryRequirements);

    /**
     * @return the crafting requirements
     */
    Requirements getCraftingRequirements();

    /**
     * Sets the crafting requirements
     *
     * @param craftingRequirements the crafting requirements to set
     */
    void setCraftingRequirements(Requirements craftingRequirements);

    Map<Sound, String> getSounds();

    void setSounds(Map<Sound, String> sounds);

    enum Sound {
        CRAFTING, ON_CRAFT
    }

    default Function<ItemStack, ?> getExtra() {
        return null;
    }

    /**
     * Determines whether or not the player meets inventory requirements
     *
     * @param player the player to check for
     * @return {@code true} if the player meets inventory requirements, {@code false} otherwise
     */
    default boolean meetsInventoryRequirements(Player player) {
        return getInventoryRequirements().meetsRequirements(player);
    }

    /**
     * Determines whether or not the player meets crafting requirements
     *
     * @param player the player to check for
     * @return {@code true} if the player meets crafting requirements, {@code false} otherwise
     */
    default boolean meetsCraftingRequirements(Player player) {
        return getCraftingRequirements().meetsRequirements(player);
    }

    /**
     * Removes crafting requirements from a player's inventory
     *
     * @param player the player to remove the items from
     */
    default void removeCraftingRequirements(Player player) {
        getCraftingRequirements().removeRequiredItems(player);
    }

    /**
     * Adds an additional crafting requirement for this item type
     *
     * @param item the crafting requirement to add
     */
    default void addCraftingRequirement(ItemStack item) {
        getCraftingRequirements().addRequirement(item);
    }

    /**
     * Adds an additional inventory requirement for this item type
     *
     * @param item the inventory requirement to add
     */
    default void addInventoryRequirement(ItemStack item) {
        getInventoryRequirements().addRequirement(item);
    }

    /**
     * Call this method instead of {@link ItemType#getIcon(UserProfessionData)} if you want to visualize all the properties of this item type.
     *
     * @param icon the original icon
     * @param upd  the user profession data
     * @return the icon based on {@link git.doomshade.professions.user.User}'s {@link git.doomshade.professions.Profession} data
     */
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

    /**
     * Basically a {@link Object#toString()}.
     *
     * @return the String representation of this class
     */
    default String toStringFormat() {
        StringBuilder sb = new StringBuilder()
                .append("\ncrafting time: " + getCraftingTime())
                .append("\ncrafting result: " + getResult())
                .append("\ncrafting reqs: " + getCraftingRequirements())
                .append("\ninv reqs: " + getInventoryRequirements());
        return sb.toString();
    }
}
