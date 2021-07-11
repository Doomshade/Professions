package git.doomshade.professions.data;

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.io.IOManager;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.ISetup;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static git.doomshade.professions.data.AbstractSettings.LEVEL;
import static git.doomshade.professions.data.AbstractSettings.outdated;

/**
 * The main settings class that registers all setting instances. Also manages settings in the root path.
 *
 * @author Doomshade
 * @version 1.0
 */
public final class Settings implements ISetup {

    private static final HashSet<AbstractSettings> SETTINGS = new HashSet<>();
    private static final String DEFAULT_PROPERTIES = "lang_en.yml";
    protected static FileConfiguration config;
    protected static Professions plugin;
    private static Settings instance;
    private static FileConfiguration lang;
    private static File langFile;
    private static Material editItem = Material.GOLD_NUGGET;
    private static boolean autoSave = true;
    private static boolean handleMineEvents = false;

    // TODO: 08.04.2020 make this a BossBarOptions and create a new path for bossbars (adding the customizability for BossBars)
    private static boolean useBossBar = false;
    private static Collection<String> miningWorlds = new HashSet<>();

    static {
        plugin = Professions.getInstance();
        instance = new Settings();
        plugin.reloadConfig();
        config = plugin.getConfig();

        registerSettings(new DefaultsSettings());
        registerSettings(new ExpSettings());
        registerSettings(new ItemSettings());
        registerSettings(new ProfessionExpSettings());
        registerSettings(new SaveSettings());
        registerSettings(new TrainableSettings());
        registerSettings(new GUISettings());
        registerSettings(new MaxProfessionsSettings());
    }

    public static Collection<AbstractSettings> getSettings() {
        return Collections.unmodifiableCollection(SETTINGS);
    }

    private Settings() {
    }

    public static void registerSettings(AbstractSettings settings) {
        SETTINGS.add(settings);
    }

    public static void unregisterSettings(AbstractSettings settings) {
        SETTINGS.remove(settings);
    }

    public static Settings getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractSettings> T getSettings(Class<T> clazz) {
        try {
            return (T) Utils.findInIterable(SETTINGS, x -> x.getClass().getName().equals(clazz.getName()));
        } catch (Utils.SearchNotFoundException e) {
            throw new IllegalArgumentException(clazz + " settings is not a registered settings!");
        }
    }

    public static <A extends Profession> AbstractProfessionSpecificSettings getProfessionSettings(Class<A> clazz) throws IllegalArgumentException {
        final Optional<Profession> opt = Professions.getProfMan().getProfession(clazz);
        return getProfessionSettings(opt.orElseThrow(() -> new IllegalArgumentException(clazz.getSimpleName() + " is not a registered profession!")));
    }

    public static <A extends Profession> AbstractProfessionSpecificSettings getProfessionSettings(Profession profession) {
        try {
            return (AbstractProfessionSpecificSettings) Utils.findInIterable(SETTINGS, x -> {
                if (x instanceof AbstractProfessionSpecificSettings) {
                    AbstractProfessionSpecificSettings settings = (AbstractProfessionSpecificSettings) x;
                    return settings.getProfession().equals(profession);
                }
                return false;
            });
        } catch (Utils.SearchNotFoundException e) {
            throw new IllegalArgumentException(profession.getColoredName() + " settings is not a registered profession settings!");
        }
    }

    public static FileConfiguration getLang() {
        return lang;
    }

    public static File getLangFile() {
        return langFile;
    }

    public static Material getEditItem() {
        return editItem;
    }

    public static boolean isAutoSave() {
        return autoSave;
    }

    public static Collection<String> getMiningWorlds() {
        return miningWorlds;
    }

    public static boolean isHandleMineEvents() {
        return handleMineEvents;
    }

    public static boolean isUseBossBar() {
        return useBossBar;
    }

    protected void printError(String section, Object value) {
        if (!outdated) {
            ProfessionLogger.log("Your configuration file is outdated!", LEVEL);
            outdated = true;
        }
        ProfessionLogger.log(String.format("Missing \"%s\" variable!", section), LEVEL);
        if (value == null)
            ProfessionLogger.log("Using default values.", LEVEL);
        else
            ProfessionLogger.log(String.format("Using %s as default value.", value.toString()), LEVEL);
    }

    private <T> T setupVariable(String configPath, T defaultCase, UnaryOperator<T> extraAction) {
        T value = (T) config.get(configPath, defaultCase);
        if (extraAction != null) {
            value = extraAction.apply(value);
        }

        if (!config.contains(configPath)) {
            printError(configPath, defaultCase);
        }
        return value;
    }

    private <T> T setupVariable(String configPath, T defaultCase) {
        return setupVariable(configPath, defaultCase, null);
    }

    @Override
    public void setup() throws IOException {

        plugin.reloadConfig();
        config = plugin.getConfig();

        useBossBar = setupVariable("use-bossbar", false);
        editItem = Material.getMaterial(setupVariable("edit-item", "GOLD_NUGGET"));
        autoSave = setupVariable("auto-save-before-edit", true);
        miningWorlds = setupVariable("mining-worlds",
                // using Collections#checkedSet to get a Set<String> (not a HashSet)
                Collections.checkedList(new ArrayList<>(), String.class),
                // makes the worlds lower case
                x -> x.stream().map(String::toLowerCase).collect(Collectors.toList()));
        handleMineEvents = setupVariable("handle-mine-events", false);

        // setup lang as last as it could throw an exception
        setupLang();

    }

    private void setupLang() throws IOException {
        final File langFolder = IOManager.getLangFolder();
        final String langString = setupVariable("lang", DEFAULT_PROPERTIES);
        final File[] langs = langFolder.listFiles((dir, name) -> dir != null && dir.getPath().equals(langFolder.getPath()) && name != null && name.startsWith(langString));
        if (langs != null && langs.length > 0) {
            langFile = langs[0];
        } else {
            Professions.createResource(IOManager.LANG_PATH + DEFAULT_PROPERTIES, false);
            ProfessionLogger.log("No language starting with '" + langString + "' exists. Using default language (" + DEFAULT_PROPERTIES + ")");
            langFile = new File(langFolder, DEFAULT_PROPERTIES);
        }
        lang = new YamlConfiguration();
        try {
            lang = YamlConfiguration.loadConfiguration(langFile);
        } catch (Exception e) {
            throw new IOException("Could not load language settings!", e);
        }
    }
}
