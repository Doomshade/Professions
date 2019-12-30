package git.doomshade.professions.profession.types.crafting.alchemy;

import git.doomshade.professions.exceptions.ProfessionInitializationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.types.ICraftable;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.crafting.ICrafting;
import git.doomshade.professions.utils.Requirements;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class PotionItemType extends ItemType<Potion> implements ICraftable {
    private double craftingTime = 0d;
    private ItemStack result = Potion.EXAMPLE_POTION.getItem();
    private Requirements inventoryRequirements = new Requirements();
    private Requirements craftingRequirements = new Requirements();
    private Map<Sound, String> sounds = new HashMap<>();


    public PotionItemType() {
        super();
    }

    public PotionItemType(Potion object, int exp) {
        super(object, exp);
    }

    @Override
    public Map<String, Object> getSerializedObject() {
        if (getObject() != null) {
            return getObject().serialize();
        }
        return new HashMap<>();
    }

    @Override
    protected Potion deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {
        Potion potion = Potion.deserialize(map);
        final Optional<ItemStack> potionItem = potion.getPotionItem(potion.getItem());
        potionItem.ifPresent(this::setResult);
        return potion;
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
    public Class<? extends IProfessionType> getDeclaredProfessionType() {
        return ICrafting.class;
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
    public Function<ItemStack, ItemStack> getExtra() {
        return itemStack -> itemStack;
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
    public void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Potion.cache(p);
        }
        Potion.ACTIVE_POTIONS.clear();
    }

    @Override
    public void onReload() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Potion.loadFromCache(p);
        }
    }
}
