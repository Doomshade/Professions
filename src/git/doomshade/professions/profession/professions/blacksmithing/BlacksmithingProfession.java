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

package git.doomshade.professions.profession.professions.blacksmithing;

import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.item.ext.ItemType;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.io.ProfessionLogger;
import git.doomshade.professions.utils.Utils;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.logging.Level;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class BlacksmithingProfession extends Profession {


    @Override
    public void onLoad() {
        utils.addItems(BSItemType.class);
    }

    @Override
    public String getID() {
        return "blacksmithing";
    }

    @Override
    public <IType extends ItemType<?>> void onEvent(ProfessionEventWrapper<IType> ev) {
        final ProfessionEvent<BSItemType> event = utils.getEventUnsafe(ev, BSItemType.class);
        String expMsg = "";
        if (utils.addExp(event)) {
            expMsg = Utils.getReceiveXp(event.getExp());
        }
        ItemStack item = event.getItemType().getObject().item;
        String itemName = item != null ?
                item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).hasDisplayName() ?
                        item.getItemMeta().getDisplayName() :
                        item.getType().name() :
                "NULL";
        ProfessionLogger.log(
                String.format("%s smithed %s".concat(expMsg), event.getPlayer().getPlayer().getName(), itemName),
                Level.CONFIG);
    }

    @Override
    public boolean isSubprofession() {
        return false;
    }
}
