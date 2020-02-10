package git.doomshade.professions.profession.professions.smelting;

import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.ICraftable;
import git.doomshade.professions.profession.types.ICrafting;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
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
    private Map<Sound, String> sounds = new HashMap<>();

    public BarItemType() {
        super();
    }

    public BarItemType(ItemStack object, int exp) {
        super(object, exp);
    }

    @Override
    public Map<String, Object> getSerializedObject() {
        return new HashMap<>();
    }

    @Override
    protected ItemStack deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {
        return null;
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
    public Map<Sound, String> getSounds() {
        return sounds;
    }

    @Override
    public void setSounds(Map<Sound, String> sounds) {
        this.sounds = sounds;
    }

    @Override
    public Function<ItemStack, ItemStack> getExtra() {
        return x -> x;
    }
}
