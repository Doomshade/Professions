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

package git.doomshade.professions.profession.professions.blacksmithing;

import git.doomshade.professions.api.spawn.ext.Element;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Strings;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static git.doomshade.professions.utils.Strings.BSEnum.ITEM;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class BSItemStack extends Element {
    private final ItemStack item;

    public BSItemStack(String id, String name, ItemStack item) {
        super(id, name);
        this.item = item;
    }

    public static BSItemStack deserialize(final Map<String, Object> map, String name)
            throws ProfessionObjectInitializationException {
        final BSItemStack is = deserializeElement(map, name, BSItemStack.class, x -> {
            try {
                return new BSItemStack(x.getId(), x.getName(), ItemUtils.deserialize(map));
            } catch (ConfigurationException | ProfessionObjectInitializationException e) {
                ProfessionLogger.logError(e, false);
                return null;
            }
        }, Collections.emptyList(), Strings.BSEnum.class);

        return is;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put(ITEM.s, ItemUtils.serialize(item));
        return new HashMap<>() {
            {
                put(ITEM.s, ItemUtils.serialize(item));
            }
        };
    }

    public ItemStack getItem() {
        return item;
    }
}
