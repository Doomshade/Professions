package git.doomshade.professions.gui.playerguis;

import git.doomshade.guiapi.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

public class TestThreeGui extends GUI {

    protected TestThreeGui(Player guiHolder, GUIManager manager) {
        super(guiHolder, manager);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void init() throws GUIInitializationException {
        // TODO Auto-generated method stub
        Integer pos = (Integer) getContext().getContext(ProfessionGUI.POSITION_GUI);
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
        meta.setDisplayName((String) getContext().getContext(PlayerProfessionsGUI.ID_PROFESSION));
        GUIItem guiItem = new GUIItem(Material.DIAMOND_PICKAXE, pos);
        guiItem.changeItem(this, () -> meta);
        setInventory(getInventoryBuilder().size(9).withItem(guiItem).title("PERFECTO TOT").size(9 * 4).build());
    }

    @Override
    public void onGuiClick(GUIClickEvent e) {
        // TODO Auto-generated method stub
        e.getEvent().setCancelled(true);
    }
}
