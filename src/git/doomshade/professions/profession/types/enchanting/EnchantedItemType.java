package git.doomshade.professions.profession.types.enchanting;

import git.doomshade.professions.profession.types.CraftableItemType;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.profession.types.Trainable;
import org.bukkit.ChatColor;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class EnchantedItemType extends CraftableItemType<Enchant> implements Trainable {
    private static String ENCHANT = "enchant", TRAINABLE = "trainable", COST = "trainable-cost";
    private boolean trainable = true;
    private int cost = 0;

    public EnchantedItemType(Enchant item, int exp) {
        super(item, exp);
    }

    protected EnchantedItemType() {
        super();
    }

    protected EnchantedItemType(Map<String, Object> map, int id) {
        super(map, id);
    }

    @Override
    public boolean isValid(Enchant t) {
        return getObject().getItem().isSimilar(t.getItem());
    }

    @Override
    protected Map<String, Object> getSerializedObject(Enchant object) {
        Map<String, Object> map = new HashMap<>();
        map.put(ENCHANT, object.serialize());
        map.put(TRAINABLE, trainable);
        map.put(COST, cost);
        return map;
    }

    @Override
    protected Enchant deserializeObject(Map<String, Object> map) {
        this.trainable = (boolean) map.get(TRAINABLE);
        this.cost = (int) map.get(COST);
        return Enchant.deserialize(((MemorySection) map.get(ENCHANT)).getValues(true));
    }

    @Override
    public Class<? extends ItemTypeHolder<?>> getHolder() {
        return EnchantedItemTypeHolder.class;
    }

    @Override
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        return IEnchanting.class;
    }

    @Override
    public String getExtra() {
        ItemStack item = getIcon(null);
        String defValue = item.getType().toString().toLowerCase();
        if (!item.hasItemMeta()) {
            return defValue;
        }
        ItemMeta itemMeta = item.getItemMeta();
        return itemMeta.hasDisplayName() ? ChatColor.stripColor(itemMeta.getDisplayName().toLowerCase()) : defValue;
    }

    @Override
    public boolean isTrainable() {
        return trainable;
    }

    @Override
    public void setTrainable(boolean trainable) {
        this.trainable = trainable;
    }

    @Override
    public int getCost() {
        return cost;
    }

}
