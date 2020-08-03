package git.doomshade.professions.profession.professions.enchanting;

import git.doomshade.professions.profession.ICraftable;
import git.doomshade.professions.profession.types.IEnchanting;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.Requirements;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * An enchant item type example for {@link EnchantingProfession}
 *
 * @author Doomshade
 */
public class EnchantedItemItemType extends ItemType<Enchant> implements ICraftable {
    private static String ENCHANT = "enchant";

    private double craftingTime = 3d;
    private ItemStack result = new ItemStack(Material.GLASS);

    private Requirements inventoryRequirements = new Requirements();
    private Requirements craftingRequirements = new Requirements();
    private Map<Sound, String> sounds = new HashMap<>();

    /**
     * Constructor for creation of the item type object
     *
     * @param object
     */
    public EnchantedItemItemType(Enchant object) {
        super(object);
    }

    @Override
    public boolean equalsObject(Enchant t) {
        Enchant ench = getObject();
        if (ench == null || t == null) {
            return false;
        }
        return ench.getItem().isSimilar(t.getItem());
    }

    @Override
    public Map<String, Object> getSerializedObject() {
        Map<String, Object> map = new HashMap<>();
        Enchant ench = getObject();
        if (ench != null)
            map.put(ENCHANT, ench.serialize());
        return map;
    }

    @Override
    protected Enchant deserializeObject(Map<String, Object> map) {
        return Enchant.deserialize(((MemorySection) map.get(ENCHANT)).getValues(true));
    }

    @Override
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        return IEnchanting.class;
    }

    @Override
    public ItemStack getIcon(UserProfessionData upd) {
        return getIcon(super.getIcon(upd), upd);
    }

    @Override
    public double getCraftingTime() {
        return craftingTime;
    }

    @Override
    public void setCraftingTime(double craftingTime) {
        this.craftingTime = craftingTime;
    }

    @Override
    public ItemStack getResult() {
        return result;
    }

    @Override
    public void setResult(ItemStack result) {
        this.result = result;
    }

    @Override
    public Requirements getInventoryRequirements() {
        return inventoryRequirements;
    }

    @Override
    public void setInventoryRequirements(Requirements inventoryRequirements) {
        this.inventoryRequirements = inventoryRequirements;
    }

    @Override
    public Requirements getCraftingRequirements() {
        return craftingRequirements;
    }

    @Override
    public void setCraftingRequirements(Requirements craftingRequirements) {
        this.craftingRequirements = craftingRequirements;
    }

    @Override
    public Map<Sound, String> getSounds() {
        return sounds;
    }

    @Override
    public void setSounds(Map<Sound, String> sounds) {
        this.sounds = sounds;
    }

    @Override
    public String toString() {
        return super.toString() + toStringFormat();
    }

    @Override
    public Function<ItemStack, PreEnchantedItem> getExtra() {
        return x -> new PreEnchantedItem(getObject(), x);
    }
}
