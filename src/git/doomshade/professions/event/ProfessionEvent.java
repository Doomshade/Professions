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

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.IProfession;
import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.item.ext.ItemType;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.ItemUtils;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An event that professions handle
 *
 * @param <T> an item type
 *
 * @author Doomshade
 * @version 1.0
 * @see Profession#onEvent(ProfessionEventWrapper)
 * @since 1.0
 */
public class ProfessionEvent<T extends ItemType<?>> extends Event implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final User user;
    private final Collection<Object> extras = new ArrayList<>();
    private final List<String> errorMessage = new ArrayList<>();
    private final Class<? extends IProfession>[] professions;
    private T t;
    private boolean cancel = false;
    private int exp;

    public ProfessionEvent(T t, User user, Class<? extends IProfession>[] professions) {
        this.t = t;
        this.user = user;
        this.exp = t.getExp();
        @SuppressWarnings("all") final List<String> errMessages =
                Professions.getProfMan().getItemTypeHolder(t.getClass()).getErrorMessage();
        this.errorMessage.addAll(errMessages);
        this.professions = new Class[professions.length];
        System.arraycopy(professions, 0, this.professions, 0, this.professions.length);
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public Class<? extends IProfession>[] getProfessions() {
        return professions;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public void printErrorMessage(UserProfessionData upd) {
        List<String> message = getErrorMessage(upd);
        if (message.isEmpty()) {
            return;
        }
        upd.getUser().sendMessage(message.toArray(new String[0]));
    }

    public List<String> getErrorMessage(UserProfessionData upd) {
        return ItemUtils.getDescription(t, errorMessage, upd);
    }

    public void addExtras(Iterable<?> iterable) {
        for (Object obj : iterable) {
            addExtra(obj);
        }
    }

    public void addExtra(Object extra) {
        extras.add(extra);
    }

    public Collection<?> getExtras() {
        return extras;
    }

    public void setExtras(Collection<?> extras) {
        this.extras.clear();
        addExtras(extras);
    }

    public void addExtras(Collection<?> extraObjects) {
        extras.addAll(extraObjects);
    }

    @SuppressWarnings("unchecked")
    public <A> Iterable<A> getExtras(Class<A> clazz) {
        return extras.stream()
                .filter(o -> o.getClass().getName().equals(clazz.getName()))
                .map(o -> (A) o)
                .collect(Collectors.toList());
    }

    public boolean hasExtra(Class<?> clazz) {
        return getExtra(clazz) != null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <A> A getExtra(Class<A> clazz) {
        return (A) extras.stream()
                .filter(o -> o.getClass().getName().equals(clazz.getName()))
                .findFirst()
                .orElse(null);
    }

    public T getItemType() {
        return t;
    }

    public void setItemType(T t) {
        this.t = t;
    }

    public User getPlayer() {
        return user;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.cancel = arg0;
    }
}
