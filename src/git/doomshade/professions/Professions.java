package git.doomshade.professions;


import git.doomshade.guiapi.GUIApi;
import git.doomshade.guiapi.GUIManager;
import git.doomshade.professions.api.IProfessionAPI;
import git.doomshade.professions.api.IProfessionManager;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.api.user.IUser;
import git.doomshade.professions.commands.AbstractCommandHandler;
import git.doomshade.professions.commands.CommandHandler;
import git.doomshade.professions.data.AbstractSettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.dynmap.MarkerManager;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.EventManager;
import git.doomshade.professions.gui.adminguis.AdminProfessionGUI;
import git.doomshade.professions.gui.adminguis.AdminProfessionsGUI;
import git.doomshade.professions.gui.oregui.OreGUI;
import git.doomshade.professions.gui.playerguis.PlayerProfessionsGUI;
import git.doomshade.professions.gui.playerguis.ProfessionGUI;
import git.doomshade.professions.gui.playerguis.TestThreeGui;
import git.doomshade.professions.gui.trainergui.TrainerChooserGUI;
import git.doomshade.professions.gui.trainergui.TrainerGUI;
import git.doomshade.professions.io.IOManager;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.listeners.*;
import git.doomshade.professions.placeholder.PlaceholderManager;
import git.doomshade.professions.profession.ProfessionManager;
import git.doomshade.professions.profession.professions.alchemy.commands.AlchemyCommandHandler;
import git.doomshade.professions.profession.professions.herbalism.commands.HerbalismCommandHandler;
import git.doomshade.professions.profession.professions.jewelcrafting.commands.JewelcraftingCommandHandler;
import git.doomshade.professions.profession.professions.mining.commands.MiningCommandHandler;
import git.doomshade.professions.profession.professions.mining.spawn.OreEditListener;
import git.doomshade.professions.task.BackupTask;
import git.doomshade.professions.task.LogTask;
import git.doomshade.professions.task.SaveTask;
import git.doomshade.professions.trait.TrainerListener;
import git.doomshade.professions.trait.TrainerTrait;
import git.doomshade.professions.user.User;
import git.doomshade.professions.utils.ISetup;
import git.doomshade.professions.utils.ItemUtils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.bukkit.DynmapPlugin;
import org.jetbrains.annotations.NotNull;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * The Main API class as well as the {@link JavaPlugin} class.
 *
 * @author Doomshade
 * @version 1.0
 */
public final class Professions extends JavaPlugin implements ISetup, IProfessionAPI {

    private static Professions instance;
    private static boolean diabloLike = false;
    private static ProfessionManager profMan;
    private static EventManager eventMan;
    private static GUIManager guiManager;
    private static PermissionManager permMan;
    private static Economy econ;

    private static final ArrayList<ISetup> SETUPS = new ArrayList<>();
    private final File CONFIG_FILE = new File(getDataFolder(), "config.yml");

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

    /**
     * @return the {@link ProfessionManager} instance
     */
    public static ProfessionManager getProfMan() {
        return profMan;
    }

    /**
     * @return instance of {@link EventManager}
     */
    public static EventManager getEventManager() {
        return eventMan;
    }

    public static boolean isDiabloLikeHook() {
        return diabloLike;
    }


    /**
     * Overridden method from {@link JavaPlugin#saveResource(String, boolean)}, removes unnecessary message and made only to save text resources in UTF-8 formatting.
     *
     * @param resource the path of file
     * @param replace  whether or not to replace if the file already exists
     */
    public static void createResource(String resource, boolean replace) {
        instance.saveResource(resource, replace);
    }

    private static void setInstance(Professions instance) {
        Professions.instance = instance;
    }

    @Override
    public void onEnable() {
        setInstance(this);
        hookPlugins();

        profMan = ProfessionManager.getInstance();
        eventMan = EventManager.getInstance();

        try {
            IOManager.setupFiles();
            registerSetups();
            setup();
        } catch (Exception e) {
            ProfessionLogger.logError(e);
        }

        // Hook dynmap after setups as it uses config
        hookPlugin("dynmap", x -> {
            // sets the dynmap plugin to marker manager
            MarkerManager.getInstance(DynmapPlugin.plugin);
            return true;
        });
        scheduleTasks();

        registerListeners();

        for (ItemTypeHolder<?, ?> holder : profMan.getItemTypeHolders()) {
            for (ItemType<?> itemType : holder) {
                itemType.onLoad();
            }
        }
    }

    @Override
    public void onDisable() {
        for (ItemTypeHolder<?, ?> holder : profMan.getItemTypeHolders()) {
            for (ItemType<?> itemType : holder) {
                itemType.onDisable();
            }
        }
        cleanup();
        Bukkit.getScheduler().cancelTasks(this);
        try {
            IOManager.saveFiles();
        } catch (IOException e) {
            ProfessionLogger.logError(e);
        }
        IOManager.closeLogFile();
    }


