package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.gui.playerguis.PlayerProfessionsGUI;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerGuiCommand extends AbstractCommand {

    public PlayerGuiCommand() {
        setCommand("gui");
        setDescription("Opens the professions GUI");
        setRequiresPlayer(true);
        addPermission(Permissions.DEFAULT_COMMAND_USAGE);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
//		GUI.getGui((Player) sender, MainGui.class).openGui();
        Professions.getGUIManager().openGui(PlayerProfessionsGUI.class, (Player) sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "playergui";
    }

}
