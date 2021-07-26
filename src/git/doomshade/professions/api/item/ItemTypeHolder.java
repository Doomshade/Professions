package git.doomshade.professions.api.item;

import git.doomshade.professions.api.IProfessionManager;
import git.doomshade.professions.commands.CommandHandler;
import git.doomshade.professions.commands.GenerateDefaultsCommand;
import git.doomshade.professions.data.DefaultsSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.SortType;
import git.doomshade.professions.exceptions.InitializationException;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.FileEnum;
import git.doomshade.professions.utils.ItemUtils;
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

import static git.doomshade.professions.api.item.ItemTypeHolder.ItemTypeHolderEnum.*;

/**
 * Manager of {@link ItemType}. To register this holder call {@link IProfessionManager#registerItemTypeHolder(Class,
 * Object, Consumer)}.
 *
 * @param <Type> the ItemType
 *
 * @author Doomshade
 * @version 1.0
 * @see IProfessionManager
 */
public class ItemTypeHolder<T extends ConfigurationSerializable, Type extends ItemType<T>> implements Iterable<Type> {
    /**
     * The example item type (this item type should have an ID of -1)
     */
    private final Type itemType;
    /**
     * List required for the ordering of item types
     */
    private LinkedHashMap<Integer, Type> itemTypes = new LinkedHashMap<>();
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
        final List<String> sortedBy = Settings.getSettings(DefaultsSettings.class).getSortedBy();
        loader.addDefault(ERROR_MESSAGE.s, errorMessage);
        loader.addDefault(SORTED_BY.s, sortedBy);
        loader.addDefault(NEW_ITEMS_AVAILABLE_MESSAGE.s, newItemsMessage);
        if (override) {
            loader.set(ERROR_MESSAGE.s, errorMessage);
            loader.set(SORTED_BY.s, sortedBy);
            loader.set(NEW_ITEMS_AVAILABLE_MESSAGE.s, newItemsMessage);

        }
        ConfigurationSection itemsSection;
        if (loader.isConfigurationSection(ItemType.KEY)) {
            itemsSection = loader.getConfigurationSection(ItemType.KEY);
        } else {
            itemsSection = loader.createSection(ItemType.KEY);
        }

        if (itemsSection != null) {
            itemsSection.addDefault(String.valueOf(0), itemType.serialize());
            if (!itemTypes.isEmpty()) {
                for (int i = 0; i < itemTypes.size(); i++) {
                    ItemType<?> registeredObject = itemTypes.get(i);
                    itemsSection.addDefault(String.valueOf(i), registeredObject.serialize());
                    if (override) {
                        itemsSection.set(String.valueOf(i), registeredObject.serialize());
                    }
                }
            }
        }
        loader.options().copyDefaults(true);
        loader.save(itemFile);
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
    public void load() throws IOException {
        File itemFile = ItemUtils.getItemTypeFile(itemType.getClass());
        FileConfiguration loader = getLoader(itemFile);
        if (loader == null) {
            return;
        }

        // TODO make a new method for this
        this.errorMessage = loader.getStringList(
                ERROR_MESSAGE.s);//ItemUtils.getDescription(itemType, loader.getStringList(ERROR_MESSAGE), null);

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
                ProfessionLogger.log(
                        "Could not deserialize " + ItemUtils.getItemTypeFile(itemType.getClass()).getName() +
                                " with id " + i, Level.WARNING);
                ProfessionLogger.logError(e, !(e instanceof InitializationException));
                successInit = false;
                continue;
            }
        }

        if (!successInit) {
            try {
                final CommandHandler cmdHandler = CommandHandler.getInstance(CommandHandler.class);
                if (cmdHandler != null) {
                    ProfessionLogger.log(
                            String.format("Could not deserialize all item types. Usage of %s is advised.",
                                    ChatColor.stripColor(
                                            cmdHandler.infoMessage(
                                                    cmdHandler.getCommand(GenerateDefaultsCommand.class)))));
                }
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

    enum ItemTypeHolderEnum implements FileEnum {
        ERROR_MESSAGE("error-message"),
        SORTED_BY("sorted-by"),
        NEW_ITEMS_AVAILABLE_MESSAGE("new-items-available-message");

        private final String s;

        ItemTypeHolderEnum(String s) {
            this.s = s;
        }

        @Override
        public EnumMap<ItemTypeHolderEnum, Object> getDefaultValues() {
            return new EnumMap<>(ItemTypeHolderEnum.class) {
                {
                    put(ERROR_MESSAGE, Arrays.asList("some", "error msg"));
                    put(SORTED_BY, Arrays.asList(SortType.values()));
                    put(NEW_ITEMS_AVAILABLE_MESSAGE, Arrays.asList("some", "new items message"));
                }
            };
        }
    }
}
