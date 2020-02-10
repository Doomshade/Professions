package git.doomshade.professions;


import git.doomshade.guiapi.GUIApi;
import git.doomshade.guiapi.GUIManager;
import git.doomshade.professions.commands.AbstractCommandHandler;
import git.doomshade.professions.commands.CommandHandler;
import git.doomshade.professions.data.AbstractSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.dynmap.MarkerManager;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.EventManager;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.gui.adminguis.AdminProfessionGUI;
import git.doomshade.professions.gui.adminguis.AdminProfessionsGUI;
import git.doomshade.professions.gui.playerguis.PlayerProfessionsGUI;
import git.doomshade.professions.gui.playerguis.ProfessionGUI;
import git.doomshade.professions.gui.playerguis.ProfessionTrainerGUI;
import git.doomshade.professions.gui.playerguis.TestThreeGui;
import git.doomshade.professions.listeners.PluginProfessionListener;
import git.doomshade.professions.listeners.ProfessionListener;
import git.doomshade.professions.listeners.SkillAPIListener;
import git.doomshade.professions.listeners.UserListener;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.profession.types.crafting.alchemy.commands.AlchemyCommandHandler;
import git.doomshade.professions.profession.types.crafting.jewelcrafting.commands.JewelcraftingCommandHandler;
import git.doomshade.professions.profession.types.gathering.herbalism.commands.HerbalismCommandHandler;
import git.doomshade.professions.profession.types.mining.commands.MiningCommandHandler;
import git.doomshade.professions.task.BackupTask;
import git.doomshade.professions.task.SaveTask;
import git.doomshade.professions.trait.ProfessionTrainerTrait;
import git.doomshade.professions.user.User;
import git.doomshade.professions.utils.ISetup;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.bukkit.DynmapPlugin;
import org.fusesource.jansi.Ansi;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.logging.Level;

/**
 * The Main API class as well as the {@link JavaPlugin} class.
 * Professions is an API for plugin developers that want to create their own customizable professions. This plugin also includes a set of examples of custom professions that's function can be disabled in config.
 *
 * @author Doomshade
 */
public final class Professions extends JavaPlugin implements ISetup {

    private static Professions instance;
    private static ProfessionManager profMan;
    private static EventManager eventMan;
    private static GUIManager guiManager;
    private static PermissionManager permMan;
    private static Economy econ;

    // 5 minutes
    private final int SAVE_DELAY = 5 * 60;

    // 1 hr
    private final int BACKUP_DELAY = 60 * 60;

