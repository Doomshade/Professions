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

package git.doomshade.professions.profession.professions.alchemy;

import git.doomshade.professions.api.item.CraftableItemType;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.profession.utils.EffectUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class PotionItemType extends CraftableItemType<Potion> {

    /**
     * Constructor for creation of the item type object
     *
     * @param object
     */
    public PotionItemType(Potion object) {
        super(object);
        setResult(Potion.EXAMPLE_POTION.getItem());
    }

    @Override
    protected Potion deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {
        Potion potion = Potion.deserialize(map);
        final Optional<ItemStack> potionItem = potion.getPotionItem(potion.getItem());
        potionItem.ifPresent(this::setResult);
        return potion;
    }


    @Override
    public Function<ItemStack, ItemStack> getExtraInEvent() {
        return Function.identity();
    }


    @Override
    public void onPluginDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            //Potion.cache(p);
        }
        Potion.POTIONS.clear();
    }

    @Override
    public void onPluginAfterReload() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            //Potion.loadFromCache(p);
        }
    }

    @Override
    public void onPluginEnable() {

        Potion.registerCustomPotionEffect(
                (potionEffect, player, negated) -> EffectUtils.addAttributes(player, negated, potionEffect));
    }
}
