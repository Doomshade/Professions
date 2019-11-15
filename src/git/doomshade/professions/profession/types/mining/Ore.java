package git.doomshade.professions.profession.types.mining;

import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static git.doomshade.professions.profession.types.mining.Ore.OreEnum.KEY_MATERIAL;
import static git.doomshade.professions.profession.types.mining.Ore.OreEnum.KEY_MINING_RESULT;

public class Ore implements ConfigurationSerializable {

    public static Ore deserialize(Map<String, Object> map) throws ProfessionObjectInitializationException {
        List<String> list = Utils.getMissingKeys(map, OreEnum.values());
        if (!list.isEmpty()) {
            throw new ProfessionObjectInitializationException(OreItemType.class, list, -1);
        }

        Material mat = Material.getMaterial((String) map.get(KEY_MATERIAL.s));
        MemorySection memorySection = (MemorySection) map.get(KEY_MINING_RESULT.s);
        ItemStack item = ItemStack.deserialize(memorySection.getValues(true));
        return new Ore(mat, item);
    }

    private Material oreMaterial;
    private ItemStack miningResult;

    public Ore(Material oreMaterial, ItemStack miningResult) {
        this.oreMaterial = oreMaterial;
        this.miningResult = miningResult;
    }

    @Override
    public Map<String, Object> serialize() {

        return new HashMap<String, Object>() {
            {
                put(KEY_MATERIAL.s, oreMaterial.name());
                put(KEY_MINING_RESULT.s, miningResult.serialize());
            }
        };
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

    enum OreEnum {
        KEY_MATERIAL("material"), KEY_MINING_RESULT("mining-result");

        final String s;

        OreEnum(String s) {
            this.s = s;
        }
    }
}
