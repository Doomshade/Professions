package git.doomshade.professions.profession.utils;

import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.utils.ItemUtils;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class YieldResult implements ConfigurationSerializable, Comparable<YieldResult> {
    public final double chance;
    public final ItemStack drop;

    public YieldResult(double chance, ItemStack drop) {
        this.chance = chance;
        this.drop = drop;
    }

    public static YieldResult deserialize(Map<String, Object> map) throws ConfigurationException, InitializationException {
        MemorySection section = (MemorySection) map.get("item");
        final ItemStack deserialize;
        try {
            deserialize = ItemUtils.deserialize(section.getValues(false));
        } catch (ConfigurationException e) {
            e.append("Yield Result");
            throw e;
        }
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
    public int compareTo(@NotNull YieldResult o) {
        return Double.compare(chance, o.chance);
    }
}
