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

import javax.annotation.Nullable;

/**
 * Basically an enchant manager
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class EnchantManager {
    private static final EnchantManager instance = new EnchantManager();

    private EnchantManager() {
    }

    /**
     * @return the instance of this class
     */
    public static EnchantManager getInstance() {
        return instance;
    }

    /**
     * @param item the ItemStack
     * @param <T>  the Enchant
     *
     * @return an Enchant if the ItemStack is one, {@code null} otherwise
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends Enchant> T getEnchantFromItem(ItemStack item) {
        return (T) Enchant.ENCHANTS.values()
                .stream()
                .filter(ench -> ench.getItem().isSimilar(item))
                .findFirst()
                .orElse(null);
    }

    /**
     * @param enchant the enchant class
     * @param <T>     the enchant
     *
     * @return an Enchant instance
     */
    @SuppressWarnings("unchecked")
    public <T extends Enchant> T getEnchant(Class<T> enchant) {
        return (T) Enchant.ENCHANTS.get(enchant);
    }

    /**
     * Registers an Enchant
     *
     * @param enchant the enchant class to register
     */
    public <T extends Enchant> void registerEnchant(Enchant enchant) {
        enchant.init();
        Enchant.ENCHANTS.putIfAbsent(enchant.getClass(), enchant);
    }

    /**
     * Enchants an item
     *
     * @param enchant the enchant
     * @param on      the item
     *
     * @see Enchant#use(ItemStack)
     */
    public void enchant(Enchant enchant, ItemStack on) {
        enchant.use(on);
    }

    /**
     * Enchants an item
     *
     * @param enchant   the enchant
     * @param on        the item
     * @param intensity the intensity of enchant
     *
     * @see Enchant#use(ItemStack, int)
     */
    public void enchant(Enchant enchant, ItemStack on, int intensity) {
        enchant.use(on, intensity);
    }

}
