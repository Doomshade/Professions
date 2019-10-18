package git.doomshade.professions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import git.doomshade.professions.profession.professions.EnchantingProfession;
import git.doomshade.professions.profession.professions.JewelcraftingProfession;
import git.doomshade.professions.profession.professions.MiningProfession;
import git.doomshade.professions.profession.professions.SkinningProfession;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.profession.types.crafting.CustomRecipeHolder;
import git.doomshade.professions.profession.types.crafting.ICrafting;
import git.doomshade.professions.profession.types.enchanting.EnchantedItemTypeHolder;
import git.doomshade.professions.profession.types.enchanting.IEnchanting;
import git.doomshade.professions.profession.types.gathering.GatherItemHolder;
import git.doomshade.professions.profession.types.gathering.IGathering;
import git.doomshade.professions.profession.types.hunting.IHunting;
import git.doomshade.professions.profession.types.hunting.PreyHolder;
import git.doomshade.professions.profession.types.mining.IMining;
import git.doomshade.professions.profession.types.mining.OreHolder;
import git.doomshade.professions.utils.Backup;
import git.doomshade.professions.utils.Setup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;

public final class ProfessionManager implements Setup, Backup {
    private static ProfessionManager instance;
    @SuppressWarnings("rawtypes")
    final HashSet<Class<? extends Profession>> REGISTERED_PROFESSIONS = new HashSet<>();
    final HashSet<Class<? extends IProfessionType>> PROFESSION_TYPES = new HashSet<>();
    final HashSet<ItemType<?>> ITEMTYPES = new HashSet<>();
    final HashSet<ItemTypeHolder<?>> ITEMTYPEHOLDERS = new HashSet<>();
    private final PluginManager pm = Bukkit.getPluginManager();
    private final Professions plugin = Professions.getInstance();
    Map<String, Profession<? extends IProfessionType>> PROFESSIONS_ID = new HashMap<>();
    Map<String, Profession<? extends IProfessionType>> PROFESSIONS_NAME = new HashMap<>();
    private File file;

    private ProfessionManager() {
    }

    public static ProfessionManager getInstance() {
        if (instance == null) {
            instance = new ProfessionManager();
        }
        return instance;
    }

    public ImmutableSet<ItemTypeHolder<?>> getItemTypeHolders() {
        return ImmutableSet.copyOf(ITEMTYPEHOLDERS);
    }

    public ImmutableSet<ItemType<?>> getItemTypes() {
        return ImmutableSet.copyOf(ITEMTYPES);
    }

    public ImmutableSet<Class<? extends IProfessionType>> getProfessionTypes() {
        return ImmutableSet.copyOf(PROFESSION_TYPES);
    }

    @SuppressWarnings("rawtypes")
    public ImmutableSet<Class<? extends Profession>> getRegisteredProfessions() {
        return ImmutableSet.copyOf(REGISTERED_PROFESSIONS);
    }

    public ImmutableMap<String, Profession<? extends IProfessionType>> getProfessionsById() {
        return ImmutableMap.copyOf(PROFESSIONS_ID);
    }

    public ImmutableMap<String, Profession<? extends IProfessionType>> getProfessionsByName() {
        return ImmutableMap.copyOf(PROFESSIONS_NAME);
    }

    public void updateProfessions() {
        PROFESSIONS_ID.forEach((y, x) -> updateProfession(x));
        sortProfessions();
    }

    @Override
    public void setup() throws IOException {
        register();
        createProfessionsFile();
        registerProfessions();
        setupProfessionsFile();

    }

    private void register() {
        registerProfessionTypes();
        registerItemTypeHolders();
    }

    private void registerItemTypeHolders() {
        registerItemTypeHolder(OreHolder.class);
        registerItemTypeHolder(PreyHolder.class);
        registerItemTypeHolder(GatherItemHolder.class);
        registerItemTypeHolder(EnchantedItemTypeHolder.class);
        registerItemTypeHolder(CustomRecipeHolder.class);
        updateItemTypeHolders();
    }

