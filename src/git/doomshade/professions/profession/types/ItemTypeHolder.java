package git.doomshade.professions.profession.types;

import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.SortType;
import git.doomshade.professions.utils.ItemUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class ItemTypeHolder<Type extends ItemType<?>> {
    private static final String ERROR_MESSAGE = "error-message", SORTED_BY = "sorted-by";
    private final List<SortType> sortTypes = new ArrayList<>();
    private boolean isInitialized = false;

    // must be list for ordering in file
    private List<Type> objects = new ArrayList<>();
    private List<String> errorMessage = new ArrayList<>();

    public abstract void init();

    public List<String> getErrorMessage() {
        return errorMessage;
    }

    public final void registerObject(Type item) {
        if (!objects.contains(item)) {
            objects.add(item);
        }
    }

    /**
     * @return all registered item types
     */
    public final List<Type> getRegisteredItemTypes() {
        return objects;
    }

    /**
     * Adds defaults and saves files
     * @throws IOException
     */
    public final void save() throws IOException {
        File itemFile = objects.get(0).getFiles()[0];
        FileConfiguration loader = YamlConfiguration.loadConfiguration(itemFile);
        loader.addDefault(ERROR_MESSAGE, errorMessage);
        loader.addDefault(SORTED_BY, Settings.getInstance().getProfessionSettings().getSortedBy());
        ConfigurationSection itemsSection;
        if (loader.isConfigurationSection(ItemType.KEY)) {
            itemsSection = loader.getConfigurationSection(ItemType.KEY);
        } else {
            itemsSection = loader.createSection(ItemType.KEY);
        }
        for (int i = 0; i < objects.size(); i++) {
            Type registeredObject = objects.get(i);
            itemsSection.addDefault(String.valueOf(i), registeredObject.serialize());
        }
        loader.options().copyDefaults(true);
        loader.save(itemFile);
    }

    public final boolean isInitialized() {
        return isInitialized;
    }

    public final void setInitialized(boolean init) {
        this.isInitialized = init;
    }

    @SuppressWarnings("unchecked")
    public void load() {
        // TODO Auto-generated method stub
        Type object = objects.get(0);
        File itemFile = object.getFiles()[0];
        FileConfiguration loader = YamlConfiguration.loadConfiguration(itemFile);
        this.errorMessage = ItemUtils.getDescription(object, loader.getStringList(ERROR_MESSAGE), null);

        this.sortTypes.clear();
        for (String st : loader.getStringList(SORTED_BY)) {
            SortType sortType = SortType.getSortType(st);
            if (sortType != null) {
                this.sortTypes.add(sortType);
            }
        }
        for (int i = 0; i < objects.size(); i++) {
            Type registeredObject = objects.get(i);
            Type deserialized = (Type) ItemType.deserialize(registeredObject.getClass(), i);
            if (deserialized != null) {
                objects.set(i, deserialized);
            } else {
                throw new RuntimeException("Could not deserialize an object! (" + objects.get(i) + ")");
            }
        }

        for (int i = sortTypes.size() - 1; i >= 0; i--) {
            switch (sortTypes.get(i)) {
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
}
