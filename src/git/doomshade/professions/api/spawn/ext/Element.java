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

package git.doomshade.professions.api.spawn.ext;

import com.google.common.collect.ImmutableMap;
import git.doomshade.professions.api.spawn.IElement;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public abstract class Element implements IElement, ConfigurationSerializable {
    private static final Map<
            Class<? extends Element>,
            Map<String, Element>
            > ELEMENTS = new HashMap<>();
    private final String id;

    protected Element(String id, boolean registerElement) {
        this.id = id;
        if (!Utils.EXAMPLE_ID.equalsIgnoreCase(id) && registerElement) {
            Map<String, Element> map = ELEMENTS.getOrDefault(getClass(), new HashMap<>());
            final Element el = map.putIfAbsent(id, this);
            if (el != null) {
                throw new IllegalArgumentException(String.format("An element %s with ID %s already exists!",
                        el.getName(), id));
            }
            ELEMENTS.put(getClass(), map);
        }
    }

    public static void unloadElements() {
        ELEMENTS.clear();
    }

    public static Map<Class<? extends Element>, Map<String, Element>> getAllElements() {
        return ImmutableMap.copyOf(ELEMENTS);
    }

    public static <E extends Element> E get(Class<E> of, String id) {
        return getElements(of).get(id);
    }

    @SuppressWarnings("unchecked")
    public static <E extends Element> Map<String, E> getElements(Class<E> of) {
        return (Map<String, E>) ImmutableMap.copyOf(ELEMENTS.get(of));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(Strings.ElementEnum.ID.s, getId());
        map.put(Strings.ElementEnum.NAME.s, getName());
        return map;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * @return all known elements (e.g. all herbs possible herbs)
     */
    public Map<String, ? extends Element> getElements() {
        return ImmutableMap.copyOf(ELEMENTS.get(getClass()));
    }

}
