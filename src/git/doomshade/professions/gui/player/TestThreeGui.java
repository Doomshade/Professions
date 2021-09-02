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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class TestThreeGui extends GUI {

    protected TestThreeGui(Player guiHolder, GUIManager manager) {
        super(guiHolder, manager);
    }

    @Override
    public void init() throws GUIInitializationException {
        Integer pos = getContext().getContext(ProfessionGUI.POSITION_GUI);
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_PICKAXE);
        Objects.requireNonNull(meta).setDisplayName(getContext().getContext(PlayerProfessionsGUI.ID_PROFESSION));
        GUIItem guiItem = new GUIItem(Material.DIAMOND_PICKAXE, pos, 1, (short) 0);
        guiItem.changeItem(this, () -> meta);
        final int sizeMultiplier = 9;
        final int rows = 4;
        setInventory(getInventoryBuilder().size(sizeMultiplier)
                .withItem(guiItem)
                .title("PERFECTO TOT")
                .size(sizeMultiplier * rows)
                .build());
    }

    @Override
    public void onGuiClick(GUIClickEvent e) {
        e.getEvent().setCancelled(true);
    }
}
