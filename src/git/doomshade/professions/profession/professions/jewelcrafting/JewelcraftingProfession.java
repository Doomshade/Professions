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

package git.doomshade.professions.profession.professions.jewelcrafting;

import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public final class JewelcraftingProfession extends Profession {

    @Override
    public void onLoad() {
        utils.addItems(GemItemType.class);
    }

    @Override
    public String getID() {
        return "jewelcrafting";
    }

    @Override
    public <A extends ItemType<?>> void onEvent(ProfessionEventWrapper<A> event) {
        final ProfessionEvent<A> e = event.event;

        if (!utils.playerMeetsLevelRequirements(e)) {
            e.setCancelled(true);
            e.printErrorMessage(utils.getUserProfessionData(e.getPlayer()));
            return;
        }

        utils.addExp(e);
    }

    @Override
    public boolean isSubprofession() {
        return false;
    }
}
