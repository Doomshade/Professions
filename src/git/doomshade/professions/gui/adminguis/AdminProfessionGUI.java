package git.doomshade.professions.gui.adminguis;

import git.doomshade.guiapi.*;
import git.doomshade.professions.Profession;
import git.doomshade.professions.listeners.UserListener;
import git.doomshade.professions.utils.Strings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdminProfessionGUI extends GUI {
    private Profession<?> prof;

    protected AdminProfessionGUI(Player guiHolder, GUIManager manager) {
        super(guiHolder, manager);
    }

    @Override
    public void init() throws GUIInitializationException {
        this.prof = (Profession<?>) getContext().getContext(AdminProfessionsGUI.ID_PROFESSION);
        GUIInventory.Builder builder = getInventoryBuilder().size(18).title(prof.getColoredName());

        int i = -1;
        for (Strings.ItemTypeEnum e : Strings.ItemTypeEnum.values()) {
            ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.CHEST);
            meta.setDisplayName(e.s);
            GUIItem item = new GUIItem(Material.CHEST, ++i);
            item.changeItem(this, () -> meta);

            builder = builder.withItem(item);
        }
        setInventory(builder.build());
    }

    @Override
    public void onGuiClick(GUIClickEvent e) {
        InventoryClickEvent event = e.getEvent();
        event.setCancelled(true);
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || currentItem.getType() == Material.AIR || !currentItem.hasItemMeta() || !currentItem.getItemMeta().hasDisplayName()) {
            return;
        }

        HumanEntity he = event.getWhoClicked();
        if (!(he instanceof Player)) {
            return;
        }
        Player player = (Player) he;

        String name = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
        for (Strings.ItemTypeEnum s : Strings.ItemTypeEnum.values()) {
            if (name.equals(s.s)) {
                Object obj = s.getDefaultValues().get(s);

                String className = obj.getClass().getSimpleName().toUpperCase();

                try {
                    UserListener.ValidInputType vit = UserListener.ValidInputType.valueOf(className);
                    UserListener.askUser(player, "INPUT BOI", vit, this);
                    event.getWhoClicked().closeInventory();
                } catch (IllegalArgumentException ex) {
                    return;
                }
            }
        }
    }

    @Override
    public void onCustomEvent(Object obj) {
        if (!(obj instanceof String)) {
            return;
        }

        String input = (String) obj;

        // write to file
        getManager().openGui(this);
    }
}
