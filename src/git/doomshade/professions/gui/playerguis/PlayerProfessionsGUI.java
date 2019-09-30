package git.doomshade.professions.gui.playerguis;

import git.doomshade.guiapi.*;
import git.doomshade.guiapi.GUIInventory.Builder;
import git.doomshade.professions.Professions;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Supplier;

public class PlayerProfessionsGUI extends GUI {
    static final String ID_PROFESSION = "name";

    protected PlayerProfessionsGUI(Player guiHolder, GUIManager manager) {
        super(guiHolder, manager);
    }

    @Override
    public void init() throws GUIInitializationException {
        Builder builder = getInventoryBuilder().size(9).title("PERFECTO TITLOS");
        User user = User.getUser(getHolder());
        int i = -1;
        for (UserProfessionData upd : user.getProfessions()) {
            GUIItem item = new GUIItem(upd.getProfession().getIcon().getType(), ++i);
            item.changeItem(this, new Supplier<ItemMeta>() {

                @Override
                public ItemMeta get() {
                    // TODO Auto-generated method stub
                    return upd.getProfession().getIcon().getItemMeta();
                }
            });
            builder = builder.withItem(item);
        }
        setInventory(builder.build());
        setNextGui(ProfessionGUI.class, Professions.getManager());
    }

    @Override
    public void onGuiClick(GUIClickEvent e) {
        // TODO Auto-generated method stub
        InventoryClickEvent event = e.getEvent();
        event.setCancelled(true);
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || currentItem.getType() == Material.AIR) {
            return;
        }
        GUI gui = getNextGui();
        gui.getContext().addContext(ID_PROFESSION, Professions.fromName(currentItem));
        Professions.getManager().openGui(gui);
    }

}