    public void updateItemTypeHolders() {
        for (ItemTypeHolder<?> type : ITEMTYPEHOLDERS) {
            if (!type.isInitialized()) {
                type.init();
                try {
                    type.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                type.load();
                type.setInitialized(true);
            }
        }
    }

    public <T extends ItemTypeHolder<?>> void registerItemTypeHolder(Class<T> itemTypeHolder) {
        try {
            Constructor<T> c = itemTypeHolder.getDeclaredConstructor();
            c.setAccessible(true);
            T holder = c.newInstance();
            ITEMTYPEHOLDERS.add(holder);
            String className = holder.getClass().getGenericSuperclass().toString();
            String[] split = className.split("[.]");

            String name = "";
            for (int i = 5; i < split.length; i++) {
                name += split[i] + ".";
            }
            registerItemType(Class.forName(name.substring(15, name.length() - 2)));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void registerProfessionTypes() {
        registerProfessionType(IMining.class);
        registerProfessionType(IHunting.class);
        registerProfessionType(IGathering.class);
        registerProfessionType(IEnchanting.class);
        registerProfessionType(ICrafting.class);
    }

    private void createProfessionsFile() throws IOException {
        file = new File(Professions.getInstance().getDataFolder(), "professions.yml");
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    private void registerProfessions() {
        registerProfession(new MiningProfession());
        registerProfession(new JewelcraftingProfession());
        registerProfession(new EnchantingProfession());
        registerProfession(new SkinningProfession());
        sortProfessions();
    }

    public Profession<?> fromName(String string) {
        if (string.isEmpty()) {
            return null;
        }
        Profession<?> prof = PROFESSIONS_ID.get(string.toLowerCase());
        if (prof == null) {
            return PROFESSIONS_NAME.get(ChatColor.stripColor(string.toLowerCase()));
        }
        return prof;
    }

    public Profession<? extends IProfessionType> getProfession(Class<? extends IProfessionType> profType) {
        for (Profession<? extends IProfessionType> prof : PROFESSIONS_ID.values()) {
            if (prof.getClass().getSimpleName().equals(profType.getSimpleName())) {
                return prof;
            }
        }
        throw new IllegalStateException(profType.getSimpleName()
                + " is not a registered profession somehow. Contact any of the following authors: "
                + Professions.getInstance().getDescription().getAuthors());
    }

    private void registerProfession(Profession<? extends IProfessionType> prof) {
        if (!Profession.INITED_PROFESSIONS.contains(prof.getClass())) {
            try {
                throw new IllegalAccessException("Do not override nor create any new constructors in your profession class!");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        prof.onLoad();
        PROFESSIONS_ID.forEach((y, x) -> {
            if (x.getID().equalsIgnoreCase(prof.getID())) {
                Bukkit.getConsoleSender()
                        .sendMessage(ChatColor.DARK_RED + "ERROR:" + ChatColor.RED + " A profession with name "
                                + prof.getName() + ChatColor.RESET + " already exists! (" + prof.getID() + ")");
                return;
            }
        });
        PROFESSIONS_ID.put(prof.getID().toLowerCase(), prof);
        PROFESSIONS_NAME.put(ChatColor.stripColor(prof.getColoredName().toLowerCase()), prof);
        if (!REGISTERED_PROFESSIONS.contains(prof.getClass())) {
            pm.registerEvents(prof, plugin);
            REGISTERED_PROFESSIONS.add(prof.getClass());
        }
        try {
            updateProfession(prof);
            Professions.getInstance()
                    .sendConsoleMessage("Registered " + prof.getColoredName() + ChatColor.RESET + " profession");
        } catch (IllegalArgumentException e) {
            Professions.getInstance().sendConsoleMessage("Could not update " + prof.getID() + " profession. Reason:");
            e.printStackTrace();
        }
    }

    public void registerProfession(Class<Profession<? extends IProfessionType>> prof) {
        try {
            registerProfession(prof.newInstance());
        } catch (Exception e) {
            Professions.getInstance().sendConsoleMessage("Do not override nor create any new constructors in your profession class!");
            e.printStackTrace();
        }
    }

    public void registerProfessionType(Class<? extends IProfessionType> clazz) {
        PROFESSION_TYPES.add(clazz);
    }

    public void updateProfession(Profession<? extends IProfessionType> prof) throws IllegalArgumentException {
        FileConfiguration loader = YamlConfiguration.loadConfiguration(file);
        if (!loader.getKeys(false).contains(prof.getID())) {
            return;
        }
        Profession<?> fileProf = Profession
                .deserialize(loader.getConfigurationSection(prof.getID()).getValues(false));
        prof.setName(fileProf.getName());
        prof.setProfessionType(fileProf.getProfessionType());
        prof.onPostLoad();
    }

    private void setupProfessionsFile() throws IOException {
        FileConfiguration loader = YamlConfiguration.loadConfiguration(file);
        Map<String, Object> defaults = new HashMap<>();
        for (Entry<String, Profession<?>> prof : PROFESSIONS_ID.entrySet()) {
            defaults.put(prof.getValue().getID(), prof.getValue().serialize());
        }
        loader.options().copyDefaults(true);
        loader.addDefaults(defaults);
        loader.save(file);
    }

    private void sortProfessions() {
        Map<String, Profession<? extends IProfessionType>> MAP_COPY = new HashMap<String, Profession<? extends IProfessionType>>(
                PROFESSIONS_ID);
        PROFESSIONS_ID = sortByValue(MAP_COPY);
        MAP_COPY = new HashMap<>(PROFESSIONS_NAME);
        PROFESSIONS_NAME = sortByValue(MAP_COPY);
    }

    private Map<String, Profession<? extends IProfessionType>> sortByValue(
            Map<String, Profession<? extends IProfessionType>> unsortMap) {

        List<Entry<String, Profession<? extends IProfessionType>>> list = new LinkedList<>(
                unsortMap.entrySet());

        Collections.sort(list, Comparator.comparing(o -> o.getValue().getName()));
        Map<String, Profession<? extends IProfessionType>> sortedMap = new LinkedHashMap<>();
        for (Entry<String, Profession<? extends IProfessionType>> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    @Override
    public File[] getFiles() {
        return new File[]{file};
    }

    /**
     * Registers an item type
     *
     * @param itemType the item type to register
     */
    private void registerItemType(Class<?> itemType) {
        try {
            Constructor<?> c = itemType.getDeclaredConstructor();
            c.setAccessible(true);
            ITEMTYPES.add((ItemType<?>) c.newInstance());
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Professions.getInstance().sendConsoleMessage("Could not register " + itemType.getSimpleName()
                    + " as it does not implement a no-args constructor!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
