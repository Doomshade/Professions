package git.doomshade.professions.profession.types.mining.smelting;

import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Requirements;
import git.doomshade.professions.utils.Utils;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static git.doomshade.professions.profession.types.mining.smelting.Bar.BarEnum.*;

@Deprecated
public class Bar implements ConfigurationSerializable {
    private final int oreId;
    private final Requirements requirements;
    private final ItemStack result;

    public Bar(int oreId, Requirements requirements, ItemStack result) {
        this.oreId = oreId;
        this.requirements = requirements;
        this.result = result;
    }

    public static Bar deserialize(Map<String, Object> map) throws ProfessionObjectInitializationException {
        Set<String> set = Utils.getMissingKeys(map, BarEnum.values());
        if (!set.isEmpty()) {
            throw new ProfessionObjectInitializationException(BarItemType.class, set);
        }
        int oreId = (int) map.get(ORE_ID.s);
        MemorySection requirementsSection = (MemorySection) map.get(REQUIREMENTS.s);
        Requirements requirements = Requirements.deserialize(requirementsSection.getValues(false));

        MemorySection resultSection = (MemorySection) map.get(RESULT.s);
        ItemStack item = ItemStack.deserialize(resultSection.getValues(false));

        return new Bar(oreId, requirements, item);
    }

    @Override
    public Map<String, Object> serialize() {
        return new HashMap<String, Object>() {
            {
                put(ORE_ID.s, oreId);
                put(REQUIREMENTS.s, requirements.serialize());
                put(RESULT.s, result.serialize());
            }
        };
    }


    enum BarEnum implements FileEnum {
        ORE_ID("ore-id"), REQUIREMENTS("requirements"), RESULT("result");

        final String s;

        BarEnum(String s) {
            this.s = s;
        }

        @Override
        public Map<Enum, Object> getDefaultValues() {
            return new HashMap<Enum, Object>() {
                {
                    put(RESULT, ItemUtils.EXAMPLE_RESULT.serialize());
                }
            };
        }

        @Override
        public String toString() {
            return s;
        }
    }
}
