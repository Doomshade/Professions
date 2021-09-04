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

package git.doomshade.professions.api.item;

import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.utils.Requirements;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * An {@link ItemType} that allows for crafting an {@link ItemStack}
 *
 * @param <T> the item type to look for in {@link ProfessionEvent}
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public interface ICraftableItemType<T extends ConfigurationSerializable> extends IItemType<T> {

    /**
     * @return the crafting time of the item type
     */
    double getCraftingTime();

    /**
     * @return the result of crafting
     */
    ItemStack getResult();

    /**
     * Sets the result of the craft
     *
     * @param result the result
     */
    void setResult(ItemStack result);

    /**
     * @return the crafting requirements
     */
    Requirements getCraftingRequirements();

    /**
     * @return the internal sounds to play
     */
    Map<Sound, String> getSounds();

    enum Sound {
        CRAFTING,
        ON_CRAFT
    }
}
