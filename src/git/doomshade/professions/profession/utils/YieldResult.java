package git.doomshade.professions.profession.utils;

import git.doomshade.professions.utils.ItemUtils;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class YieldResult implements ConfigurationSerializable, Comparable<YieldResult> {
    public final double chance;
    public final ItemStack drop;

    public YieldResult(double chance, ItemStack drop) {
        this.chance = chance;
        this.drop = drop;
    }

    public static YieldResult deserialize(Map<String, Object> map) {
        MemorySection section = (MemorySection) map.get("item");
        final ItemStack deserialize = ItemUtils.deserialize(section.getValues(false));
        final double chance = (double) map.getOrDefault("chance", 0);
        return new YieldResult(chance, deserialize);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("chance", chance);
        map.put("item", ItemUtils.serialize(drop));
        return map;
    }

    @Override
    public int compareTo(YieldResult o) {
        return Double.compare(chance, o.chance);
    }
}
