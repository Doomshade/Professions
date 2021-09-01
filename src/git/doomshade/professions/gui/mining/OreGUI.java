/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jakub Šmrha
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package git.doomshade.professions.gui.mining;

import git.doomshade.guiapi.*;
import git.doomshade.professions.Professions;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.profession.professions.mining.Ore;
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
import java.util.Objects;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class OreGUI extends GUI {

    private boolean ignore = false;
    public static final NamespacedKey NBT_KEY = new NamespacedKey(Professions.getInstance(), "ignoreRange");

    protected OreGUI(Player guiHolder, GUIManager manager) {
        super(guiHolder, manager);
    }

    @Override
    public void init() throws GUIInitializationException {
        GUIInventory.Builder builder = getInventoryBuilder().title(ChatColor.DARK_GREEN + "Ores");
        final ItemTypeHolder<Ore, OreItemType> holder = Professions.getProfMan().getItemTypeHolder(OreItemType.class);
        ignore = getContext().getContext("ignore");

        int i = -1;
        for (OreItemType ore : holder) {
            GUIItem item = new GUIItem(ore.getGuiMaterial().getType(), ++i, 1, ore.getGuiMaterial().getDurability());
            final ItemStack click = ore.getIcon(null).clone();
            final ItemMeta itemMeta = click.getItemMeta();
            final List<String> lore = Objects.requireNonNull(itemMeta).getLore();
            Objects.requireNonNull(lore)
                    .add(ChatColor.BLUE + "Ignores range: " + (ignore ? ChatColor.GREEN : ChatColor.RED) + ignore);
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
        if (click == null || !click.hasItemMeta() || click.getItemMeta() == null ||
                !click.getItemMeta().hasDisplayName()) {
            return;
        }
        try {
            Utils.findInIterable(Professions.getProfMan()
                            .getItemTypeHolder(OreItemType.class),
                    x -> Objects.requireNonNull(x.getIcon(null)
                                    .getItemMeta())
                            .getDisplayName()
                            .equals(click.getItemMeta().getDisplayName()));
        } catch (Utils.SearchNotFoundException ex) {
            return;
        }

        final PersistentDataContainer pdc = Objects.requireNonNull(click.getItemMeta()).getPersistentDataContainer();
        pdc.set(NBT_KEY, PersistentDataType.BYTE, ignore ? (byte) 1 : 0);
        event.getWhoClicked().getInventory().addItem(click);
    }
}
