package git.doomshade.professions.data;

import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.ISetup;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

public final class Settings implements ISetup {

    public static final HashSet<AbstractSettings> SETTINGS = new HashSet<>();
    private static final String DEFAULT_PROPERTIES = "lang_en.yml";
    protected static FileConfiguration config;
    protected static Professions plugin;
    private static Settings instance;
    private static FileConfiguration lang;
    private static File langFile;
    private static Material editItem = Material.GOLD_NUGGET;

    static {
        plugin = Professions.getInstance();
        instance = new Settings();
        try {
            instance.setup();
        } catch (IOException e) {
            e.printStackTrace();
        }

        registerSettings(new DefaultsSettings());
        registerSettings(new ExpSettings());
        registerSettings(new ItemSettings());
        registerSettings(new ProfessionExpSettings());
        registerSettings(new SaveSettings());
        registerSettings(new TrainableSettings());
        registerSettings(new GUISettings());
        registerSettings(new MaxProfessionsSettings());
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

    public static <A extends Profession<?>> AbstractProfessionSpecificSettings getProfessionSettings(Class<A> clazz) {
        return getProfessionSettings(Professions.getProfessionManager().getProfession(clazz));
    }

    public static <A extends Profession<?>> AbstractProfessionSpecificSettings getProfessionSettings(Profession<?> profession) {
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

    @Override
    public void setup() throws IOException {
        plugin.reloadConfig();

        config = plugin.getConfig();

        editItem = Material.getMaterial(config.getString("edit-item", "GOLD_NUGGET"));

        final File langFolder = plugin.getLangFolder();
        final String langString = config.getString("lang", DEFAULT_PROPERTIES);
        final File[] langs = langFolder.listFiles((dir, name) -> dir != null && dir.getPath().equals(langFolder.getPath()) && name != null && name.startsWith(langString));
        if (langs != null && langs.length > 0) {
            langFile = langs[0];
        } else {
            Professions.createResource(Professions.LANG_PATH + DEFAULT_PROPERTIES, false);
            Professions.log("No language starting with '" + langString + "' exists. Using default language (" + DEFAULT_PROPERTIES + ")");
            langFile = new File(plugin.getLangFolder(), DEFAULT_PROPERTIES);
        }
        lang = new YamlConfiguration();
        try {
            lang = YamlConfiguration.loadConfiguration(langFile);
        } catch (Exception e) {
            throw new IOException("Could not load language settings!", e);
        }
    }
}
