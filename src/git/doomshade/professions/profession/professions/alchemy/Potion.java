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

import com.google.common.collect.ImmutableSet;
import git.doomshade.professions.Professions;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.io.IOManager;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

import static git.doomshade.professions.utils.Strings.PotionEnum.*;

public class Potion implements ConfigurationSerializable {

    private static final NamespacedKey NBT_KEY = new NamespacedKey(
            Professions.getInstance(),
            "profession_potion"
    );

    private static final HashSet<CustomPotionEffect> CUSTOM_POTION_EFFECTS = new HashSet<>();

    private static final String TEST_POTION_ID = "test_potion";
    public static final Potion EXAMPLE_POTION = new Potion(
            Arrays.asList("vyhybani", "poskozeni"),
            5,
            TEST_POTION_ID,
            PotionType.FIRE_RESISTANCE,
            ItemUtils.itemStackBuilder(Material.POTION).withDisplayName("&aSome bottle").build());

    static final HashSet<Potion> POTIONS = new HashSet<>();

    private final ArrayList<String> potionEffects = new ArrayList<>();
    private final int duration;
    private final String potionId;
    private final PotionType potionType;
    private final ItemStack potion;

    private Potion(Collection<String> potionEffects, int duration, String potionId, PotionType potionType,
                   ItemStack potion) {
        this.duration = duration;
        this.potionId = potionId;
        this.potionType = potionType;
        this.potionEffects.addAll(potionEffects);
        this.potion = potion;
        if (!potionId.equals(TEST_POTION_ID)) {
            POTIONS.add(this);
        }
    }

    private static File getFile(Player player) {
        return new File(IOManager.getCacheFolder(), player.getUniqueId().toString().concat(".bin"));
    }

    @Deprecated
    public static void cache(Player player) {
        /*HashSet<PotionTask> potionTasks = ACTIVE_POTIONS.get(player.getUniqueId());
        if (potionTasks == null || potionTasks.isEmpty()) {
            return;
        }
        File file = getFile(player);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            for (PotionTask task : potionTasks) {
                oos.writeObject(task);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    @Deprecated
    public static void loadFromCache(Player player) {
        /*File file = getFile(player);
        if (!file.exists()) {
            return;
        }
        HashSet<PotionTask> potionTasks = new HashSet<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            while (ois.available() > 0) {
                potionTasks.add((PotionTask) ois.readObject());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            ACTIVE_POTIONS.put(player.getUniqueId(), potionTasks);
        }*/
    }

    public static ImmutableSet<Potion> getPotions() {
        return ImmutableSet.copyOf(POTIONS);
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
        try {
            return Utils.findInIterable(POTIONS, x -> x.potionId.equals(potionId));
        } catch (Utils.SearchNotFoundException e) {
            ProfessionLogger.log(POTIONS);
            throw new IllegalStateException("Found " + potionId + " in-game, but its data is not loaded!");
        }
    }

    public static void registerCustomPotionEffect(CustomPotionEffect effect) {
        CUSTOM_POTION_EFFECTS.add(effect);
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

    @SuppressWarnings("all")
    static Potion deserialize(Map<String, Object> map) throws ProfessionObjectInitializationException {
        Set<String> missingKeys = Utils.getMissingKeys(map, Strings.PotionEnum.values());
        if (!missingKeys.isEmpty()) {
            throw new ProfessionObjectInitializationException(PotionItemType.class, missingKeys);
        }

        ArrayList<String> potionEffects = (ArrayList<String>) map.get(POTION_EFFECTS.s);
        int duration = (int) map.get(POTION_DURATION.s);
        String potionId = (String) map.get(POTION_FLAG.s);
        PotionType potionType = PotionType.valueOf((String) map.get(POTION_TYPE.s));
        MemorySection mem = (MemorySection) map.get(POTION.s);
        ItemStack potion = null;
        try {
            potion = ItemUtils.deserialize(mem.getValues(false));
        } catch (ConfigurationException | InitializationException e) {
            ProfessionLogger.logError(e, false);
            throw new ProfessionObjectInitializationException("Could not deserialize potion ItemStack from file.");
        }

        return new Potion(potionEffects, duration, potionId, potionType, potion);
    }

    ItemStack getItem() {
        return potion;
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
        return potionId.equals(potion.potionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(potionId);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return new HashMap<>() {
            {
                put(POTION_EFFECTS.s, potionEffects);
                put(POTION_DURATION.s, duration);
                put(POTION_FLAG.s, potionId);
                put(POTION_TYPE.s, potionType.name());
                put(POTION.s, ItemUtils.serialize(potion));
            }
        };
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
        pds.set(NBT_KEY, PersistentDataType.STRING, potionId);
        return Optional.of(clone);
    }

    @Override
    public String toString() {
        return "Potion{" +
                "potionEffects=" + potionEffects +
                ", duration=" + duration +
                ", potionId='" + potionId + '\'' +
                ", potionType=" + potionType +
                ", potion=" + potion +
                '}';
    }

    public String getPotionId() {
        return potionId;
    }

    int getDuration() {
        return duration;
    }

}
