package git.doomshade.professions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Doomshade
 */
abstract class AbstractCommand implements ConfigurationSerializable {

    protected String command = "";
    protected String description = "";
    protected List<String> messages = new ArrayList<>();
    protected Map<Boolean, List<String>> args = new HashMap<>();
    protected boolean requiresPlayer = false, requiresOp = false;

    /**
     * @param map
     * @return partly deserialized command
     */
    public static AbstractCommand partlyDeserialize(Map<String, Object> map) {
        return new AbstractCommand() {

            @Override
            public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Map<Boolean, List<String>> getArgs() {
                // TODO Auto-generated method stub
                Map<Boolean, List<String>> args = new HashMap<>();
                args.put(true, CommandHandler.cast(map.get("arg-true")));
                args.put(false, CommandHandler.cast(map.get("arg-false")));
                return args;
            }

            @Override
            public String getCommand() {
                // TODO Auto-generated method stub
                return map.get("command").toString();
            }

            @Override
            public String getDescription() {
                // TODO Auto-generated method stub
                return map.get("description").toString();
            }

            @Override
            public boolean requiresPlayer() {
                // TODO Auto-generated method stub
                return (boolean) map.get("requiresPlayer");
            }

            @Override
            public boolean requiresOp() {
                // TODO Auto-generated method stub
                return (boolean) map.get("requiresOp");
            }

            @Override
            public String getID() {
                // TODO Auto-generated method stub
                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<String> getMessages() {
                // TODO Auto-generated method stub
                return (List<String>) map.get("message");
            }

        };
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
    public void setArgs(Map<Boolean, List<String>> args) {
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
    public void setCommand(String command) {
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
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return should return true if the command requires player
     */
    public boolean requiresPlayer() {
        return requiresPlayer;
    }

    /**
     * @return should return true if the command requires op
     */
    public boolean requiresOp() {
        return requiresOp;
    }

    /**
     * @return a unique ID for the command
     */
    public abstract String getID();

    /**
     * @param requiresPlayer the requiresPlayer to set
     */
    public void setRequiresPlayer(boolean requiresPlayer) {
        this.requiresPlayer = requiresPlayer;
    }

    /**
     * @param requiresOp the requiresOp to set
     */
    public void setRequiresOp(boolean requiresOp) {
        this.requiresOp = requiresOp;
    }

    public void setArg(boolean bool, List<String> args) {
        this.args.put(bool, args);
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    @Override
    public Map<String, Object> serialize() {
        // TODO Auto-generated method stub
        Map<String, Object> map = new HashMap<>();
        map.put("command", getCommand());
        map.put("description", getDescription());
        map.put("requiresPlayer", requiresPlayer());
        map.put("requiresOp", requiresOp());
        map.put("arg-true", getArgs().get(true));
        map.put("arg-false", getArgs().get(false));
        map.put("message", getMessages());
        return map;

    }

}
