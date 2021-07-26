package git.doomshade.professions.profession.professions.smelting;

import git.doomshade.professions.utils.ItemUtils;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BarItemStack implements ConfigurationSerializable {
    final ItemStack item;

    public BarItemStack(ItemStack item) {
        this.item = item;
    }


    @Override
    public @NotNull Map<String, Object> serialize() {
        return new HashMap<>() {
            {
                put("item", ItemUtils.serialize(item));
            }
        };
    }
}
