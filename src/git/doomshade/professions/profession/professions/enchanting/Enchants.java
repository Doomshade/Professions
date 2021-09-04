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

package git.doomshade.professions.profession.professions.enchanting;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.manager.AttributeManager;
import git.doomshade.professions.utils.Range;
import git.doomshade.professions.utils.ItemAttribute;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Utils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static git.doomshade.professions.profession.professions.enchanting.Enchant.DEFAULT_INTENSITY;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public final class Enchants {
    private static final EnchantMetadata RANDOM_ATTRIBUTE_ENCHANT = new EnchantMetadata(
            (x, y) -> {
                // put to file
                final int maxIntensity = 1;
                final HashMap<Integer, Range> intensityValues = new HashMap<>() {
                    {
                        put(DEFAULT_INTENSITY, new Range(1, 5));
                        put(maxIntensity, new Range(5, 10));
                    }
                };
                ItemMeta meta = x.getItemMeta();
                final Random r = new Random();
                switch (y) {
                    case DEFAULT_INTENSITY:
                        HashMap<String, AttributeManager.Attribute> attributes =
                                SkillAPI.getAttributeManager().getAttributes();
                        int random = r.nextInt(attributes.size());
                        int i = 0;
                        String randomAttr = "";
                        for (String attr : attributes.keySet()) {
                            if (i == random) {
                                randomAttr = attr;
                                break;
                            }
                            i++;
                        }
                        if (randomAttr.isEmpty()) {
                            throw new IllegalStateException("Returned an empty attribute from sapi attributes (???)");
                        }
                        ItemAttribute attribute = new ItemAttribute(randomAttr, intensityValues.get(y).getRandom(),
                                ItemAttribute.AttributeType.SKILLAPI);
                        break;
                    case maxIntensity:
                        break;
                }
                return null;
            },
            Arrays.asList(DEFAULT_INTENSITY, 1),
            x -> {
            }
    );
    private static final Map<Type, EnchantMetadata> ENCHANTS = new HashMap<>();
    private static final Enchant EXAMPLE_ENCHANT = new Enchant(Utils.EXAMPLE_ID, Utils.EXAMPLE_NAME,
            ItemUtils.EXAMPLE_RESULT, RANDOM_ATTRIBUTE_ENCHANT, false);

    private Enchants() {
    }

    public static Enchant getEnchant(String id, String name, ItemStack item, Enchants.Type enchant) {
        return new Enchant(id, name, item, ENCHANTS.get(enchant), true);
    }

    public static Enchant getExampleEnchant() {
        return EXAMPLE_ENCHANT;
    }

    public enum Type {
        RANDOM_ATTRIBUTE
    }
}
