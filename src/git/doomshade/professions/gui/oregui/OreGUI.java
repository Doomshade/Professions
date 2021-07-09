package git.doomshade.professions.gui.oregui;

import git.doomshade.guiapi.*;
import git.doomshade.professions.Professions;
import git.doomshade.professions.api.types.ItemTypeHolder;
import git.doomshade.professions.profession.professions.mining.OreItemType;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class OreGUI extends GUI {

    private boolean ignore = false;
    public static final NamespacedKey NBT_KEY = new NamespacedKey(Professions.getInstance(), "ignoreRange");

    protected OreGUI(Player guiHolder, GUIManager manager) {
        super(guiHolder, manager);
    }

    @Override
    public void init() throws GUIInitializationException {
        GUIInventory.Builder builder = getInventoryBuilder().title(ChatColor.DARK_GREEN + "Ores");
        final ItemTypeHolder<OreItemType> holder = Professions.getProfessionManager().getItemTypeHolder(OreItemType.class);
        ignore = getContext().getContext("ignore");

        int i = -1;
        for (OreItemType ore : holder.getRegisteredItemTypes()) {
            GUIItem item = new GUIItem(ore.getGuiMaterial().getType(), ++i, 1, ore.getGuiMaterial().getDurability());
            final ItemStack click = ore.getIcon(null).clone();
            final ItemMeta itemMeta = click.getItemMeta();
            final List<String> lore = itemMeta.getLore();
            lore.add(ChatColor.BLUE + "Ignores range: " + (ignore ? ChatColor.GREEN : ChatColor.RED) + ignore);
            final String s = Strings.ItemTypeEnum.LEVEL_REQ_COLOR.s;
            lore.replaceAll(x -> x.contains(s) ? x.replaceAll("\\{" + s + "}", ChatColor.GREEN + "") : x);
            itemMeta.setLore(lore);
            click.setItemMeta(itemMeta);
            item.changeItem(this, click::getItemMeta);
            builder = builder.withItem(item);
        }

        setInventory(builder.build());
    }

    @Override
    public void onGuiClick(GUIClickEvent e) {
        final InventoryClickEvent event = e.getEvent();
        event.setCancelled(true);
        ItemStack click = event.getCurrentItem();
        if (click == null || !click.hasItemMeta() || click.getItemMeta() == null || !click.getItemMeta().hasDisplayName()) {
            return;
        }
        try {
            Utils.findInIterable(Professions.getProfessionManager()
                            .getItemTypeHolder(OreItemType.class).getRegisteredItemTypes(),
                    x -> x.getIcon(null)
                            .getItemMeta()
                            .getDisplayName()
                            .equals(click.getItemMeta().getDisplayName()));
        } catch (Utils.SearchNotFoundException ex) {
            return;
        }

        final PersistentDataContainer pdc = click.getItemMeta().getPersistentDataContainer();
        pdc.set(NBT_KEY, PersistentDataType.BYTE, ignore ? (byte) 1 : 0);
        event.getWhoClicked().getInventory().addItem(click);
    }
}
