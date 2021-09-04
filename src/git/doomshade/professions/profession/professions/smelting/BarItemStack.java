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

package git.doomshade.professions.profession.professions.smelting;

import git.doomshade.professions.api.item.object.Element;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Utils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static git.doomshade.professions.utils.Strings.BarEnum.ITEM;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class BarItemStack extends Element {
    private static final BarItemStack EXAMPLE_BAR = new BarItemStack(Utils.EXAMPLE_ID, Utils.EXAMPLE_NAME,
            ItemUtils.EXAMPLE_RESULT);
    private final ItemStack item;

    public BarItemStack(String id, String name, ItemStack item) {
        super(id, name);
        this.item = item;
    }

    public static BarItemStack getExampleBar() {
        return EXAMPLE_BAR;
    }


    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put(ITEM.s, ItemUtils.serialize(item));
        return map;
    }

    public ItemStack getItem() {
        return item;
    }
}
