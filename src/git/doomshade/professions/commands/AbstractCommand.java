/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jakub Šmrha
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package git.doomshade.professions.commands;

import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.Utils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

/**
 * Class representing all the commands. This is not a {@link CommandExecutor}, the executor is the command handler
 * registering this command!
 *
 * @author Doomshade
 * @version 1.0
 * @see AbstractCommandHandler
 * @since 1.0
 */
@SuppressWarnings("ALL")
public abstract class AbstractCommand implements ConfigurationSerializable, Comparable<AbstractCommand> {

    // path names in commands.yml
    private static final String COMMAND = "command";
    private static final String DESCRIPTION = "description";
    private static final String REQUIRES_PLAYER = "requiresPlayer";
    private static final String ARG_TRUE = "arg-true";
    private static final String ARG_FALSE = "arg-false";
    private static final String MESSAGE = "message";
    private static final String REQUIRED_PERMISSIONS = "permissions";
    // end of path names
    private String command = "";
    private String description = "";
    private Collection<String> requiredPermissions = new ArrayList<>();
    private List<String> messages = new ArrayList<>();
    private Map<Boolean, List<String>> args = new HashMap<>();
    private boolean requiresPlayer = false;

    /**
     * Partly deserializes a command (overrides all but {@code getId()} getter methods)
     *
     * @param map the map
     *
     * @return partly deserialized command
     */
    public static AbstractCommand partlyDeserialize(Map<String, Object> map) {
        return new AbstractCommand() {

            @Override
            public void onCommand(CommandSender sender, String[] args) {

            }

            @Override
            public List<String> onTabComplete(CommandSender sender, String[] args) {
                return null;
            }

            @Override
            public Map<Boolean, List<String>> getArgs() {
                Map<Boolean, List<String>> args = new HashMap<>();
                final CommandHandler instance = AbstractCommandHandler.getInstance(CommandHandler.class);
                if (instance != null) {
                    List<String> argTrue = new ArrayList<>();
                    try {
                        argTrue = Utils.cast(map.get(ARG_TRUE));
                    } catch (ClassCastException e) {
                        logDeserializationError("true arguments");
                        ProfessionLogger.logError(e);
                    }
                    args.put(true, argTrue);

                    List<String> argFalse = new ArrayList<>();
                    try {
                        argFalse = Utils.cast(map.get(ARG_FALSE));
                    } catch (ClassCastException e) {
                        logDeserializationError("false arguments");
                    }

                    args.put(false, argFalse);
                }
                return args;
            }

            /**
             * This uses {@link #getCommand()} method, so be careful not to call it in that!
             * @param type the method the error occurred in
             */
            private void logDeserializationError(String type) {
                ProfessionLogger.log(getDeserializationError(type) + " of " + getCommand() + " command.", Level.SEVERE);
            }

            @Override
            public String getCommand() {
                String st = "";
                try {
                    st = Utils.cast(map.get(COMMAND));
                } catch (ClassCastException e) {
                    ProfessionLogger.log(getDeserializationError("command") + "(serialization = " + map + ")",
                            Level.SEVERE);
                }
                return st;
            }

            private String getDeserializationError(String type) {
                return "Failed to deserialize " + type;
            }

            @Override
            public String getDescription() {
                String st = "";
                try {
                    st = Utils.cast(map.get(DESCRIPTION));
                } catch (ClassCastException e) {
                    logDeserializationError("description");
                }
                return st;
            }

            @Override
            public boolean requiresPlayer() {
                boolean b = false;
                try {
                    b = Utils.cast(map.get(REQUIRES_PLAYER));
                } catch (ClassCastException e) {
                    logDeserializationError("requires player");
                }
                return b;
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
     * Compares commands to each other based on their command name
     *
     * @param o the other command to compare to
     *
     * @return a comparison of command names
     */
    @Override
    public final int compareTo(AbstractCommand o) {
        return getCommand().compareTo(o.getCommand());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(command, description, messages, args, requiresPlayer, requiredPermissions);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractCommand that = (AbstractCommand) o;
        return requiresPlayer == that.requiresPlayer &&
                command.equals(that.command) &&
                description.equals(that.description) &&
                messages.equals(that.messages) &&
                args.equals(that.args) &&
                requiredPermissions.equals(that.requiredPermissions);
    }

    /**
     * @param sender the sender of this command
     * @param args   the arguments of the command
     */
    public abstract void onCommand(CommandSender sender, String[] args);

    public abstract List<String> onTabComplete(CommandSender sender, String[] args);

    /**
     * @return a unique ID for the command
     */
    public abstract String getID();

    /**
     * Sets an argument to the command
     *
     * @param bool use {@code true} if the argument is required, {@code false} otherwise
     * @param args the arguments
     */
    public final void setArg(boolean bool, String... args) {
        this.args.put(bool, Arrays.asList(args));
    }

    public final void addMessages(String... messages) {
        this.messages.addAll(Arrays.asList(messages));
    }

    @Override
    public final @NotNull Map<String, Object> serialize() {
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
     * @return custom messages of the command
     */
    public List<String> getMessages() {
        return messages;
    }

    /**
     * Sets custom messages of the command
     *
     * @param messages the messages
     */
    public final void setMessages(List<String> messages) {
        this.messages = messages;
    }

    /**
     * @return required permissions
     */
    public Collection<String> getPermissions() {
        return requiredPermissions;
    }

    /**
     * Sets the required permissions
     *
     * @param permissions the permissions
     */
    public final void setPermissions(Collection<String> permissions) {
        this.requiredPermissions = permissions;
    }

    /**
     * Adds a required permissions for this command usage
     *
     * @param permissions the permissions
     *
     * @see git.doomshade.professions.utils.Permissions
     */
    public final void addPermission(String... permissions) {
        requiredPermissions.addAll(Arrays.asList(permissions));
    }

    final void setupFrom(AbstractCommand other) {
        setCommand(other.getCommand());
        setArgs(other.getArgs());
        setDescription(other.getDescription());
        setRequiresPlayer(other.requiresPlayer());
        setMessages(other.getMessages());
        setPermissions(other.getPermissions());
    }

    /**
     * @param requiresPlayer the requiresPlayer to set
     */
    public final void setRequiresPlayer(boolean requiresPlayer) {
        this.requiresPlayer = requiresPlayer;
    }
}
