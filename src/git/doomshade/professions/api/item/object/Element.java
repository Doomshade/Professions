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

package git.doomshade.professions.api.item.object;

import git.doomshade.professions.cache.Cache;
import git.doomshade.professions.cache.Cacheable;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.exceptions.ProfessionObjectInitializationException;
import git.doomshade.professions.io.IOManager;
import git.doomshade.professions.profession.professions.herbalism.Herb;
import git.doomshade.professions.profession.professions.mining.Ore;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.ILoadable;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

import static git.doomshade.professions.utils.Strings.ElementEnum.ID;

/**
 * The {@link IElement} implementation
 *
 * @author Doomshade
 * @version 1.0
 * @see IElement
 * @since 1.0
 */
public abstract class Element implements IElement, ConfigurationSerializable, Cacheable, ILoadable {
    private static final Map<
            Class<? extends Element>,
            Map<String, Element>
            > ELEMENTS = new HashMap<>();
    private final String id;
    private final String name;

    /**
     * Overloaded constructor with a {@code true} as the third parameter
     *
     * @param id   the ID of this element
     * @param name the name of this element
     *
     * @throws IllegalArgumentException if the ID of the child class exists, for example this prevents two {@link Herb}s
     *                                  with the same ID, but not a {@link Herb} and an {@link Ore} with the same ID
     * @see Element#Element(String, String, boolean)
     */
    protected Element(String id, String name) throws IllegalArgumentException {
        this(id, name, true);
    }

    /**
     * @param id              the ID of this element
     * @param name            the name of this element
     * @param registerElement whether to register this element so it's visible in-game
     *
     * @throws IllegalArgumentException if the ID of the child class exists, for example this prevents two {@link Herb}s
     *                                  with the same ID, but not a {@link Herb} and an {@link Ore} with the same ID
     */
    protected Element(String id, String name, boolean registerElement) throws IllegalArgumentException {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        final boolean isExampleId = Utils.EXAMPLE_ID.equalsIgnoreCase(id);
        if (!isExampleId && registerElement) {
            Map<String, Element> map = ELEMENTS.getOrDefault(getClass(), new HashMap<>());
            final Element el = map.putIfAbsent(id, this);
            if (el != null) {
                throw new IllegalArgumentException(String.format("An element %s with ID %s already exists!",
                        el.getName(), id));
            }
            ELEMENTS.put(getClass(), map);
        }
    }

    /**
     * Deserializes an {@link Element} and converts it to a desired implementation of it
     *
     * @param map                the serialization of the {@link Element}
     * @param name               the name of the {@link Element}
     * @param clazz              the implementation class
     * @param conversionFunction the function to convert from {@link Element} to the desired implementation
     * @param <T>                the implementation
     *
     * @return a deserialized implementation of an {@link Element}
     *
     * @throws ProfessionObjectInitializationException if there are missing keys or the conversion function returns
     *                                                 {@code null}
     * @see Element#deserializeElement(Map, String, Class, Function, Collection, Class[])
     */
    protected static <T extends Element> T deserializeElement(Map<String, Object> map,
                                                              String name,
                                                              Class<T> clazz,
                                                              Function<Element, T> conversionFunction)
            throws ProfessionObjectInitializationException {
        return deserializeElement(map, name, clazz, conversionFunction, Collections.emptyList());
    }

