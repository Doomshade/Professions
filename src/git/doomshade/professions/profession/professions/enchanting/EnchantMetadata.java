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

import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public final class EnchantMetadata {
    private final BiFunction<ItemStack, Integer, ItemStack> func;
    private final Collection<Integer> intensities;
    private final Consumer<Void> init;

    public EnchantMetadata(
            BiFunction<ItemStack, Integer, ItemStack> func, Collection<Integer> intensities,
            Consumer<Void> init) {
        this.func = func;
        this.intensities = intensities;
        this.init = init;
    }

    public BiFunction<ItemStack, Integer, ItemStack> getFunc() {
        return func;
    }

    public Collection<Integer> getIntensities() {
        return intensities;
    }

    public Consumer<Void> getInit() {
        return init;
    }
}
