package git.doomshade.professions.profession.types.mining;

import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static git.doomshade.professions.profession.types.mining.Ore.OreEnum.KEY_MATERIAL;
import static git.doomshade.professions.profession.types.mining.Ore.OreEnum.KEY_MINING_RESULT;

/**
 * Custom class for {@link git.doomshade.professions.profession.types.ItemType}.
 * Here I wanted to have a custom mining result, I'd have otherwise only passed {@link Material} as a generic argument to {@link OreItemType}.
 *
 * @author Doomshade
 */
public class Ore implements ConfigurationSerializable {


    private Material oreMaterial;
    private ItemStack miningResult;

    /**
     * Custom constructor
     *
     * @param oreMaterial
     * @param miningResult
     */
    public Ore(Material oreMaterial, ItemStack miningResult) {
        this.oreMaterial = oreMaterial;
        this.miningResult = miningResult;
    }

    /**
     * Required deserialize method of {@link ConfigurationSerializable}
     *
     * @param map serialized Ore
     * @return deserialized Ore
     * @throws ProfessionObjectInitializationException when Ore is not initialized correctly
     */
    public static Ore deserialize(Map<String, Object> map) throws ProfessionObjectInitializationException {
        Set<String> list = Utils.getMissingKeys(map, OreEnum.values());
        if (!list.isEmpty()) {
            throw new ProfessionObjectInitializationException(OreItemType.class, list);
        }

        Material mat = Material.getMaterial((String) map.get(KEY_MATERIAL.s));
        MemorySection memorySection = (MemorySection) map.get(KEY_MINING_RESULT.s);
        ItemStack item = ItemStack.deserialize(memorySection.getValues(true));
        return new Ore(mat, item);
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

    /**
     * @return the ore material
     */
    public Material getOreMaterial() {
        return oreMaterial;
    }

    /**
     * Sets the ore material
     *
     * @param oreMaterial the material
     */
    public void setOreMaterial(Material oreMaterial) {
        this.oreMaterial = oreMaterial;
    }

    /**
     * @return the mining result
     */
    public ItemStack getMiningResult() {
        return miningResult;
    }

    /**
     * Sets the mining result
     *
     * @param miningResult the ItemStack
     */
    public void setMiningResult(ItemStack miningResult) {
        this.miningResult = miningResult;
    }

    /**
     * Enum for keys in file
     */
    enum OreEnum {
        KEY_MATERIAL("material"), KEY_MINING_RESULT("mining-result");

        final String s;

        OreEnum(String s) {
            this.s = s;
        }
    }
}
