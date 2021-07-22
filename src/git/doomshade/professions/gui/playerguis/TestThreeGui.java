package git.doomshade.professions.gui.playerguis;

import git.doomshade.guiapi.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class TestThreeGui extends GUI {

    protected TestThreeGui(Player guiHolder, GUIManager manager) {
        super(guiHolder, manager);
    }

    @Override
    public void init() throws GUIInitializationException {
        Integer pos = getContext().getContext(ProfessionGUI.POSITION_GUI);
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
        Objects.requireNonNull(meta).setDisplayName(getContext().getContext(PlayerProfessionsGUI.ID_PROFESSION));
        GUIItem guiItem = new GUIItem(Material.DIAMOND_PICKAXE, pos, 1, (short) 0);
        guiItem.changeItem(this, () -> meta);
        setInventory(getInventoryBuilder().size(9).withItem(guiItem).title("PERFECTO TOT").size(9 * 4).build());
    }

    @Override
    public void onGuiClick(GUIClickEvent e) {
        e.getEvent().setCancelled(true);
    }
}
