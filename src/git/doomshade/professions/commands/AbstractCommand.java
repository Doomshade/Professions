package git.doomshade.professions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Class representing all the commands
 *
 * @author Doomshade
 */
public abstract class AbstractCommand implements ConfigurationSerializable, Comparable<AbstractCommand> {

    private static final String COMMAND = "command";
    private static final String DESCRIPTION = "description";
    private static final String REQUIRES_PLAYER = "requiresPlayer";
    private static final String ARG_TRUE = "arg-true";
    private static final String ARG_FALSE = "arg-false";
    private static final String MESSAGE = "message";
    private static final String REQUIRED_PERMISSIONS = "permissions";
    protected String command = "";
    protected String description = "";
    protected Collection<String> requiredPermissions = new ArrayList<>();
    protected List<String> messages = new ArrayList<>();
    protected Map<Boolean, List<String>> args = new HashMap<>();
    protected boolean requiresPlayer = false;

    /**
     * Partly deserializes a command (overrides all but {@code getId()} getter methods)
     *
     * @param map the map
     * @return partly deserialized command
     */
    public static AbstractCommand partlyDeserialize(Map<String, Object> map) {
        return new AbstractCommand() {

            @Override
            public int compareTo(@NotNull AbstractCommand o) {
                return getCommand().compareTo(o.getCommand());
            }

            @Override
            public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
                return false;
            }

            @Override
            public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
                return null;
            }

            @Override
            public Map<Boolean, List<String>> getArgs() {
                Map<Boolean, List<String>> args = new HashMap<>();
                final CommandHandler instance = AbstractCommandHandler.getInstance(CommandHandler.class);
                if (instance != null) {
                    args.put(true, instance.cast(map.get(ARG_TRUE)));
                    args.put(false, instance.cast(map.get(ARG_FALSE)));
                }
                return args;
            }

            @Override
            public String getCommand() {
                return map.get(COMMAND).toString();
            }

            @Override
            public String getDescription() {
                return map.get(DESCRIPTION).toString();
            }

            @Override
            public boolean requiresPlayer() {
                return (boolean) map.get(REQUIRES_PLAYER);
            }

            @Override
            public String getID() {
                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<String> getMessages() {
                return (List<String>) map.get(MESSAGE);
            }

            @SuppressWarnings("unchecked")
            @Override
            public Collection<String> getPermissions() {
                return (List<String>) map.get(REQUIRED_PERMISSIONS);
            }
        };
    }

    /**
     * Compares commands to each other based on their command name
     *
     * @param o the other command to compare to
     * @return
     */
    @Override
    public int compareTo(@NotNull AbstractCommand o) {
        return getCommand().compareTo(o.getCommand());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractCommand that = (AbstractCommand) o;
        return requiresPlayer == that.requiresPlayer &&
                command.equals(that.command) &&
                description.equals(that.description) &&
                messages.equals(that.messages) &&
                args.equals(that.args) &&
                requiredPermissions.equals(that.requiredPermissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, description, messages, args, requiresPlayer, requiredPermissions);
    }

    /**
     * @param sender
     * @param cmd
     * @param label
     * @param args
     * @return
     */
    public abstract boolean onCommand(CommandSender sender, Command cmd, String label, String[] args);

    /**
     * @param sender
     * @param cmd
     * @param label
     * @param args
     * @return
     */
    public abstract List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args);

    /**
     * @return the arguments of command
     */
    public Map<Boolean, List<String>> getArgs() {
        return args;
    }

    /**
     * @param args the args to set
     */
    public final void setArgs(Map<Boolean, List<String>> args) {
        this.args = args;
    }

    /**
     * @return the name of command
     */
    public String getCommand() {
        return command;
    }

    /**
     * @param command the command to set
     */
    public final void setCommand(String command) {
        this.command = command;
    }

    /**
     * @return the description of command
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public final void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return should return true if the command requires player
     */
    public boolean requiresPlayer() {
        return requiresPlayer;
    }

    /**
     * @return a unique ID for the command
     */
    public abstract String getID();

    /**
     * @param requiresPlayer the requiresPlayer to set
     */
    public final void setRequiresPlayer(boolean requiresPlayer) {
        this.requiresPlayer = requiresPlayer;
    }

    public final void setArg(boolean bool, List<String> args) {
        this.args.put(bool, args);
    }

    public List<String> getMessages() {
        return messages;
    }

    public final void setMessages(List<String> messages) {
        this.messages = messages;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put(COMMAND, getCommand());
        map.put(DESCRIPTION, getDescription());
        map.put(REQUIRES_PLAYER, requiresPlayer());
        map.put(ARG_TRUE, getArgs().get(true));
        map.put(ARG_FALSE, getArgs().get(false));
        map.put(MESSAGE, getMessages());
        map.put(REQUIRED_PERMISSIONS, getPermissions());
        return map;
    }

    public final void addPermission(String... permissions) {
        requiredPermissions.addAll(Arrays.asList(permissions));
    }

    public Collection<String> getPermissions() {
        return requiredPermissions;
    }

    public final void setPermissions(Collection<String> permissions) {
        this.requiredPermissions = permissions;
    }
}
