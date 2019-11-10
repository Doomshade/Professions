package git.doomshade.professions.data;

import git.doomshade.professions.Profession;
import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.ISetup;
import git.doomshade.professions.utils.Utils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;

public final class Settings implements ISetup {

    public static final HashSet<AbstractSettings> SETTINGS = new HashSet<>();
    protected static FileConfiguration config;
    protected static Professions plugin;
    private static Settings instance;

    static {
        plugin = Professions.getInstance();
        instance = new Settings();
        instance.reload();

        registerSettings(new DefaultsSettings());
        registerSettings(new ExpSettings());
        registerSettings(new ItemSettings());
        registerSettings(new ProfessionExpSettings());
        registerSettings(new SaveSettings());
        registerSettings(new TrainableSettings());
        /*for (Profession<?> prof : Professions.getProfessionManager().getProfessionsById().values()) {
            registerSettings(new ProfessionSettings<>(prof));
        }*/
    }

    private Settings() {
    }

    static void registerSettings(AbstractSettings settings) {
        SETTINGS.add(settings);
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

    public static <A extends Profession<?>> ProfessionSettings<A> getProfessionSettings(Class<A> clazz) {
        try {
            return (ProfessionSettings<A>) Utils.findInIterable(SETTINGS, x -> x.getClass().getName().equals(clazz.getName()));
        } catch (Utils.SearchNotFoundException e) {
            throw new IllegalArgumentException(clazz + " settings is not a registered profession settings!");
        }
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    @Override
    public void setup() {
        reload();
    }

}
