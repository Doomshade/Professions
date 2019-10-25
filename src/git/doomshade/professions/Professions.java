package git.doomshade.professions;

import git.doomshade.guiapi.GUIApi;
import git.doomshade.guiapi.GUIManager;
import git.doomshade.professions.commands.CommandHandler;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.EventManager;
import git.doomshade.professions.gui.playerguis.PlayerProfessionsGUI;
import git.doomshade.professions.gui.playerguis.ProfessionGUI;
import git.doomshade.professions.gui.playerguis.ProfessionTrainerGUI;
import git.doomshade.professions.gui.playerguis.TestThreeGui;
import git.doomshade.professions.listeners.PluginProfessionListener;
import git.doomshade.professions.listeners.ProfessionListener;
import git.doomshade.professions.listeners.UserListener;
import git.doomshade.professions.profession.types.IProfessionType;
import git.doomshade.professions.profession.types.ItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.profession.types.mining.commands.MiningCommandHandler;
import git.doomshade.professions.task.BackupTask;
import git.doomshade.professions.task.SaveTask;
import git.doomshade.professions.trait.ProfessionTrainerTrait;
import git.doomshade.professions.user.User;
import git.doomshade.professions.utils.Backup;
import git.doomshade.professions.utils.Setup;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class Professions extends JavaPlugin implements Setup {
    private static Professions instance;
    private static ProfessionManager profMan;
    private static EventManager eventMan;
    private static Settings settings;
    private static GUIManager guiManager;
    private static Economy econ;
    private static int LOOPS = 0;

    // 5 minutes
    private final int SAVE_DELAY = 5 * 60;

    // 1 hr
    private final int BACKUP_DELAY = 60 * 60;

    private final ArrayList<Setup> SETUPS = new ArrayList<>();
    private final ArrayList<Backup> BACKUPS = new ArrayList<>();
    private final File BACKUP_FOLDER = new File(getDataFolder(), "backup");
    private final File PLAYER_FOLDER = new File(getDataFolder(), "playerdata");
    private final File CONFIG_FILE = new File(getDataFolder(), "config.yml");
    private final File ITEM_FOLDER = new File(getDataFolder(), "itemtypes");

    private FileConfiguration configLoader;

    /**
     * @param clazz class extending ItemType class
     * @return
     * @see #registerItemTypeHolder(ItemTypeHolder)
     */
    @SuppressWarnings("unchecked")
    public static <T extends ItemType<?>> ItemType<T> getItemType(Class<T> clazz, int id) {
        for (ItemType<?> type : profMan.ITEMS.values()) {
            if (type.getClass().getSimpleName().equals(clazz.getSimpleName()) && type.getId() == id) {
                return (ItemType<T>) type;
            }
        }
        throw new RuntimeException(String.format("%s is not a registered item type holder! Consider registering it via Professions.registerItemTypeHolder()", clazz.getSimpleName()));
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static GUIManager getManager() {
        return guiManager;
    }

    private static String getPrefix(String pref) {
        return "[" + pref + "] ";
    }

    public static Professions getInstance() {
        // TODO Auto-generated method stub
        return instance;
    }

    private static void setInstance(Professions instance) {
        Professions.instance = instance;
    }

    public static void saveUsers() throws IOException {
        User.saveUsers();
    }

    public static ProfessionManager getProfessionManager() {
        return profMan;
    }

    public static Profession<?> fromName(String name) {
        return profMan.fromName(name);
    }

    public static Profession<?> fromName(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return null;
        }
        return fromName(item.getItemMeta().getDisplayName());
    }

    public static EventManager getEventManager() {
        return eventMan;
    }

    public static User getUser(Player hrac) {
        // TODO Auto-generated method stub
        return User.getUser(hrac);
    }

    public static User getUser(UUID hrac) {
        // TODO Auto-generated method stub
        return User.getUser(hrac);
    }

    @SuppressWarnings("unchecked")
    public static <A extends ItemType<?>> ItemTypeHolder<A> getItemTypeHolder(Class<A> clazz) {
        return profMan.getItemTypeHolder(clazz);
    }

    /**
     * Registers an item type holder. Calls
     * {@link ProfessionManager#registerItemTypeHolder(ItemTypeHolder)} method.
     *
     * @param itemTypeHolder
     * @see git.doomshade.professions.ProfessionManager#registerItemTypeHolder(ItemTypeHolder)
     */
    public static <T extends ItemTypeHolder<?>> void registerItemTypeHolder(T itemTypeHolder) throws IOException {
        profMan.registerItemTypeHolder(itemTypeHolder);
    }

    public static Profession<? extends IProfessionType> getProfession(Class<? extends IProfessionType> profType) {
        return profMan.getProfession(profType);
    }

    /**
     * Registers a profession type
     *
     * @param clazz ProfessionType class
     */
    public static void registerProfessionType(Class<? extends IProfessionType> clazz) {
        profMan.registerProfessionType(clazz);
    }

    /**
     * Registers a profession
     *
     * @param profession Profession to register
     */
    public static void registerProfession(Class<Profession<? extends IProfessionType>> profession) {
        profMan.registerProfession(profession);
    }


    public static void unloadUser(User user) throws IOException {
        user.save();
        user.unload();
    }

    public static void loadUser(Player player) {
        // TODO Auto-generated method stub
        User.loadUser(player);
    }

    @Override
    public void onEnable() {
        setInstance(this);
        hookGuiApi();
        hookCitizens();
        setupEconomy();
        profMan = ProfessionManager.getInstance();
        eventMan = EventManager.getInstance();

        settings = Settings.getInstance();
        settings.reload();

        // any class with setup() method contains a file
        setupFiles();

        scheduleTasks();

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new UserListener(), this);
        pm.registerEvents(new ProfessionListener(), this);
        pm.registerEvents(new PluginProfessionListener(), this);


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
    }

    private void hookCitizens() {
        if (Bukkit.getPluginManager().isPluginEnabled(Citizens.getPlugin(Citizens.class))) {
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ProfessionTrainerTrait.class));
            sendConsoleMessage("Registered " + ProfessionTrainerTrait.class.getSimpleName() + " trait.");
        }
    }

    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private String getPrefix() {
        return getPrefix(getName());
    }

    public void sendConsoleMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(getPrefix() + message);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        try {
            saveFiles();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void saveFiles() throws IOException {
        saveUsers();
    }

    public Settings getSettings() {
        return settings;
    }

    private void setupFiles() {
        if (!getDataFolder().isDirectory()) {
            getDataFolder().mkdir();
        }
        if (!BACKUP_FOLDER.isDirectory()) {
            BACKUP_FOLDER.mkdirs();
        }
        if (!PLAYER_FOLDER.isDirectory()) {
            PLAYER_FOLDER.mkdirs();
        }
        if (!ITEM_FOLDER.isDirectory()) {
            ITEM_FOLDER.mkdirs();
        }
        saveDefaultConfig();
        reloadConfig();
        registerSetups();
        setup();
        registerBackups();

    }

    @Override
    public void reloadConfig() {
        // TODO Auto-generated method stub
        configLoader = YamlConfiguration.loadConfiguration(CONFIG_FILE);
    }

    @Override
    public FileConfiguration getConfig() {
        return configLoader;
    }

    public File getBackupFolder() {
        if (!BACKUP_FOLDER.isDirectory()) {
            BACKUP_FOLDER.mkdirs();
        }
        return BACKUP_FOLDER;
    }

    public File getPlayerFolder() {
        if (!PLAYER_FOLDER.isDirectory()) {
            PLAYER_FOLDER.mkdirs();
        }
        return PLAYER_FOLDER;
    }

    public File getItemsFolder() {
        if (!ITEM_FOLDER.isDirectory()) {
            ITEM_FOLDER.mkdirs();
        }
        return ITEM_FOLDER;
    }

    private void registerSetups() {
        for (Settings s : Settings.SETTINGS) {
            registerSetup(s);
        }
        registerSetup(Messages.getInstance());
        registerSetup(CommandHandler.getInstance(CommandHandler.class));
        registerSetup(CommandHandler.getInstance(MiningCommandHandler.class));
        registerSetup(ProfessionManager.getInstance());
    }

    @Override
    public void setup() {
        for (Setup setup : SETUPS) {
            try {
                sendConsoleMessage("Setting up " + setup.getClass().getSimpleName());
                setup.setup();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        sendConsoleMessage("Done!");
    }

    public void cleanup() {
        profMan.PROFESSION_TYPES.clear();
        profMan.PROFESSIONS_ID.clear();
        profMan.PROFESSIONS_NAME.clear();
        profMan.ITEMS.clear();
    }

    private void registerSetup(Setup setup) {
        if (!SETUPS.contains(setup))
            SETUPS.add(setup);
    }

    private void registerBackups() {
        registerBackup(CommandHandler.getInstance(CommandHandler.class));
        registerBackup(CommandHandler.getInstance(MiningCommandHandler.class));
        registerBackup(ProfessionManager.getInstance());
        registerBackup(User.getNoUser());
    }

    public BackupTask.Result backup() {
        BackupTask task = new BackupTask();
        task.run();
        return task.getResult();
    }

    private void registerBackup(Backup backup) {
        BACKUPS.add(backup);
    }

}
