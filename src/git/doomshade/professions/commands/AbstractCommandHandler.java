package git.doomshade.professions.commands;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.ISetup;
import git.doomshade.professions.utils.Permissions;
import git.doomshade.professions.utils.SortedList;
import git.doomshade.professions.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * A command handler and executor/tab completer
 *
 * @author Doomshade
 * @version 1.0
 */
public abstract class AbstractCommandHandler implements CommandExecutor, TabCompleter, ISetup {
    private static final HashMap<Class<? extends AbstractCommandHandler>, AbstractCommandHandler> INSTANCES = new HashMap<>();
    protected final Professions plugin = Professions.getInstance();
    protected final SortedList<AbstractCommand> INSTANCE_COMMANDS = new SortedList<>(Comparator.comparing(AbstractCommand::getCommand));
    private final File FOLDER = new File(plugin.getDataFolder(), "commands");
    private PluginCommand cmd = null;
    private File file;

    public static ImmutableCollection<AbstractCommandHandler> getInstances() {
        return ImmutableSet.copyOf(INSTANCES.values());
    }

    public <T extends AbstractCommand> T getCommand(Class<T> clazz) throws Utils.SearchNotFoundException {
        return (T) Utils.findInIterable(getCommands(), x -> x.getClass().equals(clazz));
    }

    {
        if (!FOLDER.isDirectory()) {
            FOLDER.mkdirs();
        }
    }

    private static boolean isValid(CommandSender sender, AbstractCommand acmd) {

        if (acmd.requiresPlayer() && !(sender instanceof Player)) {
            return false;
        }

        if (sender.isOp()) {
            return true;
        }

        final Player player = (Player) sender;
        for (String perm : acmd.getPermissions()) {
            if (!Permissions.has(player, perm)) {
                return false;
            }
        }
        return true;
    }

    public static void register(AbstractCommandHandler commandHandler) {
        INSTANCES.putIfAbsent(commandHandler.getClass(), commandHandler);
    }

    public static <T extends AbstractCommandHandler> T getInstance(Class<T> commandHandlerClass) {
        try {
            if (INSTANCES.containsKey(commandHandlerClass)) {
                return (T) INSTANCES.get(commandHandlerClass);
            } else {
                throw new RuntimeException(commandHandlerClass.getSimpleName() + " is not a registered command handler!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final ImmutableSortedSet<AbstractCommand> getCommands() {
        return ImmutableSortedSet.copyOf(INSTANCE_COMMANDS);
    }

    protected abstract String getCommandName();

    public abstract void registerCommands();

    private void postRegisterCommands() {
        updateCommands();
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
                cmd.setRequiresPlayer(fileCommand.requiresPlayer());
                cmd.setMessages(fileCommand.getMessages());
                cmd.setPermissions(fileCommand.getPermissions());
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

    public String infoMessage(AbstractCommand acmd) {
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
    }

    @Override
    public final List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
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
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.STRIKETHROUGH + "-------" + ChatColor.DARK_AQUA + "[" + ChatColor.RED
                    + plugin.getName() + ChatColor.DARK_AQUA + "]" + ChatColor.STRIKETHROUGH + "-------");

            //int amnt = 0;

            // TODO add pages
            for (AbstractCommand acmd : INSTANCE_COMMANDS) {
                if (isValid(sender, acmd)) {
                    sender.sendMessage(infoMessage(acmd));
                }
                /*amnt++;
                if (amnt % 6 == 0) {
                    int page = amnt / 6;
                    break;
                }
                 */
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
                final boolean result = acmd.onCommand(sender, cmd, label, args);
                if ((acmd.getPermissions().contains(Permissions.HELPER) || acmd.getPermissions().contains(Permissions.BUILDER)) && sender instanceof Player) {
                    Professions.log(String.format("%s issued %s command with arguments: %s and result %s", sender.getName(), acmd.getCommand(), Arrays.toString(args), result), Level.CONFIG);
                }
                return result;
            }
        }
        return false;
    }

    public static String infoMessage(Class<? extends AbstractCommandHandler> ch, Class<? extends AbstractCommand> ac) {
        try {
            final AbstractCommandHandler ach = getInstance(ch);
            if (ach != null) {
                return ach.infoMessage(ach.getCommand(ac));
            }
        } catch (Utils.SearchNotFoundException ignored) {
        }
        return "";
    }
}
