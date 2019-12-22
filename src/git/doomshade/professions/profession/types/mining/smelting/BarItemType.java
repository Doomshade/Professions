package git.doomshade.professions.profession.types.mining.smelting;

import git.doomshade.professions.exceptions.ProfessionInitializationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.types.ICraftable;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.crafting.ICrafting;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Requirements;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class BarItemType extends ItemType<ItemStack> implements ICraftable {

    private double craftingTime = 5d;
    private ItemStack result = ItemUtils.EXAMPLE_RESULT;
    private Requirements inventoryRequirements = new Requirements(), craftingRequirements = new Requirements();

    public BarItemType() {
        super();
    }

    public BarItemType(ItemStack object, int exp) {
        super(object, exp);
    }

    @Override
    public void deserialize(Map<String, Object> map) throws ProfessionInitializationException {
        super.deserialize(map);
        ICraftable.deserializeCraftable(map, this);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.putAll(ICraftable.serializeCraftable(this));
        return map;
    }

    @Override
    public Map<String, Object> getSerializedObject() {
        final ItemStack item = getObject();
        if (item == null) {
            return new HashMap<>();
        }
        return item.serialize();
    }

    @Override
    protected ItemStack deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {
        return ItemStack.deserialize(map);
    }

    @Override
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        return ICrafting.class;
    }

    @Override
    public double getCraftingTime() {
        return craftingTime;
    }

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
    public Function<ItemStack, ItemStack> getExtra() {
        return x -> x;
    }
}
