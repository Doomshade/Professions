package git.doomshade.professions.api.types;

import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.ProfessionInitializationException;
import git.doomshade.professions.api.user.UserProfessionData;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Requirements;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static git.doomshade.professions.utils.Strings.ICraftableEnum.*;

public abstract class CraftableItemType<T> extends ItemType<T> implements ICraftable {

    private double craftingTime = 5d;
    private ItemStack result = ItemUtils.EXAMPLE_RESULT;
    private Requirements craftingRequirements = new Requirements();
    private Map<Sound, String> sounds = new HashMap<>();

    /**
     * Constructor for creation of the item type object
     *
     * @param object
     */
    public CraftableItemType(T object) {
        super(object);
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> map = super.serialize();
        map.put(CRAFTABLE_ITEM_REQ.s, getCraftingRequirements().serialize());
        map.put(RESULT.s, ItemUtils.serialize(getResult()));
        map.put(CRAFTING_TIME.s, getCraftingTime());
        map.put(SOUND_CRAFTED.s, getSounds().get(Sound.ON_CRAFT));
        map.put(SOUND_CRAFTING.s, getSounds().get(Sound.CRAFTING));
        return map;
    }

    @Override
    public void deserialize(int id, Map<String, Object> map) throws ProfessionInitializationException {
        super.deserialize(id, map);
        setCraftingTime((double) map.getOrDefault(CRAFTING_TIME.s, 5d));

        setSounds(new HashMap<ICraftable.Sound, String>() {
            {
                put(Sound.CRAFTING, (String) map.getOrDefault(SOUND_CRAFTING.s, "block.fire.extinguish"));
                put(Sound.ON_CRAFT, (String) map.getOrDefault(SOUND_CRAFTED.s, "block.fire.ambient"));
            }
        });

        Set<String> list = Utils.getMissingKeys(map, Strings.ICraftableEnum.values());
        if (!list.isEmpty()) {
            throw new ProfessionInitializationException(getClass(), list);
        }

        MemorySection itemReqSection = (MemorySection) map.get(CRAFTABLE_ITEM_REQ.s);
        try {
            setCraftingRequirements(Requirements.deserialize(itemReqSection.getValues(false)));
        } catch (ConfigurationException e) {
            Professions.logError(e, false);
            throw new ProfessionInitializationException("Could not deserialize " + this);
        }

        MemorySection itemStackSection = (MemorySection) map.get(RESULT.s);
        try {
            setResult(ItemUtils.deserialize(itemStackSection.getValues(false)));
        } catch (ConfigurationException e) {
            Professions.logError(e, false);
            throw new ProfessionInitializationException("Could not deserialize a craftable item type");
        }
    }

    /**
     * Consumes crafting requirements from a player's inventory
     *
     * @param player the player to remove the items from
     */
    public void consumeCraftingRequirements(Player player) {
        getCraftingRequirements().consumeRequiredItems(player);
    }

    /**
     * Adds an additional crafting requirement for this item type
     *
     * @param item the crafting requirement to add
     */
    public void addCraftingRequirement(ItemStack item) {
        getCraftingRequirements().addRequirement(item);
    }


    /**
     * Determines whether or not the player meets crafting requirements
     *
     * @param player the player to check for
     * @return {@code true} if the player meets crafting requirements, {@code false} otherwise
     */
    @Override
    public boolean meetsRequirements(Player player) {
        return super.meetsRequirements(player) && getCraftingRequirements().meetsRequirements(player);
    }


    @Override
    public ItemStack getIcon(UserProfessionData upd) {
        final ItemStack icon = super.getIcon(upd);
        ItemMeta meta = icon.getItemMeta();
        List<String> lore = meta.getLore();


        Pattern regex = Pattern.compile("\\{" + CRAFTABLE_ITEM_REQ.s + "}");
        for (int i = 0; i < lore.size(); i++) {
            String s = lore.get(i);
            Matcher m = regex.matcher(s);
            if (!m.find()) {
                continue;
            }
            s = s.replaceAll(regex.pattern(),
                    getCraftingRequirements().toString(upd.getUser().getPlayer(), ChatColor.DARK_GREEN, ChatColor.RED));
            lore.set(i, s);
        }

        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    public String toStringFormat() {
        StringBuilder sb = new StringBuilder()
                .append("\ncrafting time: " + getCraftingTime())
                .append("\ncrafting result: " + getResult())
                .append("\ncrafting reqs: " + getCraftingRequirements())
                .append("\ninv reqs: " + getInventoryRequirements());
        return sb.toString();
    }

    @Override
    public final double getCraftingTime() {
        return craftingTime;
    }

    @Override
    public final void setCraftingTime(double craftingTime) {
        this.craftingTime = craftingTime;
    }

    @Override
    public final ItemStack getResult() {
        return result;
    }

    @Override
    public final void setResult(ItemStack result) {
        this.result = result;
    }


    @Override
    public final Requirements getCraftingRequirements() {
        return craftingRequirements;
    }

    @Override
    public final void setCraftingRequirements(Requirements craftingRequirements) {
        this.craftingRequirements = craftingRequirements;
    }

    @Override
    public final Map<Sound, String> getSounds() {
        return sounds;
    }

    @Override
    public final void setSounds(Map<Sound, String> sounds) {
        this.sounds = sounds;
    }

    public Function<ItemStack, ?> getExtraInEvent() {
        return null;
    }
}
