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

package git.doomshade.professions.utils;

import git.doomshade.professions.api.Range;
import git.doomshade.professions.api.item.ICraftable;
import git.doomshade.professions.api.item.ext.CraftableItemType;
import git.doomshade.professions.api.item.ext.ItemType;
import git.doomshade.professions.api.spawn.ext.Element;
import git.doomshade.professions.api.spawn.ext.Spawnable;
import git.doomshade.professions.enums.SortType;
import git.doomshade.professions.profession.professions.alchemy.Potion;
import git.doomshade.professions.profession.professions.herbalism.Herb;
import git.doomshade.professions.profession.professions.jewelcrafting.Gem;
import git.doomshade.professions.profession.professions.mining.Ore;
import git.doomshade.professions.profession.professions.skinning.Mob;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A class of {@code public static final} {@link String}s divided into enums for queries.
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public final class Strings {
    private static final Map<Class<? extends FileEnum>, FileEnum> REGISTERED_FILE_ENUMS = new HashMap<>();

    static {
        register(ICraftableEnum.CRAFTING_TIME);
        register(ItemTypeEnum.NAME);
        register(ElementEnum.ID);
        register(GemEnum.DISPLAY_NAME);
        register(HerbEnum.ENABLE_SPAWN);
        register(ItemTypeEnum.NAME);
        register(MarkableEnum.MARKER_VISIBLE);
        register(OreEnum.RESULT);
        register(PotionEnum.POTION_TYPE);
        register(PreyEnum.CONFIG_NAME);
        register(SpawnPointEnum.RESPAWN_TIME);
        register(SpawnableElementEnum.PARTICLE);
    }

    private Strings() {
    }

    private static void register(FileEnum e) {
        //ProfessionLogger.log(String.format("Registering FileEnum - Class: %s; FileEnum: %s", e.getClass(), e));
        REGISTERED_FILE_ENUMS.put(e.getClass(), e);
    }

    /**
     * Maps a default serialization to a serialization using {@link FileEnum}
     *
     * @param map     the map to remap
     * @param clazzez the {@link FileEnum} classes that are used to remap
     *
     * @return remapped default serialization
     */
    @SafeVarargs
    public static Map<FileEnum, Object> mapToFileEnum(Map<String, Object> map, Class<? extends FileEnum>... clazzez) {
        final Map<FileEnum, Object> m = new LinkedHashMap<>();

        for (Class<? extends FileEnum> clazz : clazzez) {
            final FileEnum fe = REGISTERED_FILE_ENUMS.get(clazz);

            // loop through all file enum values and check if the getKey() equals to the map entry key and assign it an
            // object from the map with the given key
            for (Map.Entry<String, Object> mapEntry : map.entrySet()) {
                for (Map.Entry<? extends FileEnum, Object> feEntry : fe.getDefaultValues().entrySet()) {
                    final FileEnum f = feEntry.getKey();
                    if (f.getKey().equals(mapEntry.getKey())) {
                        m.put(f, mapEntry.getValue());
                    }
                }
            }
        }
        return m;
    }

    /**
     * @param map   the map
     * @param enums the enums
     *
     * @return the missing keys of given map and enums
     */
    @SafeVarargs
    public static Set<String> getMissingKeysSet(Map<String, Object> map, Class<? extends FileEnum>... enums) {
        return toSet(getMissingKeys(map, enums));
    }

    /**
     * Transforms a map to a set of missing keys
     *
     * @param map the map to transform to
     *
     * @return a set of missing keys
     */
    public static Set<String> toSet(Map<FileEnum, Object> map) {
        return map.keySet().stream().map(FileEnum::getKey).collect(Collectors.toSet());
    }

    /**
     * @param map   the map
     * @param enums the enums
     *
     * @return the missing keys of given map and enums
     */
    @SafeVarargs
    public static Map<FileEnum, Object> getMissingKeys(Map<String, Object> map, Class<? extends FileEnum>... enums) {
        Map<FileEnum, Object> m = new HashMap<>();
        for (Class<? extends FileEnum> clazz : enums) {
            putDefaults(map, m, REGISTERED_FILE_ENUMS.get(clazz));
        }
        return m;
    }

    /**
     * Puts the defaults from the {@link FileEnum} to the "defaults" map based on the "map" map
     *
     * @param map      the map to check for
     * @param defaults the map to put the defaults to
     * @param fe       the defaults to retrieve from
     */
    private static void putDefaults(Map<String, Object> map, Map<FileEnum, Object> defaults, FileEnum fe) {
        if (fe == null) {
            return;
        }
        for (Map.Entry<? extends FileEnum, Object> e : fe.getDefaultValues().entrySet()) {
            final FileEnum fee = e.getKey();
            if (!map.containsKey(fee.getKey())) {
                defaults.put(e.getKey(), e.getValue());
            }
        }
    }

    public static Map<FileEnum, Object> getMissingKeysItemType(Map<String, Object> map, ItemType<?> itemType) {
        return getMissingKeys(map, x -> x.testItemType(itemType));
    }

    private static Map<FileEnum, Object> getMissingKeys(Map<String, Object> map, Predicate<FileEnum> p) {
        Map<FileEnum, Object> enums = new HashMap<>();

        // for each registered file enum
        for (FileEnum fe : REGISTERED_FILE_ENUMS.values()) {
            if (!p.test(fe)) {
                continue;
            }

            // iterate through its enum values
            putDefaults(map, enums, fe);
        }
        return enums;
    }

    public static Map<FileEnum, Object> getMissingKeysObject(Map<String, Object> map,
                                                             ConfigurationSerializable object) {
        return getMissingKeys(map, x -> x.testObject(object));
    }

    /**
     * The enum for {@link ICraftable}
     */
    public enum ICraftableEnum implements FileEnum {
        CRAFTABLE_ITEM_REQ("item-requirements"),
        RESULT("result"),
        CRAFTING_TIME("crafting-time"),
        SOUND_CRAFTING("crafting-sound"),
        SOUND_CRAFTED("crafted-sound");

        private static final double DEFAULT_CRAFTING_TIME = 5d;
        public final String s;

        ICraftableEnum(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }

        @Override
        public boolean testItemType(ItemType<?> itemType) {
            return itemType instanceof CraftableItemType;
        }

        @Override
        public String getKey() {
            return s;
        }

        @Override
        public EnumMap<ICraftableEnum, Object> getDefaultValues() {
            return new EnumMap<>(ICraftableEnum.class) {
                {
                    put(CRAFTABLE_ITEM_REQ,
                            new Requirements(Collections.singletonList(ItemUtils.EXAMPLE_REQUIREMENT)).serialize());
                    put(RESULT, ItemUtils.EXAMPLE_RESULT.serialize());
                    put(CRAFTING_TIME, DEFAULT_CRAFTING_TIME);
                    put(SOUND_CRAFTING, "block.fire.ambient");
                    put(SOUND_CRAFTED, "block.fire.extinguish");
                }
            };
        }
    }

    /**
     * The enum for {@link ItemType}
     */
    public enum ItemTypeEnum implements FileEnum {
        LEVEL_REQ("level-req"),
        EXP("exp"),
        OBJECT("object"),
        NAME("name"),
        DESCRIPTION("description"),
        MATERIAL("gui-material"),
        RESTRICTED_WORLDS("restricted-worlds"),
        LEVEL_REQ_COLOR("level-req-color"),
        IGNORE_SKILLUP_COLOR("ignore-skillup-color"),
        TRAINABLE("trainable"),
        TRAINABLE_COST("trainable-cost"),
        INVENTORY_REQUIREMENTS("inventory-requirements");

        public final String s;

        ItemTypeEnum(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }

        @Override
        public boolean testItemType(ItemType<?> itemType) {
            return itemType != null;
        }

        @Override
        public String getKey() {
            return s;
        }

        @Override
        public EnumMap<ItemTypeEnum, Object> getDefaultValues() {
            return new EnumMap<>(ItemTypeEnum.class) {
                {
                    put(LEVEL_REQ, 0);
                    put(EXP, 0);
                    put(NAME, "&eNO_NAME");
                    put(DESCRIPTION, Arrays.asList("&aThe", "&bDescription"));
                    put(MATERIAL, Material.CHEST.toString());
                    put(RESTRICTED_WORLDS, Arrays.asList("some_world", "some_other_world"));
                    put(INVENTORY_REQUIREMENTS,
                            new Requirements(Collections.singletonList(ItemUtils.EXAMPLE_REQUIREMENT)).serialize());
                    put(IGNORE_SKILLUP_COLOR, true);
                    put(TRAINABLE, false);
                    put(TRAINABLE_COST, 0);
                }
            };
        }
    }

    public enum ElementEnum implements FileEnum {
        ID("id");

        public final String s;

        ElementEnum(String s) {
            this.s = s;
        }

        @Override
        public boolean testObject(ConfigurationSerializable object) {
            return object instanceof Element;
        }

        @Override
        public String getKey() {
            return s;
        }

        @Override
        public EnumMap<ElementEnum, Object> getDefaultValues() {
            return new EnumMap<>(ElementEnum.class) {
                {
                    put(ID, Utils.EXAMPLE_ID);
                }
            };
        }
    }

    public enum GemEnum implements FileEnum {
        GEM_EFFECT("gem-effect"),
        GEM_EFFECT_CONTEXT("gem-effect-context"),
        GEM("item"),
        DISPLAY_NAME("gem-name"),
        EQUIPMENT_SLOT("equipment-slot");

        public final String s;

        GemEnum(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }

        @Override
        public boolean testObject(ConfigurationSerializable object) {
            return object instanceof Gem;
        }

        @Override
        public String getKey() {
            return s;
        }

        @Override
        public EnumMap<GemEnum, Object> getDefaultValues() {
            return new EnumMap<>(GemEnum.class) {
                {
                    put(GEM_EFFECT, "add");
                    put(GEM_EFFECT_CONTEXT, Arrays.asList("poskozeni:5", "inteligence:4"));
                    put(GEM, ItemUtils.EXAMPLE_RESULT.serialize());
                    put(DISPLAY_NAME, "&cNejhorší gem I");
                    put(EQUIPMENT_SLOT, Gem.GemEquipmentSlot.MAINHAND.name());
                }
            };
        }
    }

    public enum HerbEnum implements FileEnum {
        GATHER_ITEM("gather-item"),
        ENABLE_SPAWN("enable-spawn"),
        TIME_GATHER("gather-duration");

        private static final int DEFAULT_GATHER_TIME = 5;
        public final String s;

        HerbEnum(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }

        @Override
        public boolean testObject(ConfigurationSerializable object) {
            return object instanceof Herb;
        }

        @Override
        public String getKey() {
            return s;
        }

        @Override
        public EnumMap<HerbEnum, Object> getDefaultValues() {
            return new EnumMap<>(HerbEnum.class) {
                {
                    put(GATHER_ITEM, ItemUtils.EXAMPLE_RESULT.serialize());
                    put(ENABLE_SPAWN, false);
                    put(TIME_GATHER, DEFAULT_GATHER_TIME);
                }
            };
        }
    }

    public enum ItemTypeHolderEnum implements FileEnum {
        ERROR_MESSAGE("error-message"),
        SORTED_BY("sorted-by"),
        NEW_ITEMS_AVAILABLE_MESSAGE("new-items-available-message");

        public final String s;

        ItemTypeHolderEnum(String s) {
            this.s = s;
        }

        @Override
        public String getKey() {
            return s;
        }

        @Override
        public EnumMap<ItemTypeHolderEnum, Object> getDefaultValues() {
            return new EnumMap<>(ItemTypeHolderEnum.class) {
                {
                    put(ERROR_MESSAGE, Arrays.asList("some", "error msg"));
                    put(SORTED_BY, Arrays.asList(SortType.values()));
                    put(NEW_ITEMS_AVAILABLE_MESSAGE, Arrays.asList("some", "new items message"));
                }
            };
        }
    }

    public enum MarkableEnum implements FileEnum {
        MARKER_SET_ID("dynmap-marker"),
        MARKER_VISIBLE("marker-visible");

        public final String s;

        MarkableEnum(String s) {
            this.s = s;
        }

        @Override
        public boolean testItemType(ItemType<?> itemType) {
            return itemType != null;
        }

        @Override
        public String getKey() {
            return s;
        }

        @Override
        public EnumMap<MarkableEnum, Object> getDefaultValues() {
            return new EnumMap<>(MarkableEnum.class) {
                {
                    put(MARKER_SET_ID, "some-marker");
                    put(MARKER_VISIBLE, false);
                }
            };
        }
    }

    /**
     * Enum for keys in file
     */
    public enum OreEnum implements FileEnum {
        RESULT("drop");

        public final String s;

        OreEnum(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }

        @Override
        public boolean testObject(ConfigurationSerializable object) {
            return object instanceof Ore;
        }

        @Override
        public String getKey() {
            return s;
        }

        @Override
        public EnumMap<OreEnum, Object> getDefaultValues() {
            return new EnumMap<>(OreEnum.class) {
                {
                    //put(RESULT, new YieldResult(40d, ItemUtils.EXAMPLE_RESULT).serialize());
                }
            };
        }
    }

    public enum PotionEnum implements FileEnum {
        POTION_EFFECTS("potion-effects"),
        POTION_DURATION("duration"),
        POTION_TYPE("potion-type"),
        POTION("potion");

        private static final String SPLIT_CHAR = ":";
        private static final int DEFAULT_POTION_DURATION = 80;
        public final String s;

        PotionEnum(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }

        @Override
        public boolean testObject(ConfigurationSerializable object) {
            return object instanceof Potion;
        }

        @Override
        public String getKey() {
            return s;

        }

        @Override
        public EnumMap<PotionEnum, Object> getDefaultValues() {
            return new EnumMap<>(PotionEnum.class) {
                {
                    put(POTION_EFFECTS, Arrays.asList(
                            String.format("kriticky_utok%s10", SPLIT_CHAR),
                            String.format("vyhybani%s5", SPLIT_CHAR),
                            String.format("sance_na_kriticky_zasah%s80", SPLIT_CHAR),
                            String.format("poskozeni%s40", SPLIT_CHAR),
                            String.format("zivoty%s30", SPLIT_CHAR)));
                    put(POTION_DURATION, DEFAULT_POTION_DURATION);
                    put(POTION_TYPE, PotionType.FIRE_RESISTANCE.name());
                    put(POTION, Potion.EXAMPLE_POTION.serialize());
                }
            };
        }
    }

    public enum PreyEnum implements FileEnum {
        ENTITY("entity"),
        CONFIG_NAME("config-name");

        public final String s;

        PreyEnum(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }

        @Override
        public boolean testObject(ConfigurationSerializable object) {
            return object instanceof Mob;
        }

        @Override
        public String getKey() {
            return s;
        }

        @Override
        public EnumMap<PreyEnum, Object> getDefaultValues() {
            return new EnumMap<>(PreyEnum.class) {
                {
                    put(ENTITY, EntityType.SKELETON.name());
                    put(CONFIG_NAME, "cfg-name");
                }
            };
        }
    }

    public enum SpawnPointEnum implements FileEnum {
        LOCATION("location"),
        RESPAWN_TIME("respawn-time");

        public final String s;

        SpawnPointEnum(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }

        @Override
        public String getKey() {
            return s;
        }

        @Override
        public EnumMap<SpawnPointEnum, Object> getDefaultValues() {
            return new EnumMap<>(SpawnPointEnum.class) {
                {
                    put(LOCATION, ItemUtils.EXAMPLE_LOCATION.serialize());
                    put(RESPAWN_TIME, new Range(0).serialize());
                }
            };
        }
    }

    public enum SpawnableElementEnum implements FileEnum {
        SPAWN_POINT("spawnpoint"),
        MATERIAL("material"),
        PARTICLE("particle");

        public final String s;

        SpawnableElementEnum(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }

        @Override
        public boolean testObject(ConfigurationSerializable object) {
            return object instanceof Spawnable;
        }

        @Override
        public String getKey() {
            return s;
        }

        @Override
        public EnumMap<SpawnableElementEnum, Object> getDefaultValues() {
            return new EnumMap<>(SpawnableElementEnum.class) {
                {
                    //put(SPAWN_POINT, SpawnPoint.EXAMPLE.serialize());
                    put(MATERIAL, ItemUtils.EXAMPLE_RESULT.getType().toString());
                    put(PARTICLE, new ParticleData().serialize());
                }
            };
        }
    }

    public enum BSEnum implements FileEnum {
        ITEM("item");

        public final String s;

        BSEnum(String s) {
            this.s = s;
        }

        @Override
        public String getKey() {
            return s;
        }

        @Override
        public EnumMap<BSEnum, Object> getDefaultValues() {
            return new EnumMap<>(BSEnum.class) {
                {
                    put(ITEM, ItemUtils.EXAMPLE_RESULT.serialize());
                }
            };
        }
    }

    public enum BarEnum implements FileEnum {
        ITEM("item");

        public final String s;

        BarEnum(String s) {
            this.s = s;
        }

        @Override
        public String getKey() {
            return s;
        }

        @Override
        public EnumMap<BarEnum, Object> getDefaultValues() {
            return new EnumMap<>(BarEnum.class) {
                {
                    put(ITEM, ItemUtils.EXAMPLE_RESULT.serialize());
                }
            };
        }
    }
}
