package git.doomshade.professions.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class Requirements implements ConfigurationSerializable, Iterable<ItemStack> {
    private final List<ItemStack> items;

    public Requirements(List<ItemStack> items) {
        this.items = items;
    }

    public Requirements(){
        this(new ArrayList<>());
    }

    public static Requirements deserialize(Map<String, Object> map) {
        List<ItemStack> items = new ArrayList<>();
        Iterator<Object> iterator = map.values().iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            items.add(ItemStack.deserialize(((MemorySection) next).getValues(true)));
        }
        return new Requirements(items);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            map.put(String.valueOf(i), items.get(i).serialize());
        }
        return map;
    }

    public List<ItemStack> getRequirements() {
        return new ArrayList<>(items);
    }

    public void addRequirement(ItemStack requirement) {
        items.add(requirement);
    }

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
                if (item.isSimilar(itemContent)) {
                    itemz.add(item);
                }
            }
        }
        return itemz;
    }

    public HashSet<ItemStack> getMissingRequirements(Player player) {
        HashSet<ItemStack> items = new HashSet<>(this.items);
        items.removeAll(getMetRequirements(player));
        return items;
    }

    public void removeRequiredItems(Player player) {
        PlayerInventory inv = player.getInventory();
        items.forEach(inv::removeItem);
    }

    @Override
    public Iterator<ItemStack> iterator() {
        return items.iterator();
    }

    @Override
    public String toString() {
        return toString(null, null, null);
    }

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
