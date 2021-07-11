package git.doomshade.professions.trait;

import git.doomshade.professions.commands.CommandHandler;
import git.doomshade.professions.commands.EditTraitCommand;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.Utils;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCTraitCommandAttachEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.logging.Level;

public class TrainerListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onRightClick(NPCRightClickEvent e) {
        final NPC npc = e.getNPC();
        if (!npc.hasTrait(TrainerTrait.class)) return;

        TrainerTrait trait = npc.getTrait(TrainerTrait.class);
        final Player player = e.getClicker();

        String trainerId = trait.getTrainerId();
        if (trainerId == null || trainerId.isEmpty()) {
            final String s = "Could not resolve trainer ID, please contact an admin.";
            player.sendMessage(s);
            throw new RuntimeException(s);
        }
        trait.openTrainerGUI(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTraitAdd(NPCTraitCommandAttachEvent e) throws Utils.SearchNotFoundException {
        if (!e.getTraitClass().equals(TrainerTrait.class)) return;
        final NPC npc = e.getNPC();
        final TrainerTrait trait = npc.getTrait(TrainerTrait.class);

        final CommandSender sender = e.getCommandSender();

        if (!(sender instanceof Player)) {
            final CommandHandler handler = CommandHandler.getInstance(CommandHandler.class);
            ProfessionLogger.log("Attached trainer trait to " + npc.getName() + ". Please use " + handler.infoMessage(handler.getCommand(EditTraitCommand.class)) + ChatColor.RESET + " ingame with the NPC selected.", Level.WARNING);
        } else {
            trait.openTrainerChooserGUI((Player) sender);
        }
    }


}
