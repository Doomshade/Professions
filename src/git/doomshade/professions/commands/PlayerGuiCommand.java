package git.doomshade.professions.commands;

import git.doomshade.professions.Professions;
import git.doomshade.professions.gui.playerguis.PlayerProfessionsGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerGuiCommand extends AbstractCommand {

    public PlayerGuiCommand() {
        // TODO Auto-generated constructor stub
        setCommand("gui");
        setDescription("Otev�e GUI profes�");
        setRequiresOp(false);
        setRequiresPlayer(true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // TODO Auto-generated method stub
//		GUI.getGui((Player) sender, MainGui.class).openGui();
        Professions.getManager().openGui(PlayerProfessionsGUI.class, (Player) sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getID() {
        // TODO Auto-generated method stub
        return "playergui";
    }

}
