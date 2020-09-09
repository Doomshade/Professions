package git.doomshade.professions.profession.professions.enchanting.enchants;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.manager.AttributeManager;
import git.doomshade.professions.profession.professions.enchanting.Enchant;
import git.doomshade.professions.utils.ItemAttribute;
import git.doomshade.professions.utils.Range;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RandomAttributeEnchant extends Enchant {
    private static final HashMap<Integer, Range> INTENSITY_VALUES = new HashMap<Integer, Range>(){
        {
            put(DEFAULT_INTENSITY, new Range(1, 5));
            put(1, new Range(5, 10));
        }
    };

    public RandomAttributeEnchant(ItemStack item) {
        super(item);
    }

    @Override
    public List<Integer> getIntensities() {
        return Arrays.asList(DEFAULT_INTENSITY, 1);
    }

    @Override
    protected void init() {
        setEnchantFunction((x, y) -> {
            ItemMeta meta = x.getItemMeta();
            switch (y) {
                case DEFAULT_INTENSITY:
                    HashMap<String, AttributeManager.Attribute> attributes = SkillAPI.getAttributeManager().getAttributes();
                    int random = this.random.nextInt(attributes.size());
                    int i = 0;
                    String randomAttr = "";
                    for (String attr : attributes.keySet()) {
                        if (i == random) {
                            randomAttr = attr;
                            break;
                        }
                        i++;
                    }
                    if (randomAttr.isEmpty()) {
                        throw new IllegalStateException("Returned an empty attribute from sapi attributes (???)");
                    }
                    ItemAttribute attribute = new ItemAttribute(randomAttr, INTENSITY_VALUES.get(y).getRandom(), ItemAttribute.AttributeType.SKILLAPI);
                    break;
                case 1:
                    break;
            }
            return null;
        });
    }

}
