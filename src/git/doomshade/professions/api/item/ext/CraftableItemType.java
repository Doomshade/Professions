/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jakub Šmrha
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package git.doomshade.professions.api.item.ext;

import com.google.common.collect.ImmutableMap;
import git.doomshade.professions.api.item.ICraftable;
import git.doomshade.professions.api.user.IUserProfessionData;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.exceptions.ProfessionInitializationException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Requirements;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import static git.doomshade.professions.utils.Strings.ICraftableEnum.*;

/**
 * An {@link ItemType} that allows for crafting an ItemStack
 *
 * @param <T> the item type to look for in {@link ProfessionEvent}
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public abstract class CraftableItemType<T extends ConfigurationSerializable> extends ItemType<T> implements ICraftable {

    private double craftingTime = 5d;
    private ItemStack result = ItemUtils.EXAMPLE_RESULT;
    private Requirements craftingRequirements = new Requirements();
    private Map<Sound, String> sounds = new HashMap<>();

    /**
     * Constructor for creation of the item type object
     *
     * @param object the object
     */
    public CraftableItemType(T object) {
        super(object);
    }

    @Override
    public void deserialize(int id, Map<String, Object> map) throws InitializationException {
        super.deserialize(id, map);
        this.craftingTime = (double) map.getOrDefault(CRAFTING_TIME.s, 5d);

        this.sounds = new HashMap<>() {
            {
                put(Sound.CRAFTING, (String) map.getOrDefault(SOUND_CRAFTING.s, "block.fire.extinguish"));
                put(Sound.ON_CRAFT, (String) map.getOrDefault(SOUND_CRAFTED.s, "block.fire.ambient"));
            }
        };

        Set<String> list = Utils.getMissingKeys(map, Strings.ICraftableEnum.values());
        if (!list.isEmpty()) {
            throw new ProfessionInitializationException(getClass(), list);
        }

        MemorySection itemReqSection = (MemorySection) map.get(CRAFTABLE_ITEM_REQ.s);
        try {
            setCraftingRequirements(Requirements.deserialize(itemReqSection.getValues(false)));
        } catch (ConfigurationException e) {
            ProfessionLogger.logError(e, false);
            throw new ProfessionInitializationException("Could not deserialize " + this);
        }

        MemorySection isSection = (MemorySection) map.get(RESULT.s);
        try {
            setResult(ItemUtils.deserialize(isSection.getValues(false)));
        } catch (ConfigurationException e) {
            ProfessionLogger.logError(e, false);
            throw new ProfessionInitializationException("Could not deserialize a craftable item type");
        }
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        final Map<String, Object> map = super.serialize();
        map.put(CRAFTABLE_ITEM_REQ.s, getCraftingRequirements().serialize());
        map.put(RESULT.s, ItemUtils.serialize(getResult()));
        map.put(CRAFTING_TIME.s, getCraftingTime());
        map.put(SOUND_CRAFTED.s, getSounds().get(Sound.ON_CRAFT));
        map.put(SOUND_CRAFTING.s, getSounds().get(Sound.CRAFTING));
        return map;
    }

    @Override
    public ItemStack getIcon(IUserProfessionData upd) {
        final ItemStack icon = super.getIcon(upd);
        ItemMeta meta = icon.getItemMeta();

        if (meta == null) {
            return icon;
        }

        List<String> lore = meta.getLore();

        if (lore == null) {
            return icon;
        }

        Pattern regex = Pattern.compile("\\{" + CRAFTABLE_ITEM_REQ.s + "}");

        super.updateLore(upd, lore, regex, getCraftingRequirements());

        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    @Override
    public String toString() {
        return "\ncrafting time: " +
                getCraftingTime() +
                "\ncrafting result: " +
                getResult() +
                "\ncrafting reqs: " +
                getCraftingRequirements() +
                "\ninv reqs: " +
                getInventoryRequirements();
    }

    /**
     * Determines whether or not the player meets crafting requirements
     *
     * @param player the player to check for
     *
     * @return {@code true} if the player meets crafting requirements, {@code false} otherwise
     */
    @Override
    public boolean meetsRequirements(Player player) {
        return super.meetsRequirements(player) && getCraftingRequirements().meetsRequirements(player);
    }

    @Override
    public final double getCraftingTime() {
        return craftingTime;
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

    private void setCraftingRequirements(Requirements craftingRequirements) {
        this.craftingRequirements = craftingRequirements;
    }

    @Override
    public final Map<Sound, String> getSounds() {
        return ImmutableMap.copyOf(sounds);
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
     * Adds a crafting requirement for this item type
     *
     * @param item the crafting requirement to add
     */
    public void addCraftingRequirement(ItemStack item) {
        getCraftingRequirements().addRequirement(item);
    }

    public Function<ItemStack, ?> getExtraInEvent() {
        return null;
    }
}
