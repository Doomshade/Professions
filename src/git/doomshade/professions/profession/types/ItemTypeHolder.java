package git.doomshade.professions.profession.types;

import git.doomshade.professions.ProfessionManager;
import git.doomshade.professions.Professions;
import git.doomshade.professions.data.DefaultsSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.SortType;
import git.doomshade.professions.exceptions.ProfessionInitializationException;
import git.doomshade.professions.utils.ItemUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

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
public abstract class ItemTypeHolder<Type extends ItemType<?>> implements Iterable<Type> {
    private static final String ERROR_MESSAGE = "error-message", SORTED_BY = "sorted-by", NEW_ITEMS_AVAILABLE_MESSAGE = "new-items-available-message";
    // must be list for ordering in file
    private List<Type> objects = new ArrayList<>();

    private final List<SortType> sortTypes = new ArrayList<>();
    private List<String> errorMessage = new ArrayList<>();
    private List<String> newItemsMessage = new ArrayList<>();

    public List<String> getErrorMessage() {
        return errorMessage;
    }

    public List<String> getNewItemsMessage() {
        return newItemsMessage;
    }

    private Type object;

    public final void registerObject(Type item) {
        if (!objects.contains(item)) {
            objects.add(item);
        }
    }

    public ItemTypeHolder() {
        this.object = getObject();
    }

    /**
     * @return all registered item types
     */
    public final List<Type> getRegisteredItemTypes() {
        return objects;
    }

    /**
     * Adds defaults and saves files
     *
     * @throws IOException
     */
    public final void save() throws IOException {
        File itemFile = object.getFile();
        FileConfiguration loader = YamlConfiguration.loadConfiguration(itemFile);
        try {
            loader.load(itemFile);
        } catch (InvalidConfigurationException e) {
            Professions.log("Could not load file as yaml exception has been thrown (make sure you haven't added ANYTHING extra to the file!)", Level.WARNING);
            return;
        }

        loader.addDefault(ERROR_MESSAGE, errorMessage);
        loader.addDefault(SORTED_BY, Settings.getSettings(DefaultsSettings.class).getSortedBy());
        loader.addDefault(NEW_ITEMS_AVAILABLE_MESSAGE, newItemsMessage);
        ConfigurationSection itemsSection;
        if (loader.isConfigurationSection(ItemType.KEY)) {
            itemsSection = loader.getConfigurationSection(ItemType.KEY);
        } else {
            itemsSection = loader.createSection(ItemType.KEY);
        }
        itemsSection.addDefault(String.valueOf(0), object.serialize());

        if (!objects.isEmpty())
            for (int i = 0; i < objects.size(); i++) {
                Type registeredObject = objects.get(i);
                itemsSection.addDefault(String.valueOf(i), registeredObject.serialize());
            }
        loader.options().copyDefaults(true);
        loader.save(itemFile);
    }

    protected abstract Type getObject();

    public final Type getObjectItem() {
        return object;
    }
    /*
    public final Type getObject(){
        String[] split = getClass().getGenericSuperclass().getTypeName().split("[.]");
        final String s = split[split.length - 1];
        try {
            final Class<? extends ItemType<?>> clazz = (Class<? extends ItemType<?>>) Class.forName(s.substring(0, s.length() - 1));
            final Constructor<? extends ItemType<?>> declaredConstructor = clazz.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            return (Type) declaredConstructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }*/

    public final File getFile() {
        String[] split = getClass().getGenericSuperclass().getTypeName().split("[.]");
        final String s = split[split.length - 1];
        try {
            return ItemUtils.getFile(Class.forName(s.substring(0, s.length() - 1)));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public void load() throws IOException {
        File itemFile = object.getFile();
        FileConfiguration loader = YamlConfiguration.loadConfiguration(itemFile);
        try {
            loader.load(itemFile);
        } catch (InvalidConfigurationException e) {
            Professions.log("Could not load file as yaml exception has been thrown (make sure you haven't added ANYTHING extra to the file!)", Level.WARNING);
            return;
        }

        this.errorMessage = ItemUtils.getDescription(object, loader.getStringList(ERROR_MESSAGE), null);

        this.sortTypes.clear();
        for (String st : loader.getStringList(SORTED_BY)) {
            SortType sortType = SortType.getSortType(st);
            if (sortType != null) {
                this.sortTypes.add(sortType);
            }
        }

        this.newItemsMessage = ItemUtils.getDescription(object, loader.getStringList(NEW_ITEMS_AVAILABLE_MESSAGE), null);

        ConfigurationSection itemsSection = loader.getConfigurationSection(ItemType.KEY);
        Iterator<String> it = itemsSection.getKeys(false).iterator();
        int i;
        final Class<? extends ItemType> clazz = object.getClass();
        while (it.hasNext()) {
            i = Integer.parseInt(it.next());

            Type deserialized;
            try {
                deserialized = (Type) ItemType.deserialize(clazz, i);
            } catch (ProfessionInitializationException e) {
                Professions.log(e.getMessage(), Level.WARNING);
                continue;
            }
            if (deserialized != null) {
                if (!objects.isEmpty() && objects.size() > i) {
                    objects.set(i, deserialized);
                } else {
                    objects.add(deserialized);
                }
            }
        }

        for (int j = sortTypes.size() - 1; j >= 0; j--) {
            switch (sortTypes.get(j)) {
                case NAME:
                    objects.sort(Comparator.comparing(o -> o.getName()));
                    break;
                case EXPERIENCE:
                    objects.sort(Comparator.comparing(o -> -o.getExp()));
                    break;
                case LEVEL_REQ:
                    objects.sort(Comparator.comparing(o -> o.getLevelReq()));
                    break;
            }
        }

    }

    public void update() throws IOException {
        save();
        load();
    }

    @Override
    public Iterator<Type> iterator() {
        return objects.iterator();
    }
}
