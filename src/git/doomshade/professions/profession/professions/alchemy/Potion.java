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

package git.doomshade.professions.profession.professions.alchemy;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.spawn.ext.Element;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

import static git.doomshade.professions.utils.Strings.PotionEnum.*;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public final class Potion extends Element {

    public static final Potion EXAMPLE_POTION = new Potion(
            Arrays.asList("vyhybani", "poskozeni"),
            5,
            Utils.EXAMPLE_ID,
            PotionType.FIRE_RESISTANCE,
            ItemUtils.itemStackBuilder(Material.POTION).withDisplayName("&aSome bottle").build(),
            Utils.EXAMPLE_NAME);
    private static final NamespacedKey NBT_KEY = new NamespacedKey(
            Professions.getInstance(),
            "profession_potion"
    );
    private static final Set<CustomPotionEffect> CUSTOM_POTION_EFFECTS = new HashSet<>();
    private final List<String> potionEffects = new ArrayList<>();
    private final int duration;
    private final PotionType potionType;
    private final ItemStack potion;

    private Potion(Collection<String> potionEffects, int duration, String potionId, PotionType potionType,
                   ItemStack potion, String name) {
        super(potionId, name);
        this.duration = duration;
        this.potionType = potionType;
        this.potionEffects.addAll(potionEffects);
        this.potion = potion;
    }

    /**
     * Retrieves an instance from memory of the potion if it exists.
     *
     * @param potion the item stack to check for
     *
     * @return a potion if the item is valid, {@code null} otherwise
     */
    @Nullable
    public static Potion getItem(ItemStack potion) {
        if (potion == null || potion.getType() != Material.POTION || !(potion.getItemMeta() instanceof PotionMeta)) {
            return null;
        }
        final PotionMeta pm = (PotionMeta) potion.getItemMeta();
        final PersistentDataContainer pdc = pm.getPersistentDataContainer();

        if (!pdc.has(NBT_KEY, PersistentDataType.STRING)) {
            return null;
        }

        String potionId = pdc.get(NBT_KEY, PersistentDataType.STRING);
        return getElement(Potion.class, potionId);
    }

    public static void registerCustomPotionEffect(CustomPotionEffect effect) {
        CUSTOM_POTION_EFFECTS.add(effect);
    }

    @SuppressWarnings("all")
    public static Potion deserialize(Map<String, Object> map, final String name)
            throws ProfessionObjectInitializationException {
        Set<String> missingKeys = Utils.getMissingKeys(map, Strings.PotionEnum.values());
        if (!missingKeys.isEmpty()) {
            throw new ProfessionObjectInitializationException(PotionItemType.class, missingKeys);
        }

        ArrayList<String> potionEffects = (ArrayList<String>) map.get(POTION_EFFECTS.s);
        int duration = (int) map.get(POTION_DURATION.s);

        // fixme
        String potionId = (String) map.get("id");
        PotionType potionType = PotionType.valueOf((String) map.get(POTION_TYPE.s));
        MemorySection mem = (MemorySection) map.get(POTION.s);
        ItemStack potion = null;
        try {
            potion = ItemUtils.deserialize(mem.getValues(false));
        } catch (ConfigurationException | InitializationException e) {
            ProfessionLogger.logError(e, false);
            throw new ProfessionObjectInitializationException("Could not deserialize potion ItemStack from file.");
        }

        return new Potion(potionEffects, duration, potionId, potionType, potion, name);
    }

    void addAttributes(final Player player, final boolean negate) {
        //final PotionMeta itemMeta = (PotionMeta) potion.getItemMeta();
        //itemMeta.getCustomEffects().forEach(x -> x.getType().createEffect(duration, 0).apply(player));


        potionEffects.forEach(x -> {
            for (CustomPotionEffect potionEffect : CUSTOM_POTION_EFFECTS) {
                potionEffect.apply(x, player, negate);
            }
        });

    }

    ItemStack getItem() {
        return potion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Potion potion = (Potion) o;
        return getId().equals(potion.getId());
    }

    @Override
    public String toString() {
        return "Potion{" +
                "potionEffects=" + potionEffects +
                ", duration=" + duration +
                ", potionId='" + getId() + '\'' +
                ", potionType=" + potionType +
                ", potion=" + potion +
                '}';
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put(POTION_EFFECTS.s, potionEffects);
        map.put(POTION_DURATION.s, duration);
        map.put(POTION_TYPE.s, potionType.name());
        map.put(POTION.s, ItemUtils.serialize(potion));
        return map;
    }

    public Optional<ItemStack> getPotionItem(ItemStack item) {
        if (item == null || !(item.getItemMeta() instanceof PotionMeta) || item.getType() != Material.POTION) {
            return Optional.empty();
        }

        ItemStack clone = item.clone();
        if (!(clone.getItemMeta() instanceof PotionMeta)) {
            return Optional.empty();
        }

        PotionMeta meta = (PotionMeta) clone.getItemMeta();
        meta.setBasePotionData(new PotionData(potionType));
        clone.setItemMeta(meta);

        final PersistentDataContainer pds = meta.getPersistentDataContainer();
        pds.set(NBT_KEY, PersistentDataType.STRING, getId());
        return Optional.of(clone);
    }

    int getDuration() {
        return duration;
    }

}
