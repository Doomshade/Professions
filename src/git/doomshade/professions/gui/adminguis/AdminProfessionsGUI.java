package git.doomshade.professions.gui.adminguis;

import git.doomshade.guiapi.*;
import git.doomshade.professions.Professions;
import git.doomshade.professions.profession.Profession;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AdminProfessionsGUI extends GUI {
    static final String ID_PROFESSION = "prof";

    protected AdminProfessionsGUI(Player guiHolder, GUIManager manager) {
        super(guiHolder, manager);
    }

    @Override
    public void init() throws GUIInitializationException {
        GUIInventory.Builder builder = getInventoryBuilder().size(9);

        int i = -1;
        for (Profession prof : Professions.getProfessionManager().getProfessionsById().values()) {
            ItemStack icon = prof.getIcon();
            GUIItem item = new GUIItem(icon.getType(), ++i, icon.getAmount(), icon.getDurability());
            item.changeItem(this, icon::getItemMeta);

            builder = builder.withItem(item);
        }

        setInventory(builder.build());
        setNextGui(AdminProfessionGUI.class, Professions.getGUIManager());
    }

    @Override
    public void onGuiClick(GUIClickEvent e) {
        InventoryClickEvent event = e.getEvent();
        event.setCancelled(true);
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || currentItem.getType() == Material.AIR) {
            return;
        }
        GUI gui = getNextGui();
        gui.getContext().addContext(ID_PROFESSION, Professions.getProfession(currentItem));
        Professions.getGUIManager().openGui(gui);
    }
}
