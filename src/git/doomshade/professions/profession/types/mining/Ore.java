package git.doomshade.professions.profession.types.mining;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

import static git.doomshade.professions.profession.types.mining.Ore.OreEnum.*;

/**
 * Custom class for {@link git.doomshade.professions.profession.types.ItemType}.
 * Here I wanted to have a custom mining result, I'd have otherwise only passed {@link Material} as a generic argument to {@link OreItemType}.
 *
 * @author Doomshade
 */
public class Ore implements ConfigurationSerializable {


    private Material oreMaterial;
    private TreeMap<Double, ItemStack> miningResults;

    /**
     * Custom constructor
     *
     * @param oreMaterial
     * @param miningResults
     */
    public Ore(Material oreMaterial, TreeMap<Double, ItemStack> miningResults) {
        this.oreMaterial = oreMaterial;
        this.miningResults = new TreeMap<>(Comparator.naturalOrder());
        this.miningResults.putAll(miningResults);

    }

    /**
     * Required deserialize method of {@link ConfigurationSerializable}
     *
     * @param map serialized Ore
     * @return deserialized Ore
     * @throws ProfessionObjectInitializationException when Ore is not initialized correctly
     */
    public static Ore deserialize(Map<String, Object> map) throws ProfessionObjectInitializationException {
        Set<String> list = Utils.getMissingKeys(map,
                Arrays.stream(OreEnum.values())
                        .filter(x -> x != KEY_ITEMSTACK && x != KEY_CHANCE)
                        .toArray(OreEnum[]::new));
        if (!list.isEmpty()) {
            throw new ProfessionObjectInitializationException(OreItemType.class, list);
        }

        Material mat = Material.getMaterial((String) map.get(KEY_MATERIAL.s));
        MemorySection memorySection = (MemorySection) map.get(KEY_MINING_RESULT.s);

        TreeMap<Double, ItemStack> miningResults = new TreeMap<>(Comparator.naturalOrder());

        for (String s : memorySection.getKeys(false)) {
            ConfigurationSection resultSection = memorySection.getConfigurationSection(s);
            double chance = resultSection.getDouble(KEY_CHANCE.s);

            try {
                ItemStack item = ItemStack.deserialize(resultSection.getConfigurationSection(KEY_ITEMSTACK.s).getValues(true));
                miningResults.put(chance, item);

            } catch (NullPointerException e) {
                throw new ProfessionObjectInitializationException(OreItemType.class, Collections.singletonList(KEY_ITEMSTACK.s));
            }
        }


        return new Ore(mat, miningResults);
    }

    @Override
    public Map<String, Object> serialize() {

        return new HashMap<String, Object>() {
            {
                put(KEY_MATERIAL.s, oreMaterial.name());

                final Iterator<Entry<Double, ItemStack>> iterator = miningResults.entrySet().iterator();
                for (int i = 0; i < miningResults.size(); i++) {
                    String s = KEY_MINING_RESULT.s.concat(String.valueOf(i));
                    Entry<Double, ItemStack> entry = iterator.next();
                    put(s.concat(KEY_CHANCE.s), entry.getKey());
                    put(s.concat(KEY_ITEMSTACK.s), entry.getValue().serialize());
                }
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
    @Nullable
    public ItemStack getMiningResult() {
        double random = Math.random() * 100;

        for (Map.Entry<Double, ItemStack> entry : miningResults.entrySet()) {
            if (random < entry.getKey()) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Sets the mining result
     *
     * @param miningResult the ItemStack
     */
    public void setMiningResult(TreeMap<Double, ItemStack> miningResult) {
        this.miningResults = miningResult;
    }

    public TreeMap<Double, ItemStack> getMiningResults() {
        return miningResults;
    }

    /**
     * Enum for keys in file
     */
    enum OreEnum implements FileEnum {
        KEY_MATERIAL("material"), KEY_MINING_RESULT("mining-result"), KEY_CHANCE("chance"), KEY_ITEMSTACK("item");

        final String s;

        OreEnum(String s) {
            this.s = s;
        }

        @Override
        public Map<Enum, Object> getDefaultValues() {
            return new HashMap<Enum, Object>() {
                {
                    put(KEY_MATERIAL, Material.GLASS);
                    TreeMap<Double, ItemStack> map = new TreeMap<>(Comparator.naturalOrder());
                    map.put(0d, new ItemStack(Material.BED));
                    put(KEY_MINING_RESULT, Maps.asMap(Sets.newHashSet(0d), x -> ItemUtils.EXAMPLE_RESULT.serialize()));
                }
            };
        }

        @Override
        public String toString() {
            return s;
        }
    }
}
