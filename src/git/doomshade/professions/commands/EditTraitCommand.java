package git.doomshade.professions.commands;

import git.doomshade.professions.trait.TrainerTrait;
import git.doomshade.professions.utils.Permissions;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Command for editing the trainer. Basically opens a trainer chooser GUI.
 *
 * @author Doomshade
 * @version 1.0
 */
@SuppressWarnings("ALL")
public class EditTraitCommand extends AbstractCommand {

    public EditTraitCommand() {
        setCommand("edit-trait");
        setDescription("Edits the trait of the selected NPC");
        setRequiresPlayer(true);
        addPermission(Permissions.BUILDER);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        final NPC selected = CitizensAPI.getDefaultNPCSelector().getSelected(sender);
        if (selected == null) {
            sender.sendMessage("You must have an NPC selected to edit the trainer");
            return;
        }

        if (!selected.hasTrait(TrainerTrait.class)) {
            sender.sendMessage(selected.getName() + ChatColor.RESET + " does not have the trainer trait (professiontrainer)!");
            return;
        }

        final TrainerTrait trait = selected.getTrait(TrainerTrait.class);
        trait.openTrainerChooserGUI((Player) sender);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public String getID() {
        return "traitedit";
    }
}