    private static final ArrayList<ISetup> SETUPS = new ArrayList<>();
    private final File BACKUP_FOLDER = new File(getDataFolder(), "backup");
    private static PrintStream fos;
    private final File PLAYER_FOLDER = new File(getDataFolder(), "playerdata");
    private final File CONFIG_FILE = new File(getDataFolder(), "config.yml");
    private final File CACHE_FOLDER = new File(getDataFolder(), "cache");
    private final File LOGS_FOLDER = new File(getDataFolder(), "logs");
    private final File LOG_FILE = new File(getLogsFolder(), String.format("%s.txt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy H_m"))));
    private final File FILTERED_LOGS_FOLDER = new File(getDataFolder(), "filtered logs");
    private final File ITEM_FOLDER = new File(getDataFolder(), "itemtypes");
    private final File PROFESSION_FOLDER = new File(getDataFolder(), "professions");
    public static final String LANG_PATH = "lang/";

    private FileConfiguration configLoader;
    private final File LANG_FOLDER = new File(getDataFolder(), "lang");


    /**
     * {@link net.milkbowl.vault.Vault}'s {@link Economy} instance
     *
     * @return the {@link Economy} instance
     */
    public static Economy getEconomy() {
        return econ;
    }

    /**
     * {@link GUIApi}'s {@link GUIManager} instance
     *
     * @return the {@link GUIManager} instance
     */
    public static GUIManager getGUIManager() {
        return guiManager;
    }

    @Nullable
    public static MarkerManager getMarkerManager() {
        return MarkerManager.getInstance();
    }

    @Nullable
    public static PermissionManager getPermissionManager() {
        return permMan;
    }

    /**
     * The instance of this plugin
     *
     * @return instance of this class
     */
    public static Professions getInstance() {
        return instance;
    }

    private static void setInstance(Professions instance) {
        Professions.instance = instance;
    }

    /**
     * Saves user data
     *
     * @throws IOException ex
     * @see User#saveUsers()
     */
    public static void saveUsers() throws IOException {
        User.saveUsers();
    }


    /**
     * @return the {@link ProfessionManager} instance
     */
    public static ProfessionManager getProfessionManager() {
        return profMan;
    }

    /**
     * @param name the name of Profession to look for
     * @return the profession
     * @see ProfessionManager#getProfession(String)
     */
    public static Profession<?> getProfession(String name) {
        return profMan.getProfession(name);
    }

    /**
     * Calls {@link #getProfession(String)} with the {@link ItemStack}'s display name
     *
     * @param item the item to look for the profession from
     * @return the profession
     * @see #getProfession(String)
     */
    public static Profession<?> getProfession(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return null;
        }
        return getProfession(item.getItemMeta().getDisplayName());
    }

    /**
     * @return instance of {@link EventManager}
     */
    public static EventManager getEventManager() {
        return eventMan;
    }

    /**
     * @param player the player to get {@link User} instance from
     * @return the {@link User} instance
     * @see User#getUser(Player)
     */
    public static User getUser(Player player) {
        return User.getUser(player);
    }

    /**
     * @param uuid the uuid of player
     * @return the {@link User} instance
     * @see User#getUser(UUID)
     */
    public static User getUser(UUID uuid) {
        return User.getUser(uuid);
    }

    /**
     * @param user the user to unload
     * @throws IOException
     * @see User#unloadUser(User)
     */
    public static void unloadUser(User user) throws IOException {
        User.unloadUser(user);
    }

    /**
     * @param player the player to load
     * @see User#loadUser(Player)
     */
    public static void loadUser(Player player) throws IOException {
        User.loadUser(player);
    }

    /**
     * Logs an object using {@link Object#toString()} method. Use {@link Level#CONFIG} to log into log file.
     *
     * @param object the object to log
     * @param level  the level
     */
    public static void log(Object object, Level level) {
        log(object == null ? "null" : object.toString(), level);
    }

    /**
     * Logs an object using {@link Object#toString()} method to console with {@link Level#INFO} level.
     *
     * @param object the object to log
     */
    public static void log(Object object) {
        log(object == null ? "null" : object.toString());
    }

    /**
     * Logs a message to console with {@link Level#INFO} level.
     *
     * @param message the message to display
     */
    public static void log(String message) {
        log(message, Level.INFO);
    }

    /**
     * Logs a message. Use {@link Level#CONFIG} to log into log file.
     *
     * @param message the message to display
     * @param level   the log level
     */
    public static void log(String message, Level level) {
        if (message.isEmpty()) {
            return;
        }
        if (level == Level.CONFIG) {
            String time = String.format("[%s] ", LocalTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(Locale.GERMAN)));

            if (fos == null) {
                try {
                    fos = new PrintStream(getInstance().LOG_FILE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return;
                }
            }
            fos.println(time.concat(ChatColor.stripColor(message)));

            // TODO delete on plugin release!
            fos.flush();
        } else {
            Ansi.Color color = Ansi.Color.WHITE;

            final List<Integer> RED = Arrays.asList(Level.WARNING.intValue(), Level.SEVERE.intValue());
            final List<Integer> GREEN = Arrays.asList(Level.FINE.intValue(), Level.FINER.intValue());

            if (RED.contains(level.intValue())) {
                color = Ansi.Color.RED;
            }
            if (GREEN.contains(level.intValue())) {
                color = Ansi.Color.GREEN;
            }

            Ansi ansi = Ansi.ansi().boldOff();

            getInstance().getLogger().log(level, ansi.fg(color).toString() + message + ansi.fg(Ansi.Color.WHITE));
        }

    }

    public static void createResource(String resource, boolean replace) {
        instance.saveResource(resource, replace);
    }

    /**
     * Cancels the tasks and saves user files
     */
    @Override
    public void onDisable() {
        for (ItemTypeHolder<?> holder : profMan.getItemTypeHolders()) {
            for (ItemType<?> itemType : holder) {
                itemType.onDisable();
            }
        }
        Bukkit.getScheduler().cancelTasks(this);
        try {
            saveFiles();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves only user files (currently)
     *
     * @throws IOException ex
     */
    public void saveFiles() throws IOException {
        saveUsers();
        fos.flush();
    }


    /**
     * Reloads the config
     */
    @Override
    public void reloadConfig() {
        configLoader = YamlConfiguration.loadConfiguration(CONFIG_FILE);
    }

    @Override
    public FileConfiguration getConfig() {
        return configLoader;
    }

    /**
     * Sets the instance of this plugin, attempts to hook {@link GUIApi}, {@link Citizens}, and {@link net.milkbowl.vault.Vault}, sets up instances of managers, sets up files, schedules tasks, and registers events of listeners.
     */
    @Override
    public void onEnable() {
        setInstance(this);
        hookGuiApi();
        hookCitizens();
        hookSkillAPI();
        hookVault();
        hookPex();

        profMan = ProfessionManager.getInstance();
        eventMan = EventManager.getInstance();

        try {
            setupFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Hook dynmap after setups as it uses config
        hookDynmap();
        scheduleTasks();

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new UserListener(), this);
        pm.registerEvents(new ProfessionListener(), this);
        pm.registerEvents(new PluginProfessionListener(), this);

        for (ItemTypeHolder<?> holder : profMan.getItemTypeHolders()) {
            for (ItemType<?> itemType : holder) {
                itemType.onLoad();
            }
        }
    }


    /**
     * @return the backup folder directory
     */
    public File getBackupFolder() {
        return getFolder(BACKUP_FOLDER);
    }

    /**
     * @return the {@link User} folder directory
     */
    public File getPlayerFolder() {
        return getFolder(PLAYER_FOLDER);
    }

    /**
     * @return the {@link ItemType} folder directory
     */
    public File getItemsFolder() {
        return getFolder(ITEM_FOLDER);
    }

    /**
     * @return the {@link Profession} folder directory
     */
    public File getProfessionFolder() {
        return getFolder(PROFESSION_FOLDER);
    }

    /**
     * @return the {@link User} cache folder directory
     */
    public File getCacheFolder() {
        return getFolder(CACHE_FOLDER);
    }

    /**
     * @return the logs folder directory
     */
    public File getLogsFolder() {
        return getFolder(LOGS_FOLDER);
    }

    /**
     * @return the filtered logs folder directory
     */
    public File getFilteredLogsFolder() {
        return getFolder(FILTERED_LOGS_FOLDER);
    }

    /**
     * @return the lang folder directory
     */
    public File getLangFolder() {
        return getFolder(LANG_FOLDER);
    }

    /**
     * @return the current log file
     */
    public File getLogFile() {
        return LOG_FILE;
    }

    @Override
    public void setup() {
        for (ISetup setup : SETUPS) {
            try {
                setup.setup();
            } catch (Exception e) {
                try {
                    throw new ConfigurationException("Could not load " + setup.getSetupName(), e);
                } catch (ConfigurationException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void cleanup() {
        for (ISetup setup : SETUPS) {
            try {
                setup.cleanup();
            } catch (Exception e) {
                try {
                    throw new ConfigurationException("Could not load " + setup.getSetupName(), e);
                } catch (ConfigurationException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Reloads the plugin
     *
     * @throws IOException if an error occurs
     */
    public void reload() throws IOException {

        // Calls on pre reload on all item types
        for (ItemTypeHolder<?> holder : profMan.getItemTypeHolders()) {
            for (ItemType<?> itemType : holder) {
                itemType.onPreReload();
            }
        }

        // Calls cleanup methods on all classes implementing ISetup
        cleanup();

        // Saves and unloads users

        for (Player p : Bukkit.getOnlinePlayers()) {
            User.unloadUser(p);
        }


        // Sets up all classes again
        setup();

        // Calls on reload on all item types
        for (ItemTypeHolder<?> holder : profMan.getItemTypeHolders()) {
            for (ItemType<?> itemType : holder) {
                itemType.onReload();
            }
        }

        // Finally loads users again
        for (Player p : Bukkit.getOnlinePlayers()) {
            User.loadUser(p);
        }
    }

    /**
     * Registers a setup class. Used only for this plugin's purposes, not the API's!
     *
     * @param setup the setup to register
     */
    public void registerSetup(ISetup setup) {
        if (!SETUPS.contains(setup))
            SETUPS.add(setup);
    }

    /**
     * Backs up all data into a zip file.
     *
     * @return the result of backup
     */
    public BackupTask.Result backup() {
        BackupTask task = new BackupTask();
        task.run();
        return task.getResult();
    }

    /**
     * Creates a folder if possible
     *
     * @param file the folder to create
     * @return the same file
     */
    private File getFolder(File file) {
        if (!file.isDirectory()) {
            file.mkdirs();
        }
        CraftPlayer p;
        return file;
    }

    /**
     * Registers all setups
     */
    private void registerSetups() {
        try {
            registerSetup(Settings.getInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (AbstractSettings s : Settings.SETTINGS) {
            try {
                registerSetup(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            registerSetup(Messages.getInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
        registerCommandHandler(CommandHandler.class);
        registerCommandHandler(MiningCommandHandler.class);
        registerCommandHandler(HerbalismCommandHandler.class);
        registerCommandHandler(AlchemyCommandHandler.class);
        registerCommandHandler(JewelcraftingCommandHandler.class);

        registerSetup(ProfessionManager.getInstance());
    }

    private void registerCommandHandler(Class<? extends AbstractCommandHandler> commandHandler) {
        registerSetup(CommandHandler.getInstance(commandHandler));
    }

    private void setupFiles() throws IllegalArgumentException {
        if (!getDataFolder().isDirectory()) {
            getDataFolder().mkdir();
        }

        // getters create folders if the dir doesn't exist (this prevents random null/FileNotFound exceptions)
        getBackupFolder();
        getPlayerFolder();
        getItemsFolder();
        getProfessionFolder();
        getCacheFolder();
        getFilteredLogsFolder();
        // getLogsFolder();
        if (!LOG_FILE.exists()) {
            try {
                LOG_FILE.createNewFile();
                fos = new PrintStream(LOG_FILE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        saveResource(LANG_PATH.concat("patterns.properties"), true);

        saveResource(LANG_PATH.concat("lang_cs.yml"), false);
        saveResource(LANG_PATH.concat("lang_cs_D.yml"), false);
        saveResource(LANG_PATH.concat("lang_en.yml"), false);

        saveDefaultConfig();
        reloadConfig();
        registerSetups();
        setup();

    }

    private void scheduleTasks() {
        new SaveTask().runTaskTimer(this, SAVE_DELAY * 20L, SAVE_DELAY * 20L);
        new BackupTask().runTaskTimer(this, BACKUP_DELAY * 20L, BACKUP_DELAY * 20L);
    }

    private void hookGuiApi() {
        guiManager = GUIApi.getGuiManager(this);
        guiManager.registerGui(PlayerProfessionsGUI.class);
        guiManager.registerGui(ProfessionGUI.class);
        guiManager.registerGui(TestThreeGui.class);
        guiManager.registerGui(ProfessionTrainerGUI.class);
        guiManager.registerGui(AdminProfessionsGUI.class);
        guiManager.registerGui(AdminProfessionGUI.class);
        hookMessage("GUIApi");
    }

    private void hookCitizens() {
        if (Bukkit.getPluginManager().getPlugin("Citizens") == null) {
            return;
        }
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ProfessionTrainerTrait.class));
        hookMessage("Citizens");
    }

    private void hookVault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }

        hookMessage("Vault");
        econ = rsp.getProvider();
    }

    /**
     * Hooks SkillAPI
     */
    private void hookSkillAPI() {
        PluginManager pm = Bukkit.getPluginManager();
        if (pm.getPlugin("SkillAPI") == null) {
            return;
        }
        pm.registerEvents(new SkillAPIListener(), this);
    }

    private void hookPex() {
        PluginManager pm = Bukkit.getPluginManager();
        if (pm.getPlugin("PermissionsEx") == null) {
            permMan = null;
        } else {
            permMan = PermissionsEx.getPermissionManager();
        }
    }

    private void hookDynmap() {
        PluginManager pm = Bukkit.getPluginManager();
        if (pm.getPlugin("dynmap") == null) {
            return;
        }

        // sets the dynmap plugin to marker manager
        MarkerManager.getInstance(DynmapPlugin.plugin);
    }

    private void hookMessage(String plugin) {
        log(String.format("Sucessfully hooked with %s plugin", plugin), Level.INFO);
    }


    /**
     * Overridden method from {@link JavaPlugin#saveResource(String, boolean)}, removes unnecessary message and made only to save text resources in UTF-16 formatting.
     *
     * @param resourcePath the path of file
     * @param replace      whether or not to replace if the file already exists
     */
    @Override
    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath != null && !resourcePath.equals("")) {
            resourcePath = resourcePath.replace('\\', '/');
            Reader in = this.getTextResource(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + super.getFile());
            } else {
                File outFile = new File(this.getDataFolder(), resourcePath);
                int lastIndex = resourcePath.lastIndexOf(47);
                File outDir = new File(this.getDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }

                try {
                    if (!outFile.exists() || replace) {

                        // NOPES: UTF-16, ISO, UTF-16BE
                        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_16);
                        char[] buf = new char[1024];

                        int len;

                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        out.close();
                        in.close();
                    }
                } catch (IOException var10) {
                    var10.printStackTrace();
                    log("Could not save " + outFile.getName() + " to " + outFile);
                }

            }
        } else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }
}