    /**
     * Deserializes an {@link Element} and converts it to a desired implementation of it including checking for missing
     * keys
     *
     * @param map                the serialization of the {@link Element}
     * @param name               the name of the {@link Element}
     * @param clazz              the implementation class
     * @param conversionFunction the function to convert from {@link Element} to the desired implementation
     * @param ignoredKeys        the ignored keys in the {@link FileEnum}
     * @param keys               the {@link FileEnum}s
     * @param <T>                the implementation
     *
     * @return a deserialized implementation of an {@link Element}
     *
     * @throws ProfessionObjectInitializationException if there are missing keys or the conversion function returns
     *                                                 {@code null}
     */
    @NotNull
    @SafeVarargs
    protected static <T extends Element> T deserializeElement(Map<String, Object> map,
                                                              String name,
                                                              Class<T> clazz,
                                                              Function<Element, T> conversionFunction,
                                                              final Collection<String> ignoredKeys,
                                                              final Class<? extends FileEnum>... keys)
            throws ProfessionObjectInitializationException {
        checkForMissingKeys(map, clazz, Collections.emptyList(), Strings.ElementEnum.class);
        checkForMissingKeys(map, clazz, ignoredKeys, keys);

        // get the ID from the serialization
        final String id = (String) map.get(ID.s);

        // create a dummy class
        final Element dummy = new Element(id, name, false) {
        };

        // convert the element
        final T element = conversionFunction.apply(dummy);
        if (element == null) {
            throw new ProfessionObjectInitializationException("Could not deserialize an element due to an error");
        }
        return element;
    }

    /**
     * Checks for missing keys in the serialization
     *
     * @param map         the serialization
     * @param clazz       the class just for logging purposes
     * @param ignoredKeys the ignored {@link FileEnum} keys
     * @param keys        the {@link FileEnum} keys
     *
     * @throws ProfessionObjectInitializationException if there are missing keys
     */
    @SafeVarargs
    protected static void checkForMissingKeys(Map<String, Object> map,
                                              Class<?> clazz,
                                              Collection<String> ignoredKeys,
                                              Class<? extends FileEnum>... keys)
            throws ProfessionObjectInitializationException {
        Set<String> missingKeys = Strings.getMissingKeysSet(map, keys);
        if (ignoredKeys != null) {
            missingKeys.removeAll(ignoredKeys);
        }
        if (!missingKeys.isEmpty()) {
            throw new ProfessionObjectInitializationException(clazz, missingKeys,
                    ProfessionObjectInitializationException.ExceptionReason.MISSING_KEYS);
        }
    }

    /**
     * Caches all elements
     */
    protected static void cacheElements() throws IOException {
        if (!Settings.isCache()) {
            return;
        }

        for (Map.Entry<Class<? extends Element>, Map<String, Element>> allElements : getAllElements().entrySet()) {
            final Map<String, Element> elementMap = allElements.getValue();

            final int dataSize = elementMap.size();
            if (dataSize == 0) {
                continue;
            }

            // now store the data in a cache
            final Cacheable[] data = new Cacheable[dataSize];
            int i = 0;
            for (Map.Entry<String, Element> elementEntry : elementMap.entrySet()) {
                data[i++] = elementEntry.getValue();
            }

            Cache cache = new Cache(IOManager.createCacheFile(data[0]));
            cache.save(data);
        }
    }

    public static Map<Class<? extends Element>, Map<String, Element>> getAllElements() {
        return Map.copyOf(ELEMENTS);
    }

    /**
     * Dereferences all elements from the memory
     */
    protected static void unloadAllElements() {
        ELEMENTS.clear();

        // call the garbage collect as there will be a lot of unused references
        System.gc();
    }

    public static <E extends Element> E getElement(Class<E> of, String id) {
        return getElements(of).get(id);
    }

    @SuppressWarnings("unchecked")
    public static <E extends Element> Map<String, E> getElements(Class<E> of) {
        return (Map<String, E>) Map.copyOf(ELEMENTS.get(of));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(ID.s, getId());
        return map;
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final String getName() {
        return name;
    }

    /**
     * @return all known elements (e.g. all herbs)
     */
    public Map<String, ? extends Element> getElements() {
        return Map.copyOf(ELEMENTS.get(getClass()));
    }

    @Override
    public void loadCache(Serializable[] data) {
    }

    @Override
    public Serializable[] cache() {
        return new Serializable[0];
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public final String getFileName() {
        return getId();
    }

    @Override
    public String toString() {
        return "Element{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
