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

package git.doomshade.professions.api;

import git.doomshade.guiapi.GUIApi;
import git.doomshade.guiapi.GUIManager;
import git.doomshade.professions.Professions;
import git.doomshade.professions.api.profession.IProfessionManager;
import git.doomshade.professions.api.user.IUser;
import org.bukkit.entity.Player;

/**
 * The API starting point
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public interface IProfessionAPI {

    /**
     * @return an implementation of this API
     */
    static IProfessionAPI getApi() {
        return Professions.getInstance();
    }

    /**
     * {@link GUIApi}'s {@link GUIManager} instance
     *
     * @return the {@link GUIManager} instance
     */
    GUIManager getGUIManager();

    /**
     * @return the profession manager
     */
    IProfessionManager getProfessionManager();

    /**
     * @param player the player
     *
     * @return the user
     */
    IUser getUser(Player player);
}
