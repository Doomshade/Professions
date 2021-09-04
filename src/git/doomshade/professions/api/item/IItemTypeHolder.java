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

import git.doomshade.professions.api.profession.IProfessionManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Manager of {@link ItemType}. To register this holder call {@link IProfessionManager#registerItemTypeHolder(Class,
 * ConfigurationSerializable, Consumer)}.
 *
 * @param <T>    the {@link ItemType}'s object
 * @param <Type> the {@link ItemType}
 *
 * @author Doomshade
 * @version 1.0
 * @see IProfessionManager
 * @since 1.0
 */
public interface IItemTypeHolder<T extends ConfigurationSerializable, Type extends ItemType<T>> extends Iterable<Type> {
    /**
     * Saves and loads the item type holder
     *
     * @throws IOException if an error occurs
     */
    void update() throws IOException;

    /**
     * Adds defaults and saves files
     *
     * @param override whether to override or only add default
     *
     * @throws IOException if the save is unsuccessful
     */
    void save(boolean override) throws IOException;

    void updateLoader(ConfigurationSection section, String path, Object value, boolean isDefault);

    @Nullable FileConfiguration getLoader(File itemFile) throws IOException;

    /**
     * Loads the item type holder from file
     *
     * @throws IOException the item type file could not be loaded
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    void load() throws IOException;

    /**
     * Sorts the item types by comparator generated from "sorted-by" values
     */
    void sortItems();

    void sortItems(List<Type> items);

    /**
     * @return the error message
     */
    List<String> getErrorMessage();

    /**
     * @return the new items message
     */
    List<String> getNewItemsMessage();

    Comparator<Type> getComparator();

    /**
     * Used for generating defaults
     *
     * @return the example item type this holder holds
     */
    Type getExampleItemType();
}
