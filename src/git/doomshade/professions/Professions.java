package git.doomshade.professions;

import git.doomshade.guiapi.GUIApi;
import git.doomshade.guiapi.GUIManager;
import git.doomshade.professions.commands.CommandHandler;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.EventManager;
import git.doomshade.professions.event.ProfessionEvent;
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
import git.doomshade.professions.profession.types.mining.IMining;
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
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    private final ArrayList<Setup> SETUPS = new ArrayList<>();
    private final ArrayList<Backup> BACKUPS = new ArrayList<>();
    private final File backupFolder = new File(getDataFolder(), "backup");
    private final File playerFolder = new File(getDataFolder(), "playerdata");
    private final File configFile = new File(getDataFolder(), "config.yml");
    private final File itemFolder = new File(getDataFolder(), "itemtypes");
    private FileConfiguration configLoader;

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
        return User.getUser(hrac);
    }

    public static User getUser(UUID hrac) {
        return User.getUser(hrac);
    }

    /**
     * @param clazz class extending ItemType class
     * @return the itemtype
     * @see #registerItemTypeHolder(Class)
     */
    @SuppressWarnings("unchecked")
    public static <T extends ItemType<?>> ItemType<T> getItemType(Class<T> clazz, int id) {
        for (ItemType<?> type : profMan.ITEMTYPES) {
            if (type.getClass().getSimpleName().equals(clazz.getSimpleName()) && type.getId() == id) {
                return (ItemType<T>) type;
            }
        }
        Bukkit.getLogger().log(Level.WARNING, getPrefix("Professions") + clazz.getSimpleName()
                        + " is not a registered item type holder! Consider registering it via Professions.registerItemTypeHolder(Class<? extends ItemTypeHolder<?>>)",
                clazz);
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <A extends ItemType<?>, T extends ItemTypeHolder<?>> T getItemTypeHolder(Class<T> clazz) {
        for (ItemTypeHolder<?> holder : profMan.ITEMTYPEHOLDERS) {
            if (holder.getClass().getSimpleName().equals(clazz.getSimpleName())) {
                LOOPS = 0;
                return (T) holder;
            }
        }

        LOOPS++;
        registerItemTypeHolder(clazz);
        Bukkit.getLogger().log(Level.WARNING, getPrefix("Professions") + clazz.getSimpleName()
                        + " is not a registered item type holder! Consider registering it via Professions.registerItemTypeHolder(Class<? extends ItemTypeHolder<?>>)",
                clazz);

        if (LOOPS >= 5) {
            Bukkit.getLogger().log(Level.SEVERE, getPrefix("Professions") + "Could not register "
                            + clazz.getSimpleName()
                            + " as a registered item type holder! Consider registering it via Professions.registerItemType(Class<? extends ItemTypeHolder<?>>)",
                    clazz);
            LOOPS = 0;
            return null;
        }
        return getItemTypeHolder(clazz);
    }

    public static Profession<? extends IProfessionType> getProfession(Class<? extends IProfessionType> profType) {
        return profMan.getProfession(profType);
    }

    /**
     * Registers an item type holder. Calls
     * {@link ProfessionManager#registerItemTypeHolder(Class)} method.
     *
     * @param clazz the itemtype holder class
     * @see git.doomshade.professions.ProfessionManager#registerItemTypeHolder(Class)
     */
    public static <ItTypeHolder extends ItemTypeHolder<?>> void registerItemTypeHolder(
            Class<ItTypeHolder> clazz) {
        profMan.registerItemTypeHolder(clazz);
    }

    /**
     * Registers a profession type
     *
     * @param clazz ProfessionType class
     */
    public static void registerProfessionType(Class<? extends IProfessionType> clazz) {
        profMan.registerInterface(clazz);
    }

    /**
     * Registers a profession
     *
     * @param profession Profession to register
     */
    public static void registerProfession(Profession<? extends IProfessionType> profession) {
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
        profMan = ProfessionManager.getInstance();
        eventMan = EventManager.getInstance();

        settings = Settings.getInstance();
        settings.reload();

        // any class with setup() method contains a file
        setupFiles();

        new BukkitRunnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                try {
                    if (Bukkit.getOnlinePlayers().isEmpty()) {
                        return;
                    }
                    saveFiles();

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }.runTaskTimer(this, SAVE_DELAY * 20L, SAVE_DELAY * 20L);

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new UserListener(), this);
        pm.registerEvents(new ProfessionListener(), this);
        pm.registerEvents(new PluginProfessionListener(), this);

        hookGuiApi();
        hookCitizens();
        setupEconomy();
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
        if (!backupFolder.isDirectory()) {
            backupFolder.mkdirs();
        }
        if (!playerFolder.isDirectory()) {
            playerFolder.mkdirs();
        }
        if (!itemFolder.isDirectory()) {
            itemFolder.mkdirs();
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
        configLoader = YamlConfiguration.loadConfiguration(configFile);
    }

    @Override
    public FileConfiguration getConfig() {
        return configLoader;
    }

    public File getPlayerFolder() {
        return playerFolder;
    }

    public File getItemsFolder() {
        return itemFolder;
    }

    private void registerSetups() {
        for (Settings s : Settings.SETTINGS) {
            registerSetup(s);
        }
        registerSetup(Messages.getInstance());
        registerSetup(CommandHandler.getInstance());
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
        profMan.ITEMTYPEHOLDERS.clear();
        profMan.ITEMTYPES.clear();
    }

    private void registerSetup(Setup setup) {
        if (!SETUPS.contains(setup))
            SETUPS.add(setup);
    }

    private void registerBackups() {
        registerBackup(CommandHandler.getInstance());
        registerBackup(ProfessionManager.getInstance());
        registerBackup(User.getNoUser());
    }

    public void backup() throws IOException {
        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(
                new File(backupFolder.getAbsolutePath(), "backup-" + new Date().getTime() + ".zip")))) {
            for (Backup backup : BACKUPS) {
                for (File file : backup.getFiles()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        sendConsoleMessage("Backing up " + file.getName());
                        zout.putNextEntry(new ZipEntry(file.getName()));
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = fis.read(buffer)) > 0) {
                            zout.write(buffer, 0, read);
                        }
                        zout.closeEntry();
                    }
                }
            }
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        }

    }

    private void registerBackup(Backup backup) {
        BACKUPS.add(backup);
    }

}
