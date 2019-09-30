package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.Backup;
import git.doomshade.professions.utils.Setup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Doomshade
 */
public class CommandHandler implements CommandExecutor, TabCompleter, Setup, Backup {
    private static final PluginCommand cmd = Bukkit.getPluginCommand("prof");
    private static Professions plugin;
    private static CommandHandler instance;
    private static List<AbstractCommand> COMMANDS;
    private static File file;

    static {
        if (instance == null) {
            instance = new CommandHandler();
        }
    }

    private CommandHandler() {

    }

    private static void createCommandFile() throws IOException {
        file = new File(plugin.getDataFolder(), "commands.yml");
        if (!file.exists()) {
            file.createNewFile();
        }

    }

    private static void registerCommands() {
        registerCommand(new ProfessCommand());
        registerCommand(new ReloadCommand());
        registerCommand(new ProfessionListCommand());
        registerCommand(new ProfessionInfoCommand());
        registerCommand(new UnprofessCommand());
        registerCommand(new BackupCommand());
        registerCommand(new SaveCommand());
        registerCommand(new AddExpCommand());
        registerCommand(new ExpMultiplierCommand());
        registerCommand(new PlayerGuiCommand());
        registerCommand(new BypassCommand());
        registerCommand(new NormalizeLevelsCommand());
        registerCommand(new AddExtraCommand());
        registerCommand(new LevelCommand());
        updateCommands();
        sortCommands();
    }

    private static void sortCommands() {
        COMMANDS.sort(Comparator.comparing(AbstractCommand::getCommand));
    }

    private static void registerCommand(AbstractCommand cmd) {
        COMMANDS.add(cmd);
    }

    /**
     * reloads commands
     */
    public static void updateCommands() {
        FileConfiguration loader = YamlConfiguration.loadConfiguration(file);
        Set<String> keys = loader.getKeys(false);
        for (AbstractCommand cmd : COMMANDS) {
            if (keys.contains(cmd.getID())) {
                AbstractCommand fileCommand = AbstractCommand
                        .partlyDeserialize(loader.getConfigurationSection(cmd.getID()).getValues(false));
                cmd.setCommand(fileCommand.getCommand());
                cmd.setArgs(fileCommand.getArgs());
                cmd.setDescription(fileCommand.getDescription());
                cmd.setRequiresOp(fileCommand.requiresOp());
                cmd.setRequiresPlayer(fileCommand.requiresPlayer());
                cmd.setMessages(fileCommand.getMessages());
            }
        }
    }

    private static void setupCommandFile() throws IOException {
        FileConfiguration loader = YamlConfiguration.loadConfiguration(file);
        Map<String, Object> defaults = new HashMap<>();
        for (AbstractCommand cmd : COMMANDS) {
            defaults.put(cmd.getID(), cmd.serialize());
        }
        loader.options().copyDefaults(true);
        loader.addDefaults(defaults);
        loader.save(file);
    }

    private static boolean isValid(CommandSender sender, AbstractCommand acmd) {
        return (!acmd.requiresOp() || sender.isOp()) && (!acmd.requiresPlayer() || sender instanceof Player);
    }

    static String infoMessage(AbstractCommand acmd) {
        StringBuilder argTrue = new StringBuilder();
        StringBuilder argFalse = new StringBuilder();
        if (acmd.getArgs() != null) {
            if (acmd.getArgs().containsKey(true) && acmd.getArgs().get(true) != null)
                acmd.getArgs().get(true).forEach(x -> {
                    argTrue.append(" <");
                    argTrue.append(x);
                    argTrue.append(">");
                });
            if (acmd.getArgs().containsKey(false) && acmd.getArgs().get(false) != null)
                acmd.getArgs().get(false).forEach(x -> {
                    argTrue.append(" [");
                    argTrue.append(x);
                    argTrue.append("]");
                });
        }

        return ChatColor.DARK_AQUA + "/" + cmd.getName() + " " + acmd.getCommand() + argTrue + argFalse + ChatColor.GOLD
                + " - " + acmd.getDescription();
    }

    @SuppressWarnings("unchecked")
    static <T> T cast(Object obj) {
        return (T) obj;
    }

    /**
     * @return instance of this class
     */
    public static CommandHandler getInstance() {
        return instance;
    }

    /**
     * @see git.doomshade.professions.utils.Setup#setup()
     */
    @Override
    public void setup() throws IOException {
        plugin = Professions.getInstance();
        cmd.setExecutor(instance);
        COMMANDS = new ArrayList<>();
        createCommandFile();
        registerCommands();
        setupCommandFile();
        COMMANDS.sort(Comparator.comparing(AbstractCommand::getCommand));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        // TODO Auto-generated method stub
        List<String> tab = new ArrayList<>();
        if (args.length == 0) {
            COMMANDS.forEach(x -> {
                if (isValid(sender, x)) {
                    tab.add(x.getCommand());
                }
            });
            return tab;
        }
        for (AbstractCommand acmd : COMMANDS) {
            if (!isValid(sender, acmd)) {
                continue;
            }
            if (acmd.getCommand().equalsIgnoreCase(args[0])) {
                return acmd.onTabComplete(sender, cmd, label, args);
            }
            if (acmd.getCommand().startsWith(args[0])) {

                tab.add(acmd.getCommand());
            }
        }

        return tab.isEmpty() ? null : tab;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.STRIKETHROUGH + "-------" + ChatColor.DARK_AQUA + "[" + ChatColor.RED
                    + plugin.getName() + ChatColor.DARK_AQUA + "]" + ChatColor.STRIKETHROUGH + "-------");
            String message = null;

            for (AbstractCommand icmd : COMMANDS) {
                if (!isValid(sender, icmd)) {
                    continue;
                }
                message = infoMessage(icmd);
                if (message != null) {
                    sender.sendMessage(message);
                }
            }
            return true;
        }
        for (AbstractCommand acmd : COMMANDS) {
            if (acmd.getCommand().equalsIgnoreCase(args[0])) {
                if (!isValid(sender, acmd)) {
                    return false;
                }
                List<String> cmdArgs = acmd.getArgs().containsKey(true) && acmd.getArgs().get(true) != null
                        ? acmd.getArgs().get(true)
                        : null;
                if (cmdArgs != null && acmd.getArgs().get(true) != null && cmdArgs.size() > args.length - 1) {
                    sender.sendMessage(infoMessage(acmd));
                    return true;
                }
                return acmd.onCommand(sender, cmd, label, args);
            }
        }
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public File[] getFiles() {
        // TODO Auto-generated method stub
        return new File[]{file};
    }

}
