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

package git.doomshade.professions.event;

import git.doomshade.professions.api.Profession;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * An extension of {@link Event}
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractProfessionEvent extends Event implements Cancellable {
    protected final User user;
    protected final Profession profession;
    protected final UserProfessionData userProfessionData;
    private boolean cancelled;

    protected AbstractProfessionEvent(UserProfessionData data) {
        this.user = data.getUser();
        this.profession = data.getProfession();
        userProfessionData = data;
        this.cancelled = false;
    }

    public final Profession getProfession() {
        return profession;
    }

    public final User getUser() {
        return user;
    }

    public final UserProfessionData getUserProfessionData() {
        return userProfessionData;
    }

    @Override
    public final boolean isCancelled() {
        return cancelled;
    }

    @Override
    public final void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
