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

import git.doomshade.professions.api.item.ICraftable;
import git.doomshade.professions.api.item.ItemType;
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
     * The enum for {@link ICraftable}
     */
    public enum ICraftableEnum implements FileEnum {
        CRAFTABLE_ITEM_REQ("item-requirements"),
        RESULT("result"),
        CRAFTING_TIME("crafting-time"),
        SOUND_CRAFTING("crafting-sound"),
        SOUND_CRAFTED("crafted-sound");

        public final String s;

        ICraftableEnum(String s) {
            this.s = s;
        }


        @Override
        public EnumMap<ICraftableEnum, Object> getDefaultValues() {
            return new EnumMap<>(ICraftableEnum.class) {
                {
                    put(CRAFTABLE_ITEM_REQ,
                            new Requirements(Collections.singletonList(ItemUtils.EXAMPLE_REQUIREMENT)).serialize());
                    put(RESULT, ItemUtils.EXAMPLE_RESULT.serialize());
                    put(CRAFTING_TIME, 5d);
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
        public EnumMap<ItemTypeEnum, Object> getDefaultValues() {
            return new EnumMap<>(ItemTypeEnum.class) {
                {
                    put(LEVEL_REQ, 0);
                    put(EXP, 0);
                    put(NAME, "&eNO_NAME");
                    put(DESCRIPTION, Arrays.asList("&aThe", "&bDescription"));
                    put(MATERIAL, Material.CHEST);
                    put(RESTRICTED_WORLDS, Arrays.asList("some_world", "some_other_world"));
                    put(INVENTORY_REQUIREMENTS,
                            new Requirements(Collections.singletonList(ItemUtils.EXAMPLE_REQUIREMENT)).serialize());
                    put(IGNORE_SKILLUP_COLOR, true);
                    put(TRAINABLE, false);
                    put(TRAINABLE_COST, 0);
                }
            };
        }

        @Override
        public String toString() {
            return s;
        }
    }
}
