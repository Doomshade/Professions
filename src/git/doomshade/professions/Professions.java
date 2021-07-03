package git.doomshade.professions;


import git.doomshade.guiapi.GUIApi;
import git.doomshade.guiapi.GUIManager;
import git.doomshade.professions.commands.AbstractCommandHandler;
import git.doomshade.professions.commands.CommandHandler;
import git.doomshade.professions.data.AbstractSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.data.cache.CacheUtils;
import git.doomshade.professions.dynmap.MarkerManager;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.EventManager;
import git.doomshade.professions.exceptions.ConfigurationException;
import git.doomshade.professions.gui.adminguis.AdminProfessionGUI;
import git.doomshade.professions.gui.adminguis.AdminProfessionsGUI;
import git.doomshade.professions.gui.oregui.OreGUI;
import git.doomshade.professions.gui.playerguis.PlayerProfessionsGUI;
import git.doomshade.professions.gui.playerguis.ProfessionGUI;
import git.doomshade.professions.gui.playerguis.TestThreeGui;
import git.doomshade.professions.gui.trainergui.TrainerChooserGUI;
import git.doomshade.professions.gui.trainergui.TrainerGUI;
import git.doomshade.professions.listeners.*;
import git.doomshade.professions.placeholder.PlaceholderManager;
import git.doomshade.professions.profession.Profession;
import git.doomshade.professions.profession.ProfessionManager;
import git.doomshade.professions.profession.professions.alchemy.commands.AlchemyCommandHandler;
import git.doomshade.professions.profession.professions.herbalism.commands.HerbalismCommandHandler;
import git.doomshade.professions.profession.professions.jewelcrafting.commands.JewelcraftingCommandHandler;
import git.doomshade.professions.profession.professions.mining.commands.MiningCommandHandler;
import git.doomshade.professions.profession.professions.mining.spawn.OreEditListener;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.task.BackupTask;
import git.doomshade.professions.task.LogTask;
import git.doomshade.professions.task.SaveTask;
import git.doomshade.professions.trait.TrainerListener;
import git.doomshade.professions.trait.TrainerTrait;
import git.doomshade.professions.user.User;
import git.doomshade.professions.utils.ISetup;
import git.doomshade.professions.utils.ItemUtils;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * The Main API class as well as the {@link JavaPlugin} class.
 *
 * @author Doomshade
 * @version 1.0
 */
public final class Professions extends JavaPlugin implements ISetup {

    private static final int RED = 900;
    private static final int GREEN = 500;
    private static ProfessionManager profMan;
    private static EventManager eventMan;
    private static GUIManager guiManager;
    private static PermissionManager permMan;
    private static Economy econ;

    // 5 minutes
    private static final int SAVE_DELAY = 5 * 60;

    // 1 hr
    private static final int BACKUP_DELAY = 60 * 60;

    // 10 minutes
    private static final int LOG_DELAY = 10 * 60;

    private static final ArrayList<ISetup> SETUPS = new ArrayList<>();
    public static PrintStream fos = null;
    private final File PLAYER_FOLDER = new File(getDataFolder(), "playerdata");
    private final File CONFIG_FILE = new File(getDataFolder(), "config.yml");
    private final File CACHE_FOLDER = new File(getDataFolder(), "cache");
    private final File DATA_FOLDER = new File(getDataFolder(), "data");
    // files
    // TODO: 23.04.2020 perhaps make some IOManager for this someday?
    private final File BACKUP_FOLDER = new File(getDataFolder(), "backup");
    private final File LOGS_FOLDER;
    private final File LOG_FILE;

    private final File FILTERED_LOGS_FOLDER = new File(getDataFolder(), "filtered logs");
    private final File ITEM_FOLDER = new File(getDataFolder(), "itemtypes");
    private final File PROFESSION_FOLDER = new File(getDataFolder(), "professions");
    private final File TRAINER_FOLDER = new File(getDataFolder(), "trainer gui");

