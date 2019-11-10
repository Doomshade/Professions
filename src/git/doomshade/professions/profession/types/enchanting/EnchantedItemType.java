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

public class EnchantedItemType extends ItemType<Enchant> implements ITrainable, ICraftable {
    private static String ENCHANT = "enchant";
    private boolean trainable = true;
    private int cost = 0;
    private String trainableStringId;

    private double craftingTime = 3d;
    private ItemStack result = new ItemStack(Material.GLASS);

    private Requirements inventoryRequirements = new Requirements();
    private Requirements craftingRequirements = new Requirements();

    public EnchantedItemType() {
        super();
    }

    public EnchantedItemType(Enchant object, int exp) {
        super(object, exp);
    }

    @Override
    public boolean isValid(Enchant t) {
        return getObject().getItem().isSimilar(t.getItem());
    }

    @Override
    protected Map<String, Object> getSerializedObject(Enchant object) {
        Map<String, Object> map = new HashMap<>();
        map.put(ENCHANT, object.serialize());
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
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.putAll(ITrainable.serializeTrainable(this));
        map.putAll(ICraftable.serializeCraftable(this));
        return map;
    }


    @Override
    public void deserialize(Map<String, Object> map) {
        super.deserialize(map);
        try {
            ITrainable.deserializeTrainable(map, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ICraftable.deserializeCraftable(map, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public String toString() {
        return super.toString() + toStringFormat();
    }
}
