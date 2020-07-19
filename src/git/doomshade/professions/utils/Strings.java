package git.doomshade.professions.utils;

import git.doomshade.professions.profession.ICraftable;
import git.doomshade.professions.profession.ITrainable;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;

/**
 * A class of {@code public static final} {@link String}s divided into enums for queries.
 *
 * @author Doomshade
 */
public final class Strings {

    /**
     * The enum for {@link ITrainable}.
     */
    public enum ITrainableEnum implements FileEnum {
        TRAINABLE("trainable"), COST("trainable-cost"), TRAINABLE_ID("trainable-id"), VAR_TRAINABLE_COST("\\{trainable-cost\\}");

        public final String s;

        ITrainableEnum(String s) {
            this.s = s;
        }

        @Override
        public EnumMap<ITrainableEnum, Object> getDefaultValues() {
            return new EnumMap<ITrainableEnum, Object>(ITrainableEnum.class) {
                {
                    put(TRAINABLE, true);
                    put(COST, 0);
                    put(TRAINABLE_ID, "some_id");

                }
            };
        }

        @Override
        public String toString() {
            return s;
        }
    }

    /**
     * The enum for {@link ICraftable}
     */
    public enum ICraftableEnum implements FileEnum {
        ITEM_REQUIREMENTS("item-requirements"),
        RESULT("result"),
        CRAFTING_TIME("crafting-time"),
        INVENTORY_REQUIREMENTS("inventory-requirements"),
        SOUND_CRAFTING("crafting-sound"),
        SOUND_CRAFTED("crafted-sound");

        public final String s;

        ICraftableEnum(String s) {
            this.s = s;
        }


        @Override
        public EnumMap<ICraftableEnum, Object> getDefaultValues() {
            return new EnumMap<ICraftableEnum, Object>(ICraftableEnum.class) {
                {
                    put(ITEM_REQUIREMENTS, new Requirements(Collections.singletonList(ItemUtils.EXAMPLE_REQUIREMENT)).serialize());
                    put(RESULT, ItemUtils.EXAMPLE_RESULT.serialize());
                    put(CRAFTING_TIME, 5d);
                    put(INVENTORY_REQUIREMENTS, new Requirements(Collections.singletonList(ItemUtils.EXAMPLE_REQUIREMENT)).serialize());
                    put(SOUND_CRAFTING, "block.fire.ambient");
                    put(SOUND_CRAFTED, "block.fire.extinguish");
                }
            };
        }

        @Override
        public String toString() {
            return s;
        }
    }

    /**
     * The enum for {@link git.doomshade.professions.profession.types.ItemType}
     */
    public enum ItemTypeEnum implements FileEnum {
        LEVEL_REQ("level-req"),
        PROFTYPE("type-unchangable"),
        EXP("exp"),
        OBJECT("object"),
        NAME("name"),
        DESCRIPTION("description"),
        MATERIAL("gui-material"),
        RESTRICTED_WORLDS("restricted-worlds"),
        HIDDEN("hidden-when-unavailable"),
        LEVEL_REQ_COLOR("level-req-color"),
        IGNORE_SKILLUP_COLOR("ignore-skillup-color");

        public final String s;

        ItemTypeEnum(String s) {
            this.s = s;
        }

        @Override
        public EnumMap<ItemTypeEnum, Object> getDefaultValues() {
            return new EnumMap<ItemTypeEnum, Object>(ItemTypeEnum.class) {
                {
                    put(LEVEL_REQ, 0);
                    put(PROFTYPE, "crafting");
                    put(EXP, 0);
                    put(NAME, "&eNO_NAME");
                    put(DESCRIPTION, Arrays.asList("&aThe", "&bDescription"));
                    put(MATERIAL, Material.CHEST);
                    put(RESTRICTED_WORLDS, Arrays.asList("some_world", "some_other_world"));
                    put(HIDDEN, true);
                    put(IGNORE_SKILLUP_COLOR, true);
                }
            };
        }

        @Override
        public String toString() {
            return s;
        }
    }
}
