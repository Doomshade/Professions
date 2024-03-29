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

package git.doomshade.professions.gui.admin;

import git.doomshade.guiapi.*;
import git.doomshade.professions.api.Profession;
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

import java.util.Objects;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class AdminProfessionGUI extends GUI {

    protected AdminProfessionGUI(Player guiHolder, GUIManager manager) {
        super(guiHolder, manager);
    }

    @Override
    public void init() {
        Profession prof = getContext().getContext(AdminProfessionsGUI.ID_PROFESSION);
        GUIInventory.Builder builder = getInventoryBuilder().size(18).title(prof.getColoredName());

        int i = -1;
        for (Strings.ItemTypeEnum e : Strings.ItemTypeEnum.values()) {
            ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.CHEST);
            Objects.requireNonNull(meta).setDisplayName(e.s);
            GUIItem item = new GUIItem(Material.CHEST, ++i, 1, (short) 0);
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
        if (currentItem == null || currentItem.getType() == Material.AIR || !currentItem.hasItemMeta() ||
                !Objects.requireNonNull(
                        currentItem.getItemMeta()).hasDisplayName()) {
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