    public static final String LANG_PATH = "lang/";
    private final File LANG_FOLDER = new File(getDataFolder(), "lang");
    private FileConfiguration configLoader;
    private static boolean FIRST_BACKUP = true;

    // put it in an initialization block so the order is obvious
    {
        LOGS_FOLDER = new File(getDataFolder(), "logs");
        LOG_FILE = new File(getLogsFolder(), String.format("%s.txt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy H_m"))));
    }

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
    public static Profession getProfession(String name) {
        return profMan.getProfession(name);
    }

    /**
     * Calls {@link #getProfession(String)} with the {@link ItemStack}'s display name
     *
     * @param item the item to look for the profession from
     * @return the profession
     * @see #getProfession(String)
     */
    public static Profession getProfession(ItemStack item) {
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
     * @throws IOException if the unload was unsuccessful
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
        log(object, Level.INFO);
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
     * Calls {@link Professions#logError(Throwable, boolean)} with {@code true} argument
     *
     * @param e the throwable
     */
    public static void logError(Throwable e) {
        logError(e, true);
    }

    public static void logError(Throwable e, boolean pluginError) {
        log((pluginError ? "Internal" : "External") + " plugin error" + (!pluginError ? ", please check logs for further information." : ", please contact author with the stack trace from your log file."), Level.WARNING);
        log(e.getMessage().replaceAll("<br>", "\n") + (pluginError ? ": " + Arrays.toString(e.getStackTrace()) : ""), Level.CONFIG);
    }

    // hooks
    private static Professions instance;
    private static boolean diabloLike = false;

    /**
     * Logs a message. Levels >= {@link Level#CONFIG} (excluding {@link Level#INFO}) will be logged to file.
     * Levels >=900 will be displayed in red. Levels <=500 will be displayed in green.
     *
     * @param message the message to display
     * @param level   the log level
     */
    public static void log(String message, Level level) {
        if (message.isEmpty()) {
            return;
        }

        final int leveli = level.intValue();
        if (leveli >= Level.CONFIG.intValue() && level != Level.INFO) {
            String time = String.format("[%s] ", LocalTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(Locale.GERMAN)));

            if (fos == null) {
                try {
                    fos = new PrintStream(getInstance().getLogFile());
                } catch (FileNotFoundException e) {
                    // DONT CALL #logError HERE!
                    e.printStackTrace();
                    return;
                }
            }

            fos.println(time.concat(ChatColor.stripColor(message)));

            if (level == Level.CONFIG)
                return;
        }
        Ansi.Color color = Ansi.Color.WHITE;

        if (leveli >= RED) {
            color = Ansi.Color.RED;
        } else if (leveli <= GREEN) {
            color = Ansi.Color.GREEN;
        }

        Ansi ansi = Ansi.ansi().boldOff();

        instance.getLogger().log(level, ansi.fg(color).toString() + message + ansi.fg(Ansi.Color.WHITE));

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
        cleanup();
        Bukkit.getScheduler().cancelTasks(this);
        try {
            saveFiles();
        } catch (IOException e) {
            Professions.logError(e);
        } finally {
            fos.close();
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
        hookPlugins();

        profMan = ProfessionManager.getInstance();
        eventMan = EventManager.getInstance();

        try {
            setupFiles();
        } catch (Exception e) {
            Professions.logError(e);
        }

        // Hook dynmap after setups as it uses config
        hookPlugin("dynmap", x -> {
            // sets the dynmap plugin to marker manager
            MarkerManager.getInstance(DynmapPlugin.plugin);
            return true;
        });
        scheduleTasks();

        registerListeners();

        for (ItemTypeHolder<?> holder : profMan.getItemTypeHolders()) {
            for (ItemType<?> itemType : holder) {
                itemType.onLoad();
            }
        }
    }

    private void hookPlugins() {
        hookPlugin("GUIApi", x -> {
            guiManager = GUIApi.getGuiManager(this);
            guiManager.registerGui(PlayerProfessionsGUI.class);
            guiManager.registerGui(ProfessionGUI.class);
            guiManager.registerGui(TestThreeGui.class);
            guiManager.registerGui(AdminProfessionsGUI.class);
            guiManager.registerGui(AdminProfessionGUI.class);
            guiManager.registerGui(OreGUI.class);
            guiManager.registerGui(TrainerGUI.class);
            guiManager.registerGui(TrainerChooserGUI.class);

            // could be unsafe, idk
            registerSetup((ISetup) guiManager.getGui(TrainerGUI.class, null).get());
            return true;
        });
        hookPlugin("Citizens", x -> {
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TrainerTrait.class));
            return true;
        });
        hookPlugin("SkillAPI", x -> {
            Bukkit.getPluginManager().registerEvents(new SkillAPIListener(), this);
            return true;
        });
        hookPlugin("Vault", x -> {
            RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            }

            econ = rsp.getProvider();
            return true;
        });
        hookPlugin("PermissionsEx", x -> {
            permMan = PermissionsEx.getPermissionManager();
            return true;
        });

        hookPlugin("DiabloLike", x -> {
            diabloLike = true;
            return true;
        });

        hookPlugin("PlaceholderAPI", x -> {
            registerSetup(PlaceholderManager.getInstance());
            return true;
        });

    }

    private void hookPlugin(String plugin, Predicate<Plugin> func) {
        final Plugin plug = Bukkit.getPluginManager().getPlugin(plugin);
        if (plug == null) {
            return;
        }

        final boolean bool;
        try {
            bool = func.test(plug);
        } catch (Exception e) {
            log(String.format("Could not hook with %s plugin!", plugin), Level.WARNING);
            Professions.logError(e);
            return;
        }
        if (bool) {
            log(String.format("Successfully hooked with %s plugin", plugin), Level.INFO);
        } else {
            log(String.format("Could not hook with %s plugin", plugin), Level.INFO);
        }
    }

    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new UserListener(), this);
        pm.registerEvents(new ProfessionListener(), this);
        pm.registerEvents(new PluginProfessionListener(), this);
        pm.registerEvents(new OreEditListener(), this);
        pm.registerEvents(new JewelcraftingListener(), this);
        pm.registerEvents(new TrainerListener(), this);
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
     * @return the folder with trainer GUI
     */
    public File getTrainerFolder() {
        return getFolder(TRAINER_FOLDER);
    }

    /**
     * @return the folder with additional data that does not belong to any other file (user, itemtype, ...)
     */
    public File getAdditionalDataFolder() {
        return getFolder(DATA_FOLDER);
    }

    /**
     * @return the current log file
     */
    public File getLogFile() {
        return LOG_FILE;
    }

    public static boolean isDiabloLikeHook() {
        return diabloLike;
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
                    Professions.logError(ex);
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
                    Professions.logError(ex);
                }
            }
        }
    }

    /**
     * Reloads the plugin
     */
    public boolean reload() {

        boolean successful = true;

        // Calls on pre reload on all item types
        for (ItemTypeHolder<?> holder : profMan.getItemTypeHolders()) {
            for (ItemType<?> itemType : holder) {
                try {
                    itemType.onPreReload();
                } catch (Exception e) {
                    String errormsg = "Failed to reload itemtype " + itemType.getName() + ".";
                    log(errormsg.concat(" Check log file for exception message."));
                    log(new Exception(errormsg), Level.CONFIG);
                    successful = false;
                }
            }
        }

        // Calls cleanup methods on all classes implementing ISetup
        cleanup();

        // Saves and unloads users
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                User.unloadUser(p);
            } catch (IOException e) {
                String errormsg = "Failed to unload user " + p.getName() + ".";
                log(errormsg.concat(" Check log file for exception message."));
                log(new IOException(errormsg), Level.CONFIG);
                successful = false;
            }
        }


        // Sets up all classes again
        setup();


        // Calls on reload on all item types
        for (ItemTypeHolder<?> holder : profMan.getItemTypeHolders()) {
            for (ItemType<?> itemType : holder) {
                try {
                    itemType.onReload();
                } catch (Exception e) {
                    log("Failed to reload itemtype " + itemType.getName() + ". Check log file for exception message.");
                    Professions.logError(e);
                    successful = false;
                }
            }
        }

        // Finally loads users again
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                User.loadUser(p);
            } catch (IOException e) {
                String errormsg = "Failed to load user " + p.getName() + ".";
                log(errormsg.concat(" Check log file for exception message."));
                log(new IOException(errormsg), Level.CONFIG);
                successful = false;
            }
        }

        return successful;
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
     * Forces the backup of plugin.
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
        return file;
    }

    /**
     * Registers all setups
     */
    private void registerSetups() {

        // register main settings first
        try {
            registerSetup(Settings.getInstance());
        } catch (Exception e) {
            Professions.logError(e);
        }

        // then other settings
        for (AbstractSettings s : Settings.SETTINGS) {
            try {
                registerSetup(s);
            } catch (Exception e) {
                Professions.logError(e);
            }
        }

        // then messages
        try {
            registerSetup(Messages.getInstance());
        } catch (Exception e) {
            Professions.logError(e);
        }
        // then the rest
        registerSetup(ItemUtils.instance);
        registerSetup(Profession.ProfessionType.PRIMARY);

        registerCommandHandler(new CommandHandler());
        registerCommandHandler(new MiningCommandHandler());
        registerCommandHandler(new HerbalismCommandHandler());
        registerCommandHandler(new AlchemyCommandHandler());
        registerCommandHandler(new JewelcraftingCommandHandler());

        // and lastly professions
        registerSetup(ProfessionManager.getInstance());
    }

    private void registerCommandHandler(AbstractCommandHandler commandHandler) {
        AbstractCommandHandler.register(commandHandler);
        registerSetup(commandHandler);
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
                Professions.logError(e);
            }
        }

        saveResource(LANG_PATH.concat("patterns.properties"), true);
        saveResource(LANG_PATH.concat("placeholders.properties"), true);

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
        new LogTask().runTaskTimer(this, LOG_DELAY * 20L, LOG_DELAY * 20L);
    }


    /**
     * Overloaded method from {@link JavaPlugin#saveResource(String, boolean)}, removes unnecessary message and made only to save text resources in UTF-8 formatting.
     *
     * @param resourcePath the path of resource
     * @param replace      whether or not to replace if the file already exists
     * @param fileName     the file name
     */
    public void saveResource(String resourcePath, String fileName, boolean replace) throws IllegalArgumentException {
        if (resourcePath != null && !resourcePath.equals("")) {
            resourcePath = resourcePath.replace('\\', '/');
            Reader in = this.getTextResource(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + super.getFile());
            } else {
                File outFile = new File(this.getDataFolder(), fileName);
                int lastIndex = resourcePath.lastIndexOf(47);
                File outDir = new File(this.getDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }

                try {
                    if (!outFile.exists() || replace) {

                        // NOPES: UTF-16, ISO, UTF-16BE
                        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8);
                        char[] buf = new char[1024];

                        int len;

                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        out.close();
                        in.close();
                    }
                } catch (IOException var10) {
                    log("Could not save " + outFile.getName() + " to " + outFile);
                    Professions.logError(var10);
                }

            }
        } else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }


    /**
     * Overridden method from {@link JavaPlugin#saveResource(String, boolean)}, removes unnecessary message and made only to save text resources in UTF-8 formatting.
     *
     * @param resourcePath the path of file
     * @param replace      whether or not to replace if the file already exists
     */
    @Override
    public void saveResource(String resourcePath, boolean replace) throws IllegalArgumentException {
        saveResource(resourcePath, resourcePath, replace);
    }

    public BackupTask.Result backupFirst() {
        if (FIRST_BACKUP) {
            try {
                FIRST_BACKUP = false;
                return backup();
            } catch (Exception e) {
                Professions.logError(e);
                return BackupTask.Result.FAILURE;
            }
        }
        return null;
    }
}
