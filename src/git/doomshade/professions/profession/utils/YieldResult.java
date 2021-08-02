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

package git.doomshade.professions.profession.utils;

import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.utils.ItemUtils;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class YieldResult implements ConfigurationSerializable, Comparable<YieldResult> {
    public final double chance;
    public final ItemStack drop;

    public YieldResult(double chance, ItemStack drop) {
        this.chance = chance;
        this.drop = drop;
    }

    public static YieldResult deserialize(Map<String, Object> map)
            throws ConfigurationException, InitializationException {
        MemorySection section = (MemorySection) map.get("item");
        final ItemStack deserialize;
        try {
            deserialize = ItemUtils.deserialize(section.getValues(false));
        } catch (ConfigurationException e) {
            e.append("Yield Result");
            throw e;
        }
        final double chance = (double) map.getOrDefault("chance", 0);
        return new YieldResult(chance, deserialize);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("chance", chance);
        map.put("item", ItemUtils.serialize(drop));
        return map;
    }

    @Override
    public int compareTo(@NotNull YieldResult o) {
        return Double.compare(chance, o.chance);
    }
}
