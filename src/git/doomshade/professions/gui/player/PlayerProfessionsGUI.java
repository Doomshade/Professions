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

package git.doomshade.professions.gui.player;

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
        Builder builder =
                getInventoryBuilder().size(9).title(Settings.getSettings(GUISettings.class).getProfessionsGuiName());
        final Player holder = getHolder();
        if (holder == null) {
            return;
        }
        User user = User.getUser(holder);
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
