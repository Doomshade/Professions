package git.doomshade.professions.profession.types.enchanting;

import git.doomshade.professions.profession.types.ICraftable;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ITrainable;
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
 * An enchant item type example for {@link git.doomshade.professions.profession.professions.EnchantingProfession}
 *
 * @author Doomshade
 */
public class EnchantedItemItemType extends ItemType<Enchant> implements ITrainable, ICraftable {
    private static String ENCHANT = "enchant";
    private boolean trainable = true;
    private int cost = 0;
    private String trainableStringId;

    private double craftingTime = 3d;
    private ItemStack result = new ItemStack(Material.GLASS);

    private Requirements inventoryRequirements = new Requirements();
    private Requirements craftingRequirements = new Requirements();
    private Map<Sound, String> sounds = new HashMap<>();

    /**
     * Required constructor
     */
    public EnchantedItemItemType() {
        super();
    }

    /**
     * Required constructor
     *
     * @param object
     * @param exp
     */
    public EnchantedItemItemType(Enchant object, int exp) {
        super(object, exp);
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
    public String getTrainableId() {
        if (trainableStringId == null) {
            if (getObject() == null) {
                throw new IllegalStateException("Both trainable id and object for " + this + " is null, cannot get a trainable id!");
            }
            trainableStringId = getObject().getClass().getSimpleName();
        }
        return trainableStringId;
    }

    @Override
    public ItemStack getIcon(UserProfessionData upd) {
        return getIcon(super.getIcon(upd), upd);
    }

    @Override
    public boolean isTrainable() {
        return trainable;
    }

    @Override
    public int getCost() {
        return cost;
    }

    @Override
    public void setCost(int cost) {
        this.cost = cost;
    }

    @Override
    public void setTrainable(boolean trainable) {
        this.trainable = trainable;
    }

    @Override
    public void setTrainableId(String trainableStringId) {
        this.trainableStringId = trainableStringId;
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
