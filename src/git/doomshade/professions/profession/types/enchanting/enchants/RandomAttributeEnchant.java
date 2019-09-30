package git.doomshade.professions.profession.types.enchanting.enchants;

import git.doomshade.professions.profession.types.enchanting.Enchant;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class RandomAttributeEnchant extends Enchant {

    protected RandomAttributeEnchant(ItemStack item) {
        super(item);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void use(ItemStack on, int intensity) {
        // TODO Auto-generated method stub
        if (!isEnchantable(on)) {
            return;
        }
        // List<ItemAttribute> attrs = new ArrayList<>(getAttributes(on));

        switch (intensity) {
            case DEFAULT_INTENSITY:
                break;
        }
    }

    @Override
    public List<Integer> getIntensities() {
        // TODO Auto-generated method stub
        return Arrays.asList(DEFAULT_INTENSITY);
    }

}