    /**
     * Reloads the plugin
     */
    public boolean reload() {
        boolean successful = true;

        // call pre reload
        for (ItemTypeHolder<?, ?> holder : profMan.getItemTypeHolders()) {
            for (ItemType<?> itemType : holder) {
                try {
                    itemType.onPreReload();
                } catch (Exception e) {
                    logExError("Failed to reload itemtype " + itemType.getName() + ".", e);
                    successful = false;
                }
            }
        }

        // then cleanup
        cleanup();

        // then save/unload users
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                User.unloadUser(p);
            } catch (IOException e) {
                logExError("Failed to unload user " + p.getName() + ".", e);
                successful = false;
            }
        }

        // then setup
        setup();

        // call reload
        for (ItemTypeHolder<?, ?> holder : profMan.getItemTypeHolders()) {
            for (ItemType<?> itemType : holder) {
                try {
                    itemType.onReload();
                } catch (Exception e) {
                    logExError("Failed to reload itemtype " + itemType.getName(), e);
                    successful = false;
                }
            }
        }

        // finally load users
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                User.loadUser(p);
            } catch (IOException e) {
                logExError("Failed to load user " + p.getName() + ".", e);
                successful = false;
            }
        }

        return successful;
    }

    /**
     * Registers a setup class
     *
     * @param setup the setup to register
     */
    public void registerSetup(ISetup setup) {
        if (!SETUPS.contains(setup))
            SETUPS.add(setup);
    }

    /**
     * Overloaded method from {@link JavaPlugin#saveResource(String, boolean)}, removes unnecessary message and made only to save text resources in UTF-8 formatting.
     *
     * @param resourcePath the path of resource
     * @param replace      whether or not to replace if the file already exists
     * @param fileName     the file name
     */
    public void saveResource(String resourcePath, String fileName, boolean replace) throws IllegalArgumentException {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        Reader in = this.getTextResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + super.getFile());
        }

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
        } catch (IOException e) {
            ProfessionLogger.log("Could not save " + outFile.getName() + " to " + outFile);
            ProfessionLogger.logError(e);
        }

    }

    @Override
    public void setup() {
        for (ISetup setup : SETUPS) {
            try {
                setup.setup();
            } catch (Exception e) {
                ProfessionLogger.log("Could not load " + setup.getSetupName(), Level.SEVERE);
                ProfessionLogger.logError(e);
            }
        }
    }

    @Override
    public void cleanup() {
        for (ISetup setup : SETUPS) {
            try {
                setup.cleanup();
            } catch (Exception e) {
                ProfessionLogger.log("Could not cleanup " + setup.getSetupName(), Level.SEVERE);
                ProfessionLogger.logError(e);
            }
        }
    }

    /**
     * Reloads the config
     */
    @Override
    public void reloadConfig() {
        configLoader = YamlConfiguration.loadConfiguration(CONFIG_FILE);
    }

    /**
     * Overridden method from {@link JavaPlugin#saveResource(String, boolean)}, removes unnecessary message and made only to save text resources in UTF-8 formatting.
     *
     * @param resourcePath the path of file
     * @param replace      whether or not to replace if the file already exists
     */
    @Override
    public void saveResource(@NotNull String resourcePath, boolean replace) throws IllegalArgumentException {
        saveResource(resourcePath, resourcePath, replace);
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        return configLoader;
    }

    /**
     * Logs an exception error
     *
     * @param errMsg the error message
     * @param e      the exception
     */
    private void logExError(String errMsg, Throwable e) {
        ProfessionLogger.log(errMsg.concat(". Check log file for exception message."));
        ProfessionLogger.logError(e);
    }

    /**
     * Registers all setups
     */
    private void registerSetups() {

        // register main settings first
        try {
            registerSetup(Settings.getInstance());
        } catch (Exception e) {
            ProfessionLogger.logError(e);
        }

        // then other settings
        for (AbstractSettings s : Settings.getSettings()) {
            try {
                registerSetup(s);
            } catch (Exception e) {
                ProfessionLogger.logError(e);
            }
        }

        // then messages
        try {
            registerSetup(Messages.getInstance());
        } catch (Exception e) {
            ProfessionLogger.logError(e);
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

    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new UserListener(), this);
        pm.registerEvents(new ProfessionListener(), this);
        pm.registerEvents(new PluginProfessionListener(), this);
        pm.registerEvents(new OreEditListener(), this);
        pm.registerEvents(new JewelcraftingListener(), this);
    }

    private void registerCommandHandler(AbstractCommandHandler commandHandler) {
        AbstractCommandHandler.register(commandHandler);
        registerSetup(commandHandler);
    }

    private void scheduleTasks() {
        new SaveTask().startTask();
        new BackupTask().startTask();
        new LogTask().startTask();
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
            guiManager.getGui(TrainerGUI.class, null).ifPresent(y -> registerSetup((ISetup) y));
            return true;
        });
        hookPlugin("Citizens", x -> {
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TrainerTrait.class));
            Bukkit.getPluginManager().registerEvents(new TrainerListener(), this);
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
        if (plug == null || !plug.isEnabled()) {
            return;
        }

        final boolean bool;
        try {
            bool = func.test(plug);
        } catch (Exception e) {
            ProfessionLogger.log(String.format("Could not hook with %s plugin!", plugin), Level.WARNING);
            ProfessionLogger.logError(e);
            return;
        }
        if (bool) {
            ProfessionLogger.log(String.format("Successfully hooked with %s plugin", plugin), Level.INFO);
        } else {
            ProfessionLogger.log(String.format("Could not hook with %s plugin", plugin), Level.INFO);
        }
    }

    @Override
    public IProfessionManager getProfessionManager() {
        return getProfMan();
    }

    @Override
    public IUser getUser(Player player) {
        return User.getUser(player);
    }
}
