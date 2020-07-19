package git.doomshade.professions.gui.oregui;

import git.doomshade.guiapi.*;
import git.doomshade.professions.Professions;
import git.doomshade.professions.profession.professions.mining.OreItemType;
import git.doomshade.professions.profession.types.ItemTypeHolder;
import git.doomshade.professions.utils.Strings;
import git.doomshade.professions.utils.Utils;
import net.minecraft.server.v1_9_R1.NBTTagByte;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class OreGUI extends GUI {

    private boolean ignore = false;

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
        if (click == null || !click.hasItemMeta() || !click.getItemMeta().hasDisplayName()) {
            return;
        }
        try {
            Utils.findInIterable(Professions.getProfessionManager().getItemTypeHolder(OreItemType.class).getRegisteredItemTypes(), x -> x.getIcon(null).getItemMeta().getDisplayName().equals(click.getItemMeta().getDisplayName()));
        } catch (Utils.SearchNotFoundException ex) {
            return;
        }
        net.minecraft.server.v1_9_R1.ItemStack is = CraftItemStack.asNMSCopy(click);
        NBTTagCompound nbtTag = is.hasTag() ? is.getTag() : new NBTTagCompound();
        nbtTag.set("ignoreRange", new NBTTagByte(ignore ? (byte) 1 : 0));
        is.setTag(nbtTag);
        event.getWhoClicked().getInventory().addItem(CraftItemStack.asBukkitCopy(is));
    }
}
