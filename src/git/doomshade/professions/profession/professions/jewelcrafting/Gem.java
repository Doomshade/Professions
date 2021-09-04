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

package git.doomshade.professions.profession.professions.jewelcrafting;

import com.google.common.collect.Sets;
import git.doomshade.professions.Professions;
import git.doomshade.professions.api.item.object.Element;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static git.doomshade.professions.profession.professions.jewelcrafting.GemUtils.*;
import static git.doomshade.professions.utils.Strings.GemEnum.*;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public final class Gem extends Element {

    // TODO settings
    public static final String EMPTY_GEM = "(Místo pro klenot)";

    // stay here cuz of order!
    public static final Gem EXAMPLE_GEM = new Gem(
            Utils.EXAMPLE_ID,
            ItemUtils.EXAMPLE_RESULT,
            GemUtils.ADD_ATTRIBUTE_EFFECT,
            Utils.EXAMPLE_NAME,
            Arrays.asList("poskozeni:50", "vitalita:30"),
            GemEquipmentSlot.ARMOR
    );

    private static final Map<UUID, Set<Gem>> ACTIVE_GEMS = new HashMap<>();
    private static final Set<String> LOGGED_ERROR_GEMS = new HashSet<>();
    private final List<String> context;
    private final ItemStack gem;
    private final GemEffect gemEffect;
    private final GemEquipmentSlot equipmentSlot;
    //private static final InsertResult INVALID_ITEM_RESULT = new InsertResult(null, ResultEnum.INVALID_ITEM);
    //private static final InsertResult NO_GEM_SPACE_RESULT = new InsertResult(null, ResultEnum.NO_GEM_SPACE);


    private Gem(String id, ItemStack gem, GemEffect gemEffect, String displayName, List<String> context,
                GemEquipmentSlot equipmentSlot) throws IllegalArgumentException {
        super(id, displayName, true);

        this.equipmentSlot = equipmentSlot;
        if (equipmentSlot == null) {
            throw new IllegalArgumentException(
                    "Invalid equipment slot. Valid equipment slots: " + Arrays.toString(GemEquipmentSlot.values()));
        }
        this.context = context;
        this.gem = gem;
        addNbtTag(gem, GEM_NBT_TAG);
        this.gemEffect = gemEffect;
    }

    private void addNbtTag(ItemStack gem, String key) {
        addNbtTag(gem, key, this.getId());
    }

    private void addNbtTag(ItemStack gem, String key, String id) {
        if (gem.getItemMeta() == null) {
            return;
        }
        PersistentDataContainer pdc = gem.getItemMeta().getPersistentDataContainer();
        NamespacedKey nmsk = new NamespacedKey(Professions.getInstance(), key);
        pdc.set(nmsk, PersistentDataType.STRING, id);
    }

    /**
     * Checks for the NBT Tag of the ItemStack. If found, returns the gem
     *
     * @param item the item to check for
     *
     * @return the gem if it actually is one
     */
    public static Optional<Gem> getGem(ItemStack item) {
        return getGem(item, GEM_NBT_TAG);
    }

    private static Optional<Gem> getGem(ItemStack item, String tag) {
        final Optional<Gem> empty = Optional.empty();

        if (item == null || item.getType() == Material.AIR || item.getItemMeta() == null) {
            return empty;
        }

        final PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();

        NamespacedKey key = new NamespacedKey(Professions.getInstance(), tag);
        if (!pdc.has(key, PersistentDataType.STRING)) {
            return empty;
        }

        final String id = pdc.get(key, PersistentDataType.STRING);
        final Gem value = getElement(Gem.class, id);
        if (value == null) {
            if (LOGGED_ERROR_GEMS.add(id)) {
                final String s = "Found a gem but the gem is not registered! (" + id + ")";
                ProfessionLogger.log(s, Level.WARNING);
                ProfessionLogger.log(s, Level.CONFIG);
            }
            return empty;
        }

        return Optional.of(value);
    }

    public static void update(Player player) {

        Set<Gem> gems = new HashSet<>();

        final PlayerInventory inventory = player.getInventory();
        for (ItemStack item : inventory.getArmorContents()) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            if (hasGem(item)) {
                final Optional<Gem> opt = getGemInItem(item);
                opt.ifPresent(gems::add);
            }
        }

        for (ItemStack item : getScannedItems(inventory)) {
            if (item == null) {
                continue;
            }
            if (hasGem(item)) {
                final Optional<Gem> opt = getGemInItem(item);
                opt.ifPresent(gems::add);
            }
        }

        // apply new gems and unapply the ones that are no longer active
        Set<Gem> activeGems = ACTIVE_GEMS.getOrDefault(player.getUniqueId(), new HashSet<>());
        final Sets.SetView<Gem> difference1 = Sets.difference(gems, activeGems);
        difference1.forEach(x -> x.apply(player));
        final Sets.SetView<Gem> difference2 = Sets.difference(activeGems, gems);
        difference2.forEach(x -> x.unApply(player));
    }

    public static Optional<Gem> getGemInItem(ItemStack item) {
        return getGem(item, ACTIVE_GEM_NBT_TAG);
    }

    public static boolean hasGem(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return false;
        }
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(Professions.getInstance(), ACTIVE_GEM_NBT_TAG);

        return pdc.has(key, PersistentDataType.STRING);
    }

    @NotNull
    private static Iterable<ItemStack> getScannedItems(PlayerInventory inventory) {
        // TODO add this to config
        List<ItemStack> scannedItems = new ArrayList<>(Arrays.asList(
                inventory.getItemInMainHand(),
                inventory.getItemInOffHand(),
                inventory.getItem(9),
                inventory.getItem(10),
                inventory.getItem(11)
        )
        );

        scannedItems.addAll(Arrays.stream(inventory.getArmorContents()).collect(Collectors.toList()));
        return scannedItems;
    }

    public void apply(Player player) {
        final UUID uniqueId = player.getUniqueId();
        Set<Gem> gems = ACTIVE_GEMS.getOrDefault(uniqueId, new HashSet<>());
        if (gems.add(this)) {
            gemEffect.apply(this, player, false);
            ACTIVE_GEMS.put(uniqueId, gems);
        }
    }

    public void unApply(Player player) {
        final UUID uniqueId = player.getUniqueId();
        Set<Gem> gems = ACTIVE_GEMS.getOrDefault(uniqueId, new HashSet<>());
        if (gems.remove(this)) {
            gemEffect.apply(this, player, true);
            ACTIVE_GEMS.put(uniqueId, gems);
        }
    }

    public static void unApplyAll(Player player) {
        ACTIVE_GEMS.getOrDefault(player.getUniqueId(), new HashSet<>()).forEach(x -> x.unApply(player));
    }

    @SuppressWarnings("all")
    public static Gem deserialize(Map<String, Object> map) throws ProfessionObjectInitializationException {
        final Set<FileEnum> missingKeysEnum = Utils.getMissingKeysEnum(map, values());

        if (!missingKeysEnum.isEmpty()) {
            throw new ProfessionObjectInitializationException(GemItemType.class,
                    missingKeysEnum.stream().map(Object::toString).collect(Collectors.toSet()));
        }
        GemEquipmentSlot equipmentSlot;
        try {
            equipmentSlot = GemEquipmentSlot.valueOf((String) map.get(EQUIPMENT_SLOT.s));
        } catch (IllegalArgumentException e) {
            ProfessionLogger.log("Available equipment slots: " +
                    Arrays.stream(GemEquipmentSlot.values()).map(Enum::name).collect(Collectors.joining(" ")));
            throw new ProfessionObjectInitializationException(GemItemType.class,
                    Collections.singletonList(EQUIPMENT_SLOT.s),
                    ProfessionObjectInitializationException.ExceptionReason.KEY_ERROR);
        }

        // fixme
        String id = (String) map.get("id");
        String gemEffect = (String) map.get(GEM_EFFECT.s);
        MemorySection itemSection = (MemorySection) map.get(GEM.s);
        ItemStack item = null;
        try {
            item = ItemUtils.deserialize(itemSection.getValues(false));
        } catch (ConfigurationException | InitializationException e) {
            ProfessionLogger.logError(e, false);
            throw new ProfessionObjectInitializationException("Could not deserialize gem ItemStack from file");
        }
        String displayName = (String) map.get(DISPLAY_NAME.s);
        displayName = displayName == null || displayName.isEmpty() ?
                displayName :
                ChatColor.translateAlternateColorCodes('&', displayName);

        List<String> context = (List<String>) map.get(GEM_EFFECT_CONTEXT.s);
        return new Gem(id, item, IDS.get(gemEffect), displayName, context, equipmentSlot);
    }

    public ItemStack getGem() {
        return gem;
    }

    public List<String> getContext() {
        return context;
    }

    public boolean isActive(Player player) {
        return ACTIVE_GEMS.getOrDefault(player.getUniqueId(), new HashSet<>()).contains(this);
    }

    public InsertResult insert(ItemStack item) {
        return insert(item, false);
    }

    public InsertResult insert(ItemStack item, boolean ignoreSpace) {

        if (item == null) {
            return InsertResult.INVALID_ITEM;
        }

        // firstly check whether or not the item has lore
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return InsertResult.INVALID_ITEM;
        }

        if (hasGem(item)) {
            return InsertResult.NO_GEM_SPACE;
        }

        final List<String> lore = meta.getLore();
        final List<String> loreCopy = new ArrayList<>(Objects.requireNonNull(lore))
                .stream()
                .map(ChatColor::stripColor)
                .collect(Collectors.toList());
        int idx = loreCopy.indexOf(EMPTY_GEM);

        // look for the gem slot, return false if not found
        /*AtomicInteger index = new AtomicInteger(-1);
        try {
            Utils.findInIterable(lore, new Predicate<String>() {

                int i = 0;

                @Override
                public boolean test(String x) {
                    boolean found = !x.isEmpty() && ChatColor.stripColor(x).equalsIgnoreCase(EMPTY_GEM);

                    if (found) {
                        index.set(i);
                    } else {
                        i++;
                    }

                    return found;
                }
            });
        } catch (Utils.SearchNotFoundException e) {
            if (!ignoreSpace) {
                return NO_GEM_SPACE_RESULT;
            }
        }*/

        if (idx != -1) {
            lore.set(idx, getName());
            meta.setLore(lore);
            item.setItemMeta(meta);
        } else if (!ignoreSpace) {
            return InsertResult.NO_GEM_SPACE;
        }

        // gem slot found, time to add NBT tag and replace the gem line

        addNbtTag(item, ACTIVE_GEM_NBT_TAG);
        return InsertResult.SUCCESS;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return new LinkedHashMap<>() {
            {
                put(GEM_EFFECT.s, gemEffect.toString());
                put(GEM_EFFECT_CONTEXT.s, context);
                put(GEM.s, ItemUtils.serialize(gem));
                put(EQUIPMENT_SLOT.s, equipmentSlot.name());
            }
        };
    }

    @Override
    public int hashCode() {
        return Objects.hash(gem, gemEffect, getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Gem gem1 = (Gem) o;
        return gem.equals(gem1.gem) &&
                gemEffect.equals(gem1.gemEffect) &&
                getId().equals(gem1.getId());
    }

    @Override
    public String toString() {
        return "Gem{" +
                "gemEffect=" + gemEffect +
                ", id='" + getId() + '\'' +
                ", equipmentSlot=" + equipmentSlot +
                ", displayName='" + getName() + '\'' +
                '}';
    }

    public enum InsertResult {
        INVALID_ITEM,
        NO_GEM_SPACE,
        SUCCESS
    }

    public enum GemEquipmentSlot {
        MAINHAND,
        OFFHAND,
        ARMOR
    }
}
