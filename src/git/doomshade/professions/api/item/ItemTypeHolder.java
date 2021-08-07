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

import git.doomshade.professions.api.IProfessionManager;
import git.doomshade.professions.api.dynmap.AMarkable;
import git.doomshade.professions.commands.CommandHandler;
import git.doomshade.professions.commands.GenerateDefaultsCommand;
import git.doomshade.professions.data.DefaultsSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.SortType;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static git.doomshade.professions.utils.Strings.ItemTypeHolderEnum.*;

/**
 * Manager of {@link ItemType}. To register this holder call {@link IProfessionManager#registerItemTypeHolder(Class,
 * ConfigurationSerializable, Consumer)} )}.
 *
 * @param <Type> the ItemType
 *
 * @author Doomshade
 * @version 1.0
 * @see IProfessionManager
 * @since 1.0
 */
public class ItemTypeHolder<T extends ConfigurationSerializable, Type extends ItemType<T>> extends AMarkable
        implements Iterable<Type> {
    /**
     * The dynmap marker layer
     */
    private static final int MARKER_LAYER = 0;

    /**
     * The example item type (this item type should have an ID of -1)
     */
    private final Type itemType;

    /**
     * Using linked hashmap as in the file it is not specified that the item type IDs must be consecutive
     */
    private LinkedHashMap<Integer, Type> itemTypes = new LinkedHashMap<>();

    /**
     * Comparator to sort ItemTypes
     */
    private Comparator<Type> comparator = null;

    /**
     * The error message
     */
    private List<String> errorMessage = new ArrayList<>();

    /**
     * The new items message
     */
    private List<String> newItemsMessage = new ArrayList<>();

    /**
     * The main constructor of a holder of ItemType
     * <p>Note that the ItemType should have an ID of -1 (i.e. not deserialized from file) !</p>
     *
     * @param itemType the ItemType to create a holder for
     */
    public ItemTypeHolder(Class<Type> itemType, T o) {
        this(itemType, o, null);
    }

    /**
     * The main constructor of a holder of ItemType
     * <p>Note that the ItemType should have an ID of -1 (i.e. not deserialized from file) !</p>
     *
     * @param itemType the ItemType to create a holder for
     */
    public ItemTypeHolder(Class<Type> itemType, T o, Consumer<Type> additionalFunction) {
        this.itemType = ItemType.getExampleItemType(itemType, o);
        if (additionalFunction != null) {
            additionalFunction.accept(this.itemType);
        }
    }

    /**
     * Saves and loads the item type holder
     *
     * @throws IOException if an error occurs
     */
    public void update() throws IOException {
        save(true);
        load();
    }

    /**
     * Adds defaults and saves files
     *
     * @param override whether to override or only add default
     *
     * @throws IOException if the save is unsuccessful
     */
    public final void save(boolean override) throws IOException {
        File itemFile = ItemUtils.getItemTypeFile(itemType.getClass());
        FileConfiguration loader = getLoader(itemFile);
        if (loader == null) {
            return;
        }

        // add defaults or set the values to the root based on override value
        final List<String> sortedBy = Settings.getSettings(DefaultsSettings.class).getSortedBy();
        updateLoader(loader, ERROR_MESSAGE.s, errorMessage, !override);
        updateLoader(loader, SORTED_BY.s, sortedBy, !override);
        updateLoader(loader, NEW_ITEMS_AVAILABLE_MESSAGE.s, newItemsMessage, !override);
        updateLoader(loader, Strings.MarkableEnum.MARKER_SET_ID.s, getMarkerSetId(), !override);
        updateLoader(loader, Strings.MarkableEnum.MARKER_VISIBLE.s, isVisible(), !override);

        // get or create a new "items" section
        final ConfigurationSection itemsSection;
        if (loader.isConfigurationSection(ItemType.KEY_ITEMS)) {
            itemsSection = loader.getConfigurationSection(ItemType.KEY_ITEMS);
        } else {
            itemsSection = loader.createSection(ItemType.KEY_ITEMS);
        }

        if (itemsSection != null) {
            // add a default serialized example item type
            itemsSection.addDefault(String.valueOf(0), itemType.serialize());

            // serialize all the registered item types
            for (Map.Entry<Integer, Type> entry : itemTypes.entrySet()) {
                Type itemType = entry.getValue();
                updateLoader(itemsSection, entry.getKey().toString(), itemType.serialize(), !override);
            }
        }
        loader.options().copyDefaults(true);
        loader.save(itemFile);
    }

    private void updateLoader(ConfigurationSection section, String path, Object value, boolean isDefault) {
        if (isDefault) {
            section.addDefault(path, value);
        } else {
            section.set(path, value);
        }
    }

    @Nullable
    private FileConfiguration getLoader(File itemFile) throws IOException {
        FileConfiguration loader = YamlConfiguration.loadConfiguration(itemFile);
        try {
            loader.load(itemFile);
        } catch (InvalidConfigurationException e) {
            ProfessionLogger.log(
                    "Could not load file as yaml exception has been thrown (make sure you haven't added ANYTHING " +
                            "extra to the file!)",
                    Level.WARNING);
            ProfessionLogger.logError(e, false);
            return null;
        }
        return loader;
    }


    /**
     * Loads the item type holder from file
     *
     * @throws IOException the item type file could not be loaded
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void load() throws IOException {
        final Class<? extends ItemType> clazz = itemType.getClass();
        File itemFile = ItemUtils.getItemTypeFile(clazz);
        FileConfiguration loader = getLoader(itemFile);
        if (loader == null) {
            return;
        }

        // TODO make a new method for this
        this.errorMessage = loader.getStringList(
                ERROR_MESSAGE.s); //ItemUtils.getDescription(itemType, loader.getStringList(ERROR_MESSAGE), null);
        this.setVisible(loader.getBoolean(Strings.MarkableEnum.MARKER_VISIBLE.s));
        this.setMarkerSetId(loader.getString(Strings.MarkableEnum.MARKER_SET_ID.s));

        this.itemTypes.clear();
        Comparator<ItemType<?>> comparator = null;
        for (String st : loader.getStringList(SORTED_BY.s)) {
            SortType sortType = SortType.getSortType(st);
            if (comparator == null) {
                comparator = sortType.getComparator();
            } else {
                comparator = comparator.thenComparing(sortType.getComparator());
            }
        }
        this.comparator =
                comparator == null ? (Comparator<Type>) SortType.NAME.getComparator() : (Comparator<Type>) comparator;

        // TODO
        this.newItemsMessage = loader.getStringList(
                NEW_ITEMS_AVAILABLE_MESSAGE.s);
        //ItemUtils.getDescription(itemType, loader.getStringList
        // (NEW_ITEMS_AVAILABLE_MESSAGE), null);

        final ConfigurationSection itemsSection = loader.isConfigurationSection(ItemType.KEY_ITEMS) ?
                loader.getConfigurationSection(ItemType.KEY_ITEMS) :
                loader.createSection(ItemType.KEY_ITEMS);
        if (itemsSection == null) {
            return;
        }
        final Collection<Integer> keys = itemsSection.getKeys(false)
                .stream()
                .map(Integer::parseInt)
                .sorted()
                .collect(Collectors.toList());
        boolean successInit = true;
        for (int i : keys) {
            try {
                Type deserializedItemType = (Type) ItemType.deserialize(clazz, i);
                if (deserializedItemType != null) {
                    itemTypes.put(i, deserializedItemType);
                    deserializedItemType.setMarkerSetId(this);
                    deserializedItemType.setVisible(this);
                }
            } catch (Exception e) {
                ProfessionLogger.log(
                        "Could not deserialize " + ItemUtils.getItemTypeFile(clazz).getName() +
                                " with id " + i, Level.WARNING);
                ProfessionLogger.logError(e, !(e instanceof InitializationException));
                successInit = false;
            }
        }

        if (!successInit) {
            try {
                final CommandHandler cmdHandler = CommandHandler.getInstance(CommandHandler.class);
                if (cmdHandler != null) {
                    final String infoCommand = ChatColor.stripColor(
                            cmdHandler.infoMessage(cmdHandler.getCommand(GenerateDefaultsCommand.class)));
                    ProfessionLogger.log(
                            String.format("Could not deserialize all item types. Usage of %s is advised.",
                                    infoCommand));
                }
            } catch (Utils.SearchNotFoundException ignored) {
            }
        }

        sortItems();
    }

    /**
     * Sorts the item types by comparator generated from "sorted-by" values
     */
    public void sortItems() {
        this.itemTypes = Utils.sortMapByValue(this.itemTypes, this.comparator);
    }

    public void sortItems(List<Type> items) {
        items.sort(this.comparator);
    }

    /**
     * @return the error message
     */
    public List<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * @return the new items message
     */
    public List<String> getNewItemsMessage() {
        return newItemsMessage;
    }

    public Comparator<Type> getComparator() {
        return comparator;
    }

    /**
     * @return the example item type this holder holds
     *
     * @apiNote Used for generating defaults
     */
    public final Type getExampleItemType() {
        return itemType;
    }

    @Override
    public @NotNull Iterator<Type> iterator() {
        return itemTypes.values().iterator();
    }

    @Override
    public final int getLayer() {
        return MARKER_LAYER;
    }

}
