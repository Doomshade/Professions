package git.doomshade.professions.profession.types;

import com.google.common.collect.ImmutableList;
import git.doomshade.professions.ProfessionManager;
import git.doomshade.professions.Professions;
import git.doomshade.professions.commands.CommandHandler;
import git.doomshade.professions.commands.GenerateDefaultsCommand;
import git.doomshade.professions.data.DefaultsSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.SortType;
import git.doomshade.professions.exceptions.ProfessionInitializationException;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/**
 * Holder for {@link ItemType}. To register this holder call {@link git.doomshade.professions.ProfessionManager#registerItemTypeHolder(ItemTypeHolder)}.
 *
 * @param <Type>
 * @author Doomshade
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
    private List<Type> itemTypes = new ArrayList<>();

    private final List<SortType> sortTypes = new ArrayList<>();
    private List<String> errorMessage = new ArrayList<>();
    private List<String> newItemsMessage = new ArrayList<>();

    public List<String> getErrorMessage() {
        return errorMessage;
    }

    public List<String> getNewItemsMessage() {
        return newItemsMessage;
    }

    private Type itemType;

    public ItemTypeHolder(Type itemType) {
        this.itemType = itemType;
    }

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

        if (safely) {
            itemsSection.addDefault(String.valueOf(0), itemType.serialize());
        }
        if (!itemTypes.isEmpty())
            for (int i = 0; i < itemTypes.size(); i++) {
                Type registeredObject = itemTypes.get(i);
                if (safely)
                    itemsSection.addDefault(String.valueOf(i), registeredObject.serialize());
                else
                    itemsSection.set(String.valueOf(i), registeredObject.serialize());
            }
        loader.options().copyDefaults(true);
        loader.save(itemFile);
    }

    public final Type getItemType() {
        return itemType;
    }

    public final File getFile() {
        return itemType.getFile();
    }

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

        this.errorMessage = ItemUtils.getDescription(itemType, loader.getStringList(ERROR_MESSAGE), null);

        this.sortTypes.clear();
        for (String st : loader.getStringList(SORTED_BY)) {
            SortType sortType = SortType.getSortType(st);
            if (sortType != null) {
                this.sortTypes.add(sortType);
            }
        }

        this.newItemsMessage = ItemUtils.getDescription(itemType, loader.getStringList(NEW_ITEMS_AVAILABLE_MESSAGE), null);

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
                    Professions.log("Could not deserialize all item types. Usage of " + ChatColor.stripColor(instance.infoMessage(Utils.findInIterable(instance.getCommands(), x -> x.getClass().equals(GenerateDefaultsCommand.class)))) + " is advised.");
            } catch (Utils.SearchNotFoundException ignored) {
            }
        }

        for (int j = sortTypes.size() - 1; j >= 0; j--) {
            switch (sortTypes.get(j)) {
                case NAME:
                    itemTypes.sort(Comparator.comparing(o -> o.getName()));
                    break;
                case EXPERIENCE:
                    itemTypes.sort(Comparator.comparing(o -> -o.getExp()));
                    break;
                case LEVEL_REQ:
                    itemTypes.sort(Comparator.comparing(o -> o.getLevelReq()));
                    break;
            }
        }

    }

    public void update() throws IOException {
        save(true);
        load();
    }

    @NotNull
    @Override
    public Iterator<Type> iterator() {
        return itemTypes.iterator();
    }
}
