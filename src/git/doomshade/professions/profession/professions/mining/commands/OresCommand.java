package git.doomshade.professions.profession.professions.mining.commands;

import git.doomshade.guiapi.GUI;
import git.doomshade.professions.Professions;
import git.doomshade.professions.gui.oregui.OreGUI;
import git.doomshade.professions.utils.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class OresCommand extends AbstractEditCommand {

    public OresCommand() {
        setCommand("ores");
        setDescription("Opens a GUI with available ores. Place the ore block to register the position of an ore.");
        setRequiresPlayer(true);
        addPermission(Permissions.BUILDER);
        setArg(false, "ignore range (true/false, default=false)");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        final Player player = (Player) sender;
        final Optional<? extends GUI> opt = Professions.getGUIManager().getGui(OreGUI.class, player);
        if (!opt.isPresent()) {
            return true;
        }

        GUI gui = opt.get();

        boolean ignore = false;

        if (args.length >= 2) {

            try {
                ignore = Boolean.parseBoolean(args[1]);
            } catch (Exception ignored) {
            }
        }
        gui.getContext().addContext("ignore", ignore);
        Professions.getGUIManager().openGui(gui);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "place";
    }
}
