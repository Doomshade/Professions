package git.doomshade.professions.api.item;

import git.doomshade.professions.api.IProfessionManager;
import git.doomshade.professions.commands.CommandHandler;
import git.doomshade.professions.commands.GenerateDefaultsCommand;
import git.doomshade.professions.data.DefaultsSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.SortType;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ItemUtils;
import git.doomshade.professions.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Holder for {@link ItemType}. To register this holder call {@link IProfessionManager#registerItemTypeHolder(Class, Object, Consumer)}.
 *
 * @param <Type> the ItemType
 * @author Doomshade
 * @version 1.0
 * @see IProfessionManager
 */
public class ItemTypeHolder<T, Type extends ItemType<T>> implements Iterable<Type> {

    /**
     * Keys in item type file
     */
    private static final String ERROR_MESSAGE = "error-message", SORTED_BY = "sorted-by", NEW_ITEMS_AVAILABLE_MESSAGE = "new-items-available-message";

    /**
     * List required for the ordering of item types
     */
    private LinkedHashMap<Integer, Type> itemTypes = new LinkedHashMap<>();

    private Comparator<Type> comparator = null;

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
     * @throws IOException if the save is unsuccessful
     */
    public final void save(boolean override) throws IOException {
        File itemFile = ItemUtils.getItemTypeFile(itemType.getClass());
        FileConfiguration loader = YamlConfiguration.loadConfiguration(itemFile);
        try {
            loader.load(itemFile);
        } catch (InvalidConfigurationException e) {
            ProfessionLogger.log("Could not load file as yaml exception has been thrown (make sure you haven't added ANYTHING extra to the file!)", Level.WARNING);
            ProfessionLogger.logError(e, false);
            return;
        }
        final List<String> sortedBy = Settings.getSettings(DefaultsSettings.class).getSortedBy();
        if (override) {
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
            if (override) {
                itemsSection.addDefault(String.valueOf(0), itemType.serialize());
            }
            if (!itemTypes.isEmpty()) {
                for (int i = 0; i < itemTypes.size(); i++) {
                    ItemType<?> registeredObject = itemTypes.get(i);
                    if (override) {
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
     * Loads the item type holder from file
     *
     * @throws IOException if an IO error occurs
     */
    @SuppressWarnings("all")
    public void load() throws IOException {
        File itemFile = ItemUtils.getItemTypeFile(itemType.getClass());
        FileConfiguration loader = YamlConfiguration.loadConfiguration(itemFile);
        try {
            loader.load(itemFile);
        } catch (InvalidConfigurationException e) {
            ProfessionLogger.log("Could not load file as yaml exception has been thrown (make sure you haven't added ANYTHING extra to the file!)", Level.WARNING);
            ProfessionLogger.logError(e, false);
            return;
        }

        // TODO make a new method for this
        this.errorMessage = loader.getStringList(ERROR_MESSAGE);//ItemUtils.getDescription(itemType, loader.getStringList(ERROR_MESSAGE), null);

        this.itemTypes.clear();
        Comparator<ItemType<?>> comparator = null;
        for (String st : loader.getStringList(SORTED_BY)) {
            SortType sortType = SortType.getSortType(st);
            if (comparator == null) {
                comparator = sortType.getComparator();
            } else {
                comparator = comparator.thenComparing(sortType.getComparator());
            }
        }
        this.comparator = comparator == null ? (Comparator<Type>) SortType.NAME.getComparator() : (Comparator<Type>) comparator;

        // TODO
        this.newItemsMessage = loader.getStringList(NEW_ITEMS_AVAILABLE_MESSAGE);//ItemUtils.getDescription(itemType, loader.getStringList(NEW_ITEMS_AVAILABLE_MESSAGE), null);

        ConfigurationSection itemsSection = loader.getConfigurationSection(ItemType.KEY);
        Collection<Integer> keys = itemsSection.getKeys(false)
                .stream()
                .map(Integer::parseInt)
                .sorted()
                .collect(Collectors.toList());
        final Class<? extends ItemType> clazz = itemType.getClass();
        boolean successInit = true;
        for (int i : keys) {
            try {
                Type deserializedItemType = (Type) ItemType.deserialize(clazz, i);
                if (deserializedItemType != null) {
                    itemTypes.put(i, deserializedItemType);
                }
            } catch (Exception e) {
                ProfessionLogger.log("Could not deserialize " + ItemUtils.getItemTypeFile(itemType.getClass()).getName() + " with id " + i, Level.WARNING);
                ProfessionLogger.logError(e, !(e instanceof InitializationException));
                successInit = false;
                continue;
            }
        }

        if (!successInit) {
            try {
                final CommandHandler instance = CommandHandler.getInstance(CommandHandler.class);
                if (instance != null)
                    ProfessionLogger.log("Could not deserialize all item types. Usage of " + ChatColor.stripColor(instance.infoMessage(instance.getCommand(GenerateDefaultsCommand.class))) + " is advised.");
            } catch (Utils.SearchNotFoundException ignored) {
            }
        }

        sortItems();
    }

    /**
     * Sorts from last index to first index as the first sort has the highest priority
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
     */
    public final Type getItemType() {
        return itemType;
    }

    @Override
    public @NotNull Iterator<Type> iterator() {
        return itemTypes.values().iterator();
    }
}
