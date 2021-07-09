package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.gui.playerguis.PlayerProfessionsGUI;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Opens profession GUI with help of GUIApi framework
 *
 * @author Doomshade
 * @version 1.0
 * @see git.doomshade.guiapi.GUI
 * @see git.doomshade.guiapi.GUIApi
 */
public class PlayerGuiCommand extends AbstractCommand {

    public PlayerGuiCommand() {
        setCommand("gui");
        setDescription("Opens the professions GUI");
        setRequiresPlayer(true);
        addPermission(Permissions.DEFAULT_COMMAND_USAGE);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
//		GUI.getGui((Player) sender, MainGui.class).openGui();
        Professions.getGUIManager().openGui(PlayerProfessionsGUI.class, (Player) sender);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "playergui";
    }

}
