package git.doomshade.professions.commands;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import git.doomshade.professions.Professions;
import git.doomshade.professions.utils.ISetup;
import git.doomshade.professions.utils.Permissions;
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
 * Command is referred to<br><b>/[cmd_executor_name] [command_name] [args]</b><br>
 * Command handler has the same meaning as command executor in the java doc comments
 *
 * @author Doomshade
 * @version 1.0
 */
public abstract class AbstractCommandHandler implements CommandExecutor, TabCompleter, ISetup {

    // cache instances of all command handlers
    private static final HashMap<Class<? extends AbstractCommandHandler>, AbstractCommandHandler> INSTANCES = new HashMap<>();

    // only need one reference of plugin
    // honestly not needed, but it does not eat that much memory
    protected final Professions plugin = Professions.getInstance();

    // tree map to print the commands in natural order
    // again, memory/CPU usage is little no none, no need to keep it as a simple hashmap
    protected final TreeMap<String, AbstractCommand> INSTANCE_COMMANDS = new TreeMap<>(String::compareToIgnoreCase);

    // TODO
    // will likely shift to an IOManager someday
    private final File FOLDER = new File(plugin.getDataFolder(), "commands");

    // initialize plugin command and its file
    private PluginCommand cmd = null;
    private File file = null;

    /**
     * Keep in mind that this method CREATES a copy of the command handlers
     *
     * @return an immutable set of all command handlers
     */
    public static Collection<AbstractCommandHandler> getInstances() {
        return ImmutableSet.copyOf(INSTANCES.values());
    }


    // just make sure the folder is an actual directory every time a new instance is created
    {
        if (!FOLDER.isDirectory()) {
            FOLDER.mkdirs();
        }
    }

