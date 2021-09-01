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

import git.doomshade.professions.api.item.ext.CraftableItemType;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

/**
 * An enchant item type example for {@link EnchantingProfession}
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class EnchantedItemItemType extends CraftableItemType<Enchant> {
    private static final String ENCHANT = "enchant";

    /**
     * Constructor for creation of the item type object
     *
     * @param object the enchant
     */
    public EnchantedItemItemType(Enchant object) {
        super(object);
    }

    @Override
    protected Enchant deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {
        return Enchant.deserialize(map, getName());
    }

    @Override
    public boolean equalsObject(Enchant t) {
        Enchant ench = getObject();
        if (ench == null || t == null) {
            return false;
        }
        return ench.getItem().isSimilar(t.getItem());
    }

    @Override
    public Function<ItemStack, Collection<?>> getExtraInEvent() {

        // we can add a collection as an argument
        return x -> Arrays.asList(
                new PreEnchantedItem(getObject(), x),
                EnchantingProfession.ProfessionEventType.CRAFT);
    }
}
