package git.doomshade.professions.profession.professions.blacksmithing;

import git.doomshade.professions.utils.ItemUtils;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BSItemStack implements ConfigurationSerializable {
    final ItemStack item;

    public BSItemStack(ItemStack item) {
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
