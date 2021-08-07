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

package git.doomshade.professions.profession.professions.skinning;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static git.doomshade.professions.utils.Strings.PreyEnum.CONFIG_NAME;
import static git.doomshade.professions.utils.Strings.PreyEnum.ENTITY;

/**
 * Custom class for {@link PreyItemType} Here I needed mob's config name (if the Prey is a MythicMob), I'd have
 * otherwise only passed {@link EntityType} as a generic argument to {@link PreyItemType}.
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class Mob implements ConfigurationSerializable {
    final String configName;
    final EntityType type;

    Mob(EntityType type, String configName) {
        this.type = type;
        this.configName = configName;
    }

    public Mob(EntityType type) {
        this(type, "");
    }

    boolean isMythicMob() {
        return !configName.isEmpty();
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put(ENTITY.s, type.name());
        map.put(CONFIG_NAME.s, configName);
        return map;
    }
}
