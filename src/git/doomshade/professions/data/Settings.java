package git.doomshade.professions.data;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.Setup;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;

public class Settings implements Setup {

    private static MiningSettings miningSettings;

    public static final ArrayList<Settings> SETTINGS = new ArrayList<>();
    protected static FileConfiguration config;
    protected static Professions plugin;
    private static Settings instance;
    private static ExpSettings expSettings;
    private static SaveSettings saveSettings;
    private static ItemSettings itemSettings;
    private static ProfessionSettings professionSettings;

    static {
        plugin = Professions.getInstance();
        instance = new Settings();
        instance.loadConfig();
        SETTINGS.add(instance);
        SETTINGS.add(expSettings = new ExpSettings());
        SETTINGS.add(saveSettings = new SaveSettings());
        SETTINGS.add(itemSettings = new ItemSettings());
        SETTINGS.add(professionSettings = new ProfessionSettings());
        SETTINGS.add(miningSettings = new MiningSettings());
    }

    Settings() {
    }


    public static final Settings getInstance() {
        return instance;
    }

    protected final boolean isSection(String section, Object value) {
        boolean isSection = config.isConfigurationSection(section);
        if (!isSection) {
            printError(section, value);
        }
        return isSection;
    }

    protected final boolean isSection(String section) {
        return isSection(section, null);
    }

    private final void loadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public final void reload() {
        try {
            setup();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public final ExpSettings getExpSettings() {
        return expSettings;
    }

    public final SaveSettings getSaveSettings() {
        return saveSettings;
    }

    public final ItemSettings getItemSettings() {
        return itemSettings;
    }

    public final ProfessionSettings getProfessionSettings() {
        return professionSettings;
    }

    public final MiningSettings getMiningSettings() {
        return miningSettings;
    }

    /*protected final void printError(Object value, String... section){
        plugin.sendConsoleMessage("Your configuration file is outdated!");
        plugin.sendConsoleMessage(String.format("Missing \"%s\" section!", Arrays.asList(section).stream().map(x -> x + ".").collect(Collectors.toList())));
        if (value == null)
            plugin.sendConsoleMessage("Using default values.");
        else
            plugin.sendConsoleMessage(String.format("Using %s as default value.", value.toString()));
    }*/

    protected final void printError(String section, Object value) {
        plugin.sendConsoleMessage("Your configuration file is outdated!");
        plugin.sendConsoleMessage(String.format("Missing \"%s\" section!", section));
        if (value == null)
            plugin.sendConsoleMessage("Using default values.");
        else
            plugin.sendConsoleMessage(String.format("Using %s as default value.", value.toString()));
    }

    @Override
    public void setup() throws Exception {
        // TODO Auto-generated method stub
        loadConfig();
    }

}
