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

import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
import org.bukkit.entity.EntityType;

import java.util.Map;
import java.util.Set;

import static git.doomshade.professions.utils.Strings.PreyEnum.CONFIG_NAME;
import static git.doomshade.professions.utils.Strings.PreyEnum.ENTITY;

/**
 * A prey (mob hunting) example for {@link SkinningProfession}
 *
 * @author Doomshade
 */
public class PreyItemType extends ItemType<Mob> {

    public PreyItemType(Mob object) {
        super(object);
    }

    @Override
    protected Mob deserializeObject(Map<String, Object> map) throws ProfessionObjectInitializationException {

        Set<String> list = Utils.getMissingKeys(map, Strings.PreyEnum.values());

        if (!list.isEmpty()) {
            throw new ProfessionObjectInitializationException(getClass(), list, getFileId());
        }

        String entityTypeName = (String) map.get(ENTITY.s);
        String configName = (String) map.get(CONFIG_NAME.s);
        for (EntityType et : EntityType.values()) {
            if (et.name().equals(entityTypeName)) {
                return new Mob(et, configName);
            }
        }
        throw new IllegalArgumentException(entityTypeName + " is not a valid entity type name!");
    }

}
