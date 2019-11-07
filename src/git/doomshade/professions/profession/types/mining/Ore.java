package git.doomshade.professions.profession.types.mining;

import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Ore implements ConfigurationSerializable {
    private static final String KEY_MATERIAL = "material", KEY_MINING_RESULT = "mining-result";

    private Material oreMaterial;
    private ItemStack miningResult;

    public Ore(Material oreMaterial, ItemStack miningResult) {
        this.oreMaterial = oreMaterial;
        this.miningResult = miningResult;
    }

    public static Ore deserialize(Map<String, Object> map) {
        MemorySection memorySection = (MemorySection) map.get(KEY_MINING_RESULT);
        ItemStack item = ItemStack.deserialize(memorySection.getValues(true));
        Material mat = Material.getMaterial((String) map.get(KEY_MATERIAL));
        return new Ore(mat, item);
    }

    public Material getOreMaterial() {
        return oreMaterial;
    }

    public void setOreMaterial(Material oreMaterial) {
        this.oreMaterial = oreMaterial;
    }

    public ItemStack getMiningResult() {
        return miningResult;
    }

    public void setMiningResult(ItemStack miningResult) {
        this.miningResult = miningResult;
    }

    @Override
    public Map<String, Object> serialize() {

        return new HashMap<String, Object>() {
            {
                put(KEY_MATERIAL, oreMaterial.name());
                put(KEY_MINING_RESULT, miningResult.serialize());
            }
        };
    }
}
