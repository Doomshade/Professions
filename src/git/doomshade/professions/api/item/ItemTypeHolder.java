package git.doomshade.professions.api.item;

import com.google.common.collect.ImmutableList;
import git.doomshade.professions.Professions;
import git.doomshade.professions.commands.CommandHandler;
import git.doomshade.professions.commands.GenerateDefaultsCommand;
import git.doomshade.professions.data.DefaultsSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.SortType;
import git.doomshade.professions.exceptions.ProfessionInitializationException;
import git.doomshade.professions.profession.ProfessionManager;
import git.doomshade.professions.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/**
 * Holder for {@link ItemType}. To register this holder call {@link ProfessionManager#registerItemTypeHolder(ItemTypeHolder)}.
 *
 * @param <Type> the ItemType
 * @author Doomshade
 * @version 1.0
 * @see ProfessionManager and its regsiterItemTypeHolders() method on GitHub to see an example on how to register this holder properly.
 */
public class ItemTypeHolder<Type extends ItemType<?>> implements Iterable<Type> {

    /**
     * Keys in item type file
     */
    private static final String ERROR_MESSAGE = "error-message", SORTED_BY = "sorted-by", NEW_ITEMS_AVAILABLE_MESSAGE = "new-items-available-message";

    /**
     * List required for the ordering of item types
     */
    private final List<Type> itemTypes = new ArrayList<>();

    /**
     * The sorting order
     */
    private final List<SortType> sortTypes = new ArrayList<>();
    /**
     * The example item type (this item type should have an ID of -1)
     */
    private final Type itemType;
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
    public ItemTypeHolder(Type itemType) {
        this.itemType = itemType;
    }

    /**
     * Adds a custom item type that is not in the file into this item holder
     *
     * @param item the ItemType to add
     * @see #getRegisteredItemTypes()
     */
    public final void registerObject(Type item) {
        if (!itemTypes.contains(item)) {
            itemTypes.add(item);
        }
    }

    /**
     * @return all registered item types
     */
    public final List<Type> getRegisteredItemTypes() {
        return itemTypes;
    }

    /**
     * Adds defaults and saves files
     *
     * @throws IOException if the save is unsuccessful
     */
    public final void save(boolean safely) throws IOException {
        File itemFile = itemType.getFile();
        FileConfiguration loader = YamlConfiguration.loadConfiguration(itemFile);
        try {
            loader.load(itemFile);
        } catch (InvalidConfigurationException e) {
            Professions.log("Could not load file as yaml exception has been thrown (make sure you haven't added ANYTHING extra to the file!)", Level.WARNING);
            return;
        }
        final ImmutableList<String> sortedBy = Settings.getSettings(DefaultsSettings.class).getSortedBy();
        if (safely) {
            loader.addDefault(ERROR_MESSAGE, errorMessage);
            loader.addDefault(SORTED_BY, sortedBy);
            loader.addDefault(NEW_ITEMS_AVAILABLE_MESSAGE, newItemsMessage);
        } else {
            loader.set(ERROR_MESSAGE, errorMessage);
            loader.set(SORTED_BY, sortedBy);
            loader.set(NEW_ITEMS_AVAILABLE_MESSAGE, newItemsMessage);
        }
        ConfigurationSection itemsSection;
        if (loader.isConfigurationSection(ItemType.KEY)) {
            itemsSection = loader.getConfigurationSection(ItemType.KEY);
        } else {
            itemsSection = loader.createSection(ItemType.KEY);
        }

        if (itemsSection != null) {
            if (safely) {
                itemsSection.addDefault(String.valueOf(0), itemType.serialize());
            }
            if (!itemTypes.isEmpty()) {
                for (int i = 0; i < itemTypes.size(); i++) {
                    Type registeredObject = itemTypes.get(i);
                    if (safely) {
                        itemsSection.addDefault(String.valueOf(i), registeredObject.serialize());
                    } else {
                        itemsSection.set(String.valueOf(i), registeredObject.serialize());
                    }
                }
            }
        }
        loader.options().copyDefaults(true);
        loader.save(itemFile);
    }

    /**
     * @return the example item type this holder holds
     */
    public final Type getItemType() {
        return itemType;
    }

    /**
     * @return the file of this item type holder
     * @see ItemType#getFile()
     */
    public final File getFile() {
        return itemType.getFile();
    }


    /**
     * Loads the item type holder from file
     *
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("all")
    public void load() throws IOException {
        File itemFile = itemType.getFile();
        FileConfiguration loader = YamlConfiguration.loadConfiguration(itemFile);
        try {
            loader.load(itemFile);
        } catch (InvalidConfigurationException e) {
            Professions.log("Could not load file as yaml exception has been thrown (make sure you haven't added ANYTHING extra to the file!)", Level.WARNING);
            return;
        }

        // TODO make a new method for this
        this.errorMessage = loader.getStringList(ERROR_MESSAGE);//ItemUtils.getDescription(itemType, loader.getStringList(ERROR_MESSAGE), null);

        this.sortTypes.clear();
        for (String st : loader.getStringList(SORTED_BY)) {
            SortType sortType = SortType.getSortType(st);
            if (sortType != null) {
                this.sortTypes.add(sortType);
            }
        }

        // TODO
        this.newItemsMessage = loader.getStringList(NEW_ITEMS_AVAILABLE_MESSAGE);//ItemUtils.getDescription(itemType, loader.getStringList(NEW_ITEMS_AVAILABLE_MESSAGE), null);

        ConfigurationSection itemsSection = loader.getConfigurationSection(ItemType.KEY);
        Iterator<String> it = itemsSection.getKeys(false).iterator();
        int i;
        final Class<? extends ItemType> clazz = itemType.getClass();
        boolean successInit = true;
        while (it.hasNext()) {
            i = Integer.parseInt(it.next());

            Type deserializedItemType;
            try {
                deserializedItemType = (Type) ItemType.deserialize(clazz, i);
            } catch (ProfessionInitializationException e) {
                Professions.log(e.getMessage(), Level.WARNING);
                successInit = false;
                continue;
            } catch (Exception e) {
                Professions.logError(e);
                successInit = false;
                continue;
            }
            if (deserializedItemType != null) {
                if (!itemTypes.isEmpty() && itemTypes.size() > i) {
                    itemTypes.set(i, deserializedItemType);
                } else {
                    itemTypes.add(deserializedItemType);
                }
            }
        }

        if (!successInit) {
            try {
                final CommandHandler instance = CommandHandler.getInstance(CommandHandler.class);
                if (instance != null)
                    Professions.log("Could not deserialize all item types. Usage of " + ChatColor.stripColor(instance.infoMessage(instance.getCommand(GenerateDefaultsCommand.class))) + " is advised.");
            } catch (Utils.SearchNotFoundException ignored) {
            }
        }

        sortItems();
    }

    /**
     * Sorts from last index to first index as the first sort has the highest priority
     */
    public void sortItems() {
        for (int j = sortTypes.size() - 1; j >= 0; j--) {
            final SortType sortType = sortTypes.get(j);
            itemTypes.sort(sortType.getComparator());
        }
    }

    public void sortItems(List<ItemType<?>> items) {
        for (int j = sortTypes.size() - 1; j >= 0; j--) {
            final SortType sortType = sortTypes.get(j);
            items.sort(sortType.getComparator());
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

    @Override
    public @NotNull Iterator<Type> iterator() {
        return itemTypes.iterator();
    }
}