    /**
     * Validates the command execution
     *
     * @param sender the sender of command
     * @param acmd   the command to send
     * @return {@code true} if sender is either op or meets command requirements, {@code false} otherwise
     */
    private static boolean isValid(CommandSender sender, AbstractCommand acmd) {

        // invalid cmd
        if (acmd == null) return false;

        // cmd requires player
        if (acmd.requiresPlayer() && !(sender instanceof Player)) {
            return false;
        }

        // sender is op, don't give a shit about perm
        if (sender.isOp()) {
            return true;
        }

        // we can cast do player as the console command sender is OP by default
        // check perms
        // we can keep this loop as high permissions inherit the lower ones
        final Player player = (Player) sender;
        for (String perm : acmd.getPermissions()) {
            if (!Permissions.has(player, perm)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Registers a command handler
     *
     * @param commandHandler the command handler to register
     */
    public static void register(AbstractCommandHandler commandHandler) {
        INSTANCES.putIfAbsent(commandHandler.getClass(), commandHandler);
    }

    /**
     * <b>Key</b> is the command name <br>
     * <b>Value</b> is the command instance
     *
     * @return a copy of the command handler's commands
     */
    public final Map<String, AbstractCommand> getCommands() {
        return ImmutableSortedMap.copyOf(INSTANCE_COMMANDS);
    }

    /**
     * This performs in O(n) as we only cache commands' name and its class
     *
     * @param clazz the command class
     * @param <T>   the command
     * @return the command based on class given
     * @throws Utils.SearchNotFoundException if there is no such command registered
     */
    public <T extends AbstractCommand> T getCommand(Class<T> clazz) throws Utils.SearchNotFoundException {
        return (T) Utils.findInIterable(getCommands().values(), x -> x.getClass().equals(clazz));
    }

    /**
     * @param commandHandlerClass the command handler class
     * @param <T>                 the command handler
     * @return the command handler
     * @throws RuntimeException if the command handler is not registered
     */
    public static <T extends AbstractCommandHandler> T getInstance(Class<T> commandHandlerClass) throws RuntimeException {
        if (INSTANCES.containsKey(commandHandlerClass)) {
            return (T) INSTANCES.get(commandHandlerClass);
        } else {
            throw new RuntimeException(commandHandlerClass.getSimpleName() + " is not a registered command handler!");
        }
    }


    /**
     * @return the command executor (handler) name (/[command_executor_name] [command_name] [args])
     */
    protected abstract String getCommandExecutorName();

    /**
     * Registers the commands <br>
     * Please use {@link AbstractCommandHandler#registerCommand(AbstractCommand)} in this method to register <b>all</b> commands
     */
    public abstract void registerCommands();

    /**
     * Deprecated method, calls updateCommands
     */
    private void postRegisterCommands() {
        updateCommands();
    }

    /**
     * Registers the command instance
     *
     * @param cmd the command to register
     */
    protected final void registerCommand(AbstractCommand cmd) {
        INSTANCE_COMMANDS.put(cmd.getCommand(), cmd);
    }

    /**
     * Reloads commands from file
     */
    private void updateCommands() {
        // load the command handler file
        FileConfiguration loader = YamlConfiguration.loadConfiguration(file);
        Set<String> keys = loader.getKeys(false);

        // this is quite ugly, but essentially deserializes the command, creating a helper class
        // we can only partly deserialize from file as there's no way we serialize onCommand and onTabComplete methods
        // there could likely be a better looking solution, but fuck that
        for (AbstractCommand cmd : INSTANCE_COMMANDS.values()) {
            final String id = cmd.getID();

            // sometimes we forget the ID of command (which is mandatory), throw an ex to let the coder know
            if (id == null) {
                throw new NullPointerException("Null ID of " + cmd.getClass().getSimpleName() + " found!");
            }

            // there is a possibility that somebody made a mistake in file and overrode the ID of command, let them know!
            // this will VERY LIKELY not happen, but if I happen to call this method somewhere else but the setup method, then I need to know :)
            if (!keys.contains(id)) {
                Professions.log("Could not find command with " + id + " ID, please reload the plugin!", Level.WARNING);
                continue;
            }
            // lastly, partly deserialize the command
            AbstractCommand fileCommand = AbstractCommand
                    .partlyDeserialize(loader.getConfigurationSection(id).getValues(false));
            final String command = fileCommand.getCommand();
            if (command == null || command.isEmpty()) {
                Professions.log("Could not deserialize " + id + ".", Level.WARNING);
                continue;
            }
            // ugly but duh
            cmd.setCommand(command);
            cmd.setArgs(fileCommand.getArgs());
            cmd.setDescription(fileCommand.getDescription());
            cmd.setRequiresPlayer(fileCommand.requiresPlayer());
            cmd.setMessages(fileCommand.getMessages());
            cmd.setPermissions(fileCommand.getPermissions());

        }
    }

    /**
     * Sets up the command file, adding the command serialization as defaults.
     * This will add missing commands
     *
     * @throws IOException if there's an error saving to file
     */
    private void setupCommandFile() throws IOException {
        FileConfiguration loader = YamlConfiguration.loadConfiguration(file);
        Map<String, Object> defaults = new HashMap<>();
        for (AbstractCommand cmd : INSTANCE_COMMANDS.values()) {
            defaults.put(cmd.getID(), cmd.serialize());
        }
        loader.addDefaults(defaults);
        loader.options().copyDefaults(true);
        loader.save(file);
    }

    /**
     * This method is implemented here as we have only private access to the command executor name
     *
     * @param acmd the command
     * @return a string representation of command
     */
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

    /**
     * @see ISetup#setup()
     */
    @Override
    public final void setup() throws IOException {
        if (cmd == null || file == null) {
            String commandName = getCommandExecutorName();
            try {
                file = new File(FOLDER, String.format("%s.yml", commandName));
                if (!file.exists()) {
                    file.createNewFile();
                }
            } catch (IOException e) {
                Professions.logError(e);
            }
            cmd = Bukkit.getPluginCommand(commandName);
            cmd.setExecutor(this);
        }
        INSTANCE_COMMANDS.clear();
        registerCommands();

        setupCommandFile();
        postRegisterCommands();
    }

    @Override
    public final List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 0) {
            INSTANCE_COMMANDS.forEach((y, x) -> {
                if (isValid(sender, x)) {
                    tab.add(x.getCommand());
                }
            });
            return tab;
        }

        AbstractCommand acmd = INSTANCE_COMMANDS.get(args[0]);
        if (isValid(sender, acmd)) {
            return acmd.onTabComplete(sender, args);
        }

        for (AbstractCommand instanceCmd : INSTANCE_COMMANDS.values()) {
            if (instanceCmd.getCommand().startsWith(args[0])) {
                tab.add(instanceCmd.getCommand());
            }
        }

        return tab.isEmpty() ? null : tab;
    }

    private void printValidCommands(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.STRIKETHROUGH + "-------" + ChatColor.DARK_AQUA + "[" + ChatColor.RED
                + plugin.getName() + ChatColor.DARK_AQUA + "]" + ChatColor.STRIKETHROUGH + "-------");
        for (AbstractCommand acmd : INSTANCE_COMMANDS.values()) {
            if (isValid(sender, acmd)) {
                sender.sendMessage(infoMessage(acmd));
            }
            // TODO add pages
                /*amnt++;
                if (amnt % 6 == 0) {
                    int page = amnt / 6;
                    break;
                }
                 */
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            //int amnt = 0;
            printValidCommands(sender);
            return true;
        }

        AbstractCommand acmd = INSTANCE_COMMANDS.get(args[0]);
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
        acmd.onCommand(sender, args);
        if ((acmd.getPermissions().contains(Permissions.HELPER) || acmd.getPermissions().contains(Permissions.BUILDER)) && sender instanceof Player) {
            Professions.log(String.format("%s issued %s command with arguments: %s", sender.getName(), acmd.getCommand(), Arrays.toString(args)), Level.CONFIG);
        }
        return true;
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
