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
import git.doomshade.professions.api.profession.IProfession;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.user.User;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.PluginManager;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A helper class for event managing
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public final class EventManager {

    private static final EventManager EM = new EventManager();
    private static final PluginManager PM = Bukkit.getPluginManager();

    private EventManager() {
    }

    public static EventManager getInstance() {
        return EM;
    }

    /**
     * @param object        the generic type object inside an item type
     * @param itemTypeClass the item type class
     * @param <T>           the generic type object
     * @param <Item>        the item type
     *
     * @return an item type if such exists or {@code null}
     */
    @Nullable
    public <T extends ConfigurationSerializable, Item extends ItemType<T>> Item getItemType(T object,
                                                                                            Class<Item> itemTypeClass) {
        if (object == null || itemTypeClass == null) {
            return null;
        }

        // get the item type holder
        final ItemTypeHolder<T, Item> itemTypeHolder = Professions.getProfMan().getItemTypeHolder(itemTypeClass);

        // loop through item types and search for one that equals to the one provided
        for (Item it : itemTypeHolder) {
            final T itObject = it.getObject();
            if (itObject.equals(object) || it.equalsObject(object)) {
                return it;
            }
        }
        return null;
    }

    /**
     * Calls a profession event that professions handle
     *
     * @param itemType the item type
     * @param user     the user
     * @param extras   extras if needed
     * @param <T>      the item type
     *
     * @return the called profession event
     *
     * @see ProfessionEvent#getExtras(Class)
     */
    public <T extends ItemType<?>> ProfessionEvent<T> callEvent(T itemType, User user, List<Object> extras, Class<?
            extends IProfession>... professions) {
        return callEvent(getEvent(itemType, user, extras, professions));
    }

    /**
     * Gets a profession event that professions handle (this does <b>NOT</b> call the event)
     *
     * @param itemType    the item type
     * @param user        the user
     * @param extras      extras if needed
     * @param professions the professions to call this event for
     * @param <T>         the item type
     *
     * @return the called profession event
     *
     * @see ProfessionEvent#getExtras(Class)
     */
    @SafeVarargs
    public final <T extends ItemType<?>> ProfessionEvent<T> getEvent(T itemType, User user, List<Object> extras, Class<?
            extends IProfession>... professions) {
        if (professions == null) {
            return null;
        }
        ProfessionEvent<T> pe = new ProfessionEvent<>(itemType, user, professions);
        pe.setExtras(extras);
        return pe;
    }

    /**
     * Calls the event
     *
     * @param event the event to call
     * @param <T>   the item type
     *
     * @return the called profession event
     */
    public <T extends ItemType<?>> ProfessionEvent<T> callEvent(ProfessionEvent<T> event) {
        PM.callEvent(event);
        return event;
    }
}
