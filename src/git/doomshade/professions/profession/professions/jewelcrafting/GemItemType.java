package git.doomshade.professions.profession.professions.jewelcrafting;

import com.google.common.collect.Maps;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.ICraftable;
import git.doomshade.professions.profession.ITrainable;
import git.doomshade.professions.profession.types.ICrafting;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Requirements;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class GemItemType extends ItemType<Gem> implements ICraftable, ITrainable {
    private double craftingTime = 0d;
    private ItemStack result = ItemUtils.EXAMPLE_RESULT;
    private Requirements inventoryRequirements = new Requirements();
    private Requirements craftingRequirements = new Requirements();
    private Map<Sound, String> sounds = new HashMap<>();
    private String trainableId;
    private boolean trainable = true;
    private int cost = 0;

    /**
     * Constructor for creation of the item type object
     *
     * @param object
     */
    public GemItemType(Gem object) {
        super(object);
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
    public String getTrainableId() {
        if (trainableId == null) {
            if (getObject() == null) {
                throw new IllegalStateException("Both trainable id and object for " + this + " is null, cannot get a trainable id!");
            }
            trainableId = getObject().getClass().getSimpleName();
        }
        return trainableId;
    }

    @Override
    public void setTrainableId(String trainableId) {
        this.trainableId = trainableId;
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

    @Override
    public void setCost(int cost) {
        this.cost = cost;
    }

    @Override
    public Map<String, Object> getSerializedObject() {

        final Gem object = getObject();
        if (object == null) {
            return Maps.newHashMap();
        }

        return object.serialize();
    }

    @Override
    protected Gem deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {
        return Gem.deserialize(map);
    }

    @Override
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        return ICrafting.class;
    }

    @Override
    public void onDisable() {
        Gem.GEMS.clear();
    }
}
