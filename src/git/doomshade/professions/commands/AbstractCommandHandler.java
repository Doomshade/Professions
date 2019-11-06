package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.IBackup;
import git.doomshade.professions.utils.ISetup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;

public abstract class AbstractCommandHandler implements CommandExecutor, TabCompleter, ISetup, IBackup {
    static final HashMap<Class<? extends AbstractCommandHandler>, AbstractCommandHandler> INSTANCES = new HashMap<>();
    private static final HashMap<Class<? extends AbstractCommandHandler>, List<AbstractCommand>> COMMANDS = new HashMap<>();
    protected final Professions plugin = Professions.getInstance();
    private final ArrayList<AbstractCommand> INSTANCE_COMMANDS = new ArrayList<>();
    private final File FOLDER = new File(plugin.getDataFolder(), "commands");
    private PluginCommand cmd = null;
    private File file;

    {
        if (!FOLDER.isDirectory()) {
            FOLDER.mkdirs();
        }
    }

    public static <T extends AbstractCommandHandler> T getInstance(Class<T> commandHandlerClass) {
        try {
            Constructor<T> constructor = commandHandlerClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            if (INSTANCES.containsKey(commandHandlerClass)) {
                return (T) INSTANCES.get(commandHandlerClass);
            } else {
                T t = constructor.newInstance();
                INSTANCES.put(commandHandlerClass, t);
                return t;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean isValid(CommandSender sender, AbstractCommand acmd) {
        return (!acmd.requiresOp() || sender.isOp()) && (!acmd.requiresPlayer() || sender instanceof Player);
    }

    protected abstract String getCommandName();

    public abstract void registerCommands();

    private void postRegisterCommands() {
        updateCommands();
        sortCommands();
    }

    private void sortCommands() {
        INSTANCE_COMMANDS.sort(Comparator.comparing(AbstractCommand::getCommand));
    }

    protected final void registerCommand(AbstractCommand cmd) {
        INSTANCE_COMMANDS.add(cmd);
    }

    /**
     * reloads commands
     */
    private void updateCommands() {
        FileConfiguration loader = YamlConfiguration.loadConfiguration(file);
        Set<String> keys = loader.getKeys(false);
        for (AbstractCommand cmd : INSTANCE_COMMANDS) {
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

    private void setupCommandFile() throws IOException {
        FileConfiguration loader = YamlConfiguration.loadConfiguration(file);
        Map<String, Object> defaults = new HashMap<>();
        for (AbstractCommand cmd : INSTANCE_COMMANDS) {
            defaults.put(cmd.getID(), cmd.serialize());
        }
        loader.options().copyDefaults(true);
        loader.addDefaults(defaults);
        loader.save(file);
    }

    private String infoMessage(AbstractCommand acmd) {
        StringBuilder args = new StringBuilder();
        final Map<Boolean, List<String>> args1 = acmd.getArgs();
        if (args1 != null) {
            if (args1.containsKey(true) && args1.get(true) != null)
                args1.get(true).forEach(x -> {
                    args.append(" <");
                    args.append(x);
                    args.append(">");
                });
            if (args1.containsKey(false) && args1.get(false) != null)
                args1.get(false).forEach(x -> {
                    args.append(" [");
                    args.append(x);
                    args.append("]");
                });
        }

        return ChatColor.DARK_AQUA + "/" + cmd.getName() + " " + acmd.getCommand() + args + ChatColor.GOLD
                + " - " + acmd.getDescription();
    }

    @SuppressWarnings("unchecked")
    final <T> T cast(Object obj) {
        return (T) obj;
    }

    /**
     * @see ISetup#setup()
     */
    @Override
    public final void setup() throws IOException {
        if (cmd == null) {

            String commandName = getCommandName();
            try {
                file = new File(FOLDER, String.format("%s.yml", commandName));
                if (!file.exists()) {
                    file.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            cmd = Bukkit.getPluginCommand(commandName);
            cmd.setExecutor(this);
        }
        INSTANCE_COMMANDS.clear();
        registerCommands();
        postRegisterCommands();
        setupCommandFile();
        sortCommands();
        COMMANDS.put(this.getClass(), INSTANCE_COMMANDS);
    }

    @Override
    public final List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        // TODO Auto-generated method stub
        List<String> tab = new ArrayList<>();
        if (args.length == 0) {
            INSTANCE_COMMANDS.forEach(x -> {
                if (isValid(sender, x)) {
                    tab.add(x.getCommand());
                }
            });
            return tab;
        }
        for (AbstractCommand acmd : INSTANCE_COMMANDS) {
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
    public final boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.STRIKETHROUGH + "-------" + ChatColor.DARK_AQUA + "[" + ChatColor.RED
                    + plugin.getName() + ChatColor.DARK_AQUA + "]" + ChatColor.STRIKETHROUGH + "-------");
            String message;

            for (AbstractCommand icmd : INSTANCE_COMMANDS) {
                if (!isValid(sender, icmd)) {
                    continue;
                }
                sender.sendMessage(infoMessage(icmd));

            }
            return true;
        }
        for (AbstractCommand acmd : INSTANCE_COMMANDS) {
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
    public final File[] getFiles() {
        // TODO Auto-generated method stub
        return new File[]{file};
    }
}
