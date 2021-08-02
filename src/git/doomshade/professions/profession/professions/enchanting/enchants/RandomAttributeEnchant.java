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

package git.doomshade.professions.profession.professions.enchanting.enchants;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.manager.AttributeManager;
import git.doomshade.professions.api.spawn.Range;
import git.doomshade.professions.profession.professions.enchanting.Enchant;
import git.doomshade.professions.utils.ItemAttribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RandomAttributeEnchant extends Enchant {
    private static final HashMap<Integer, Range> INTENSITY_VALUES = new HashMap<>() {
        {
            put(DEFAULT_INTENSITY, new Range(1, 5));
            put(1, new Range(5, 10));
        }
    };

    public RandomAttributeEnchant(ItemStack item) {
        super(item);
    }

    @Override
    public List<Integer> getIntensities() {
        return Arrays.asList(DEFAULT_INTENSITY, 1);
    }

    @Override
    protected void init() {
        setEnchantFunction((x, y) -> {
            ItemMeta meta = x.getItemMeta();
            switch (y) {
                case DEFAULT_INTENSITY:
                    HashMap<String, AttributeManager.Attribute> attributes =
                            SkillAPI.getAttributeManager().getAttributes();
                    int random = this.random.nextInt(attributes.size());
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
                    ItemAttribute attribute = new ItemAttribute(randomAttr, INTENSITY_VALUES.get(y).getRandom(),
                            ItemAttribute.AttributeType.SKILLAPI);
                    break;
                case 1:
                    break;
            }
            return null;
        });
    }

}
