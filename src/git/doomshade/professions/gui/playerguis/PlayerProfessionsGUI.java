package git.doomshade.professions.gui.playerguis;

import git.doomshade.guiapi.*;
import git.doomshade.guiapi.GUIInventory.Builder;
import git.doomshade.professions.Professions;
import git.doomshade.professions.api.user.IUserProfessionData;
import git.doomshade.professions.data.GUISettings;
import git.doomshade.professions.data.Settings;
import git.doomshade.professions.user.User;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerProfessionsGUI extends GUI {
    static final String ID_PROFESSION = "name";

    protected PlayerProfessionsGUI(Player guiHolder, GUIManager manager) {
        super(guiHolder, manager);
    }

    @Override
    public void init() throws GUIInitializationException {
        Builder builder = getInventoryBuilder().size(9).title(Settings.getSettings(GUISettings.class).getProfessionsGuiName());
        User user = User.getUser(getHolder());
        int i = -1;
        for (IUserProfessionData upd : user.getProfessions()) {
            final ItemStack icon = upd.getProfession().getIcon();
            GUIItem item = new GUIItem(icon.getType(), ++i, icon.getAmount(), icon.getDurability());
            item.changeItem(this, icon::getItemMeta);
            builder = builder.withItem(item);
        }
        setInventory(builder.build());
        setNextGui(ProfessionGUI.class, Professions.getGUIManager());
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
        gui.getContext().addContext(ID_PROFESSION, Professions.getProfMan().getProfession(currentItem));
        Professions.getGUIManager().openGui(gui);
    }

}
