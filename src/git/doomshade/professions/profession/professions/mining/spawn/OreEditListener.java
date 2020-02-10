package git.doomshade.professions.profession.professions.mining.spawn;

import git.doomshade.guiapi.GUI;
import git.doomshade.professions.Professions;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.gui.oregui.OreGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class OreEditListener implements Listener {


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        // no reason to check if no block right clicked
        if (!event.hasBlock() || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        final Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItemInMainHand();

        // Checks if the block is clicked with editor and is a wool
        if (hand == null || hand.getType() != Settings.getEditItem())
            return;

        final Optional<? extends GUI> optionalGUI = Professions.getGUIManager().getGui(OreGUI.class, player);
        if (optionalGUI.isPresent()) {
            GUI gui = optionalGUI.get();
            gui.getContext().addContext(OreGUI.ORE_LOCATION, event.getClickedBlock().getLocation());
        }
    }
}
