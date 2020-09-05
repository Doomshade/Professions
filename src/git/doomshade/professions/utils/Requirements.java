package git.doomshade.professions.utils;

import git.doomshade.professions.profession.ICraftable;
import org.bukkit.ChatColor;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

/**
 * A class managing requirements of a player.
 *
 * @author Doomshade
 * @see ICraftable
 */
public class Requirements implements ConfigurationSerializable, Iterable<ItemStack> {

    // keep it as a list due to indexing in file
    private final List<ItemStack> items;

    /**
     * @param items the requirements
     */
    public Requirements(List<ItemStack> items) {
        this.items = items;
    }

    /**
     * Calls {@link Requirements#Requirements(List)} with an empty {@link ArrayList}.
     */
    public Requirements() {
        this(new ArrayList<>());
    }

    /**
     * Deserialization method
     *
     * @param map the {@link ConfigurationSerializable#serialize()}
     * @return deserialized class
     * @see ConfigurationSerializable
     */
    public static Requirements deserialize(Map<String, Object> map) {
        List<ItemStack> items = new ArrayList<>();
        for (Object next : map.values()) {
            if (next instanceof MemorySection)
                items.add(ItemUtils.deserialize(((MemorySection) next).getValues(true)));
        }
        return new Requirements(items);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();

        for (int i = 0; i < items.size(); i++) {
            map.put(String.valueOf(i), ItemUtils.serialize(items.get(i)));
        }
        return map;
    }

    /**
     * @return the list of requirements
     */
    public List<ItemStack> getRequirements() {
        return items;
    }

    /**
     * Adds an {@link ItemStack} requirement
     *
     * @param requirement the requirement to add
     */
    public void addRequirement(ItemStack requirement) {
        items.add(requirement);
    }

    /**
     * @param player the player to check the requirements for
     * @return {@code true} if player meets requirements, {@code false} otherwise
     */
    public boolean meetsRequirements(Player player) {
        HashSet<ItemStack> itemz = getMetRequirements(player);
        if (itemz.size() > items.size()) {
            throw new IllegalStateException("This should not happen???");
        }
        return itemz.size() == items.size();
    }

    private HashSet<ItemStack> getMetRequirements(Player player) {
        HashSet<ItemStack> itemz = new HashSet<>();
        for (ItemStack itemContent : player.getInventory()) {
            if (itemContent == null) {
                continue;
            }
            for (ItemStack item : items) {
                if (item.isSimilar(itemContent) && itemContent.getAmount() >= item.getAmount()) {
                    itemz.add(item);
                }
            }
        }
        return itemz;
    }

    /**
     * @param player the player to check the requirements for
     * @return the missing requirements of player
     */
    public Collection<ItemStack> getMissingRequirements(Player player) {
        HashSet<ItemStack> items = new HashSet<>(this.items);
        items.removeAll(getMetRequirements(player));
        return items;
    }

    /**
     * Removes requirements from players inventory
     *
     * @param player the player to remove the requirements from
     */
    public void consumeRequiredItems(Player player) {
        PlayerInventory inv = player.getInventory();
        items.forEach(inv::removeItem);
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return items.iterator();
    }

    /**
     * Calls {@link #toString(Player, ChatColor, ChatColor)} with all null arguments.
     *
     * @return the {@link String} representation of this class
     */
    @Override
    public String toString() {
        return toString(null, null, null);
    }

    /**
     * @param player            the player to base this {@link String} representation about
     * @param requirementMet    the custom met requirement color
     * @param requirementNotMet the custom not met requirement color
     * @return the {@link String} representation of this class
     */
    public String toString(Player player, ChatColor requirementMet, ChatColor requirementNotMet) {
        StringBuilder sb = new StringBuilder();
        Iterator<ItemStack> iterator = iterator();
        HashSet<ItemStack> metRequirements = player != null ? getMetRequirements(player) : new HashSet<>();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (metRequirements.contains(item) && requirementMet != null) {
                sb.append(requirementMet);

            } else if (requirementNotMet != null) {
                sb.append(requirementNotMet);
            }
            sb.append(item.getAmount() + "x "
                    + (item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName()
                    : item.getType().toString()));
            if (iterator.hasNext())
                sb.append(", ");
        }
        return sb.toString();
    }

}
