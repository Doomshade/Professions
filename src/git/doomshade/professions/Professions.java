package git.doomshade.professions;


import git.doomshade.guiapi.GUIApi;
import git.doomshade.guiapi.GUIManager;
import git.doomshade.professions.commands.CommandHandler;
import git.doomshade.professions.data.AbstractSettings;
import git.doomshade.professions.data.Settings;
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
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
    private static Economy econ;

    // 5 minutes
    private final int SAVE_DELAY = 5 * 60;

    // 1 hr
    private final int BACKUP_DELAY = 60 * 60;

    private static final ArrayList<ISetup> SETUPS = new ArrayList<>();
    private final File BACKUP_FOLDER = new File(getDataFolder(), "backup");
    private final File PLAYER_FOLDER = new File(getDataFolder(), "playerdata");
    private final File CONFIG_FILE = new File(getDataFolder(), "config.yml");
    private final File ITEM_FOLDER = new File(getDataFolder(), "itemtypes");
    private final File PROFESSION_FOLDER = new File(getDataFolder(), "professions");

    private FileConfiguration configLoader;


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
    public static GUIManager getManager() {
        return guiManager;
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
     * @param clazz the {@link ItemTypeHolder} class to look for
     * @param <A>   the {@link ItemTypeHolder}'s {@link ItemType}
     * @return instance of {@link ItemTypeHolder}
     * @see ProfessionManager#getItemTypeHolder(Class)
     */
    public static <A extends ItemType<?>> ItemTypeHolder<A> getItemTypeHolder(Class<A> clazz) {
        return profMan.getItemTypeHolder(clazz);
    }

    /**
     * @param itemTypeHolder the {@link ItemTypeHolder} to register
     * @see ProfessionManager#registerItemTypeHolder(ItemTypeHolder)
     */
    public static <T extends ItemTypeHolder<?>> void registerItemTypeHolder(T itemTypeHolder) throws IOException {
        profMan.registerItemTypeHolder(itemTypeHolder);
    }


    /**
     * @param profession the {@link Profession} class
     * @return instance of {@link Profession}
     * @see ProfessionManager#getProfession(Class)
     */
    public static Profession<? extends IProfessionType> getProfession(Class<? extends Profession<?>> profession) {
        return profMan.getProfession(profession);
    }

    /**
     * @param clazz ProfessionType class
     * @see ProfessionManager#registerProfessionType(Class)
     */
    public static void registerProfessionType(Class<? extends IProfessionType> clazz) {
        profMan.registerProfessionType(clazz);
    }

    /**
     * @param profession Profession to register
     * @see ProfessionManager#registerProfession(Class)
     */
    public static void registerProfession(Class<Profession<? extends IProfessionType>> profession) {
        profMan.registerProfession(profession);
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
     * Logs an error message to console
     *
     * @param message the message to log
     * @param level   the log level
     */
    public static void log(String message, Level level) {
        Ansi.Color color = Ansi.Color.WHITE;

        final List<Integer> RED = Arrays.asList(Level.WARNING.intValue(), Level.SEVERE.intValue());
        final List<Integer> GREEN = Arrays.asList(Level.FINE.intValue(), Level.CONFIG.intValue(), Level.FINER.intValue());

        if (RED.contains(level.intValue())) {
            color = Ansi.Color.RED;
        }


        if (GREEN.contains(level.intValue())) {
            color = Ansi.Color.GREEN;
        }

        Ansi ansi = Ansi.ansi().boldOff();


        getInstance().getLogger().log(level, ansi.fg(color).toString() + message + ansi.fg(Ansi.Color.WHITE));
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
        setupEconomy();

        profMan = ProfessionManager.getInstance();
        eventMan = EventManager.getInstance();

        // any class with setup() method contains a file
        try {
            setupFiles();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

        scheduleTasks();

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new UserListener(), this);
        pm.registerEvents(new ProfessionListener(), this);
        pm.registerEvents(new PluginProfessionListener(), this);
    }

    /**
     * Cancels the tasks and saves user files
     */
    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        try {
            saveFiles();
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
     * @return the backup folder directory
     */
    public File getBackupFolder() {
        if (!BACKUP_FOLDER.isDirectory()) {
            BACKUP_FOLDER.mkdirs();
        }
        return BACKUP_FOLDER;
    }

    /**
     * @return the {@link User} folder directory
     */
    public File getPlayerFolder() {
        if (!PLAYER_FOLDER.isDirectory()) {
            PLAYER_FOLDER.mkdirs();
        }
        return PLAYER_FOLDER;
    }

    /**
     * @return the {@link ItemType} folder directory
     */
    public File getItemsFolder() {
        if (!ITEM_FOLDER.isDirectory()) {
            ITEM_FOLDER.mkdirs();
        }
        return ITEM_FOLDER;
    }

    public File getProfessionFolder() {
        if (!PROFESSION_FOLDER.isDirectory()) {
            PROFESSION_FOLDER.mkdirs();
        }
        return PROFESSION_FOLDER;
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

    public void reload() throws IOException {
        cleanup();
        User.saveUsers();

        for (Player p : Bukkit.getOnlinePlayers()) {
            User.unloadUser(p);
        }
        setup();
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

    private void registerSetups() {
        registerSetup(Settings.getInstance());
        for (AbstractSettings s : Settings.SETTINGS) {
            registerSetup(s);
        }
        registerSetup(Messages.getInstance());
        registerSetup(CommandHandler.getInstance(CommandHandler.class));
        registerSetup(MiningCommandHandler.getInstance(MiningCommandHandler.class));
        registerSetup(ProfessionManager.getInstance());
    }

    private void setupFiles() throws ConfigurationException {
        if (!getDataFolder().isDirectory()) {
            getDataFolder().mkdir();
        }

        // getters create folders if the dir doesn't exist (this prevents random null/FileNotFound exceptions)
        getBackupFolder();
        getPlayerFolder();
        getItemsFolder();
        getProfessionFolder();

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
    }

    private void hookCitizens() {
        if (Bukkit.getPluginManager().getPlugin("Citizens") == null) {
            return;
        }
        log("Successfully hooked with Citizens plugin", Level.INFO);
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ProfessionTrainerTrait.class));
        log("Registered " + ProfessionTrainerTrait.class.getSimpleName() + " trait.", Level.INFO);

    }

    private void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }

        log("Successfully hooked with Vault plugin", Level.INFO);
        econ = rsp.getProvider();
    }

    private void hookSkillAPI() {
        PluginManager pm = Bukkit.getPluginManager();
        if (pm.getPlugin("SkillAPI") == null) {
            return;
        }
        pm.registerEvents(new SkillAPIListener(), this);
    }
}
