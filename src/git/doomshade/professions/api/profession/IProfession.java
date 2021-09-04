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

package git.doomshade.professions.api.profession;

import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.api.item.ItemTypeHolder;
import git.doomshade.professions.data.ProfessionSettingsManager;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.listeners.ProfessionListener;
import git.doomshade.professions.user.UserProfessionData;
import git.doomshade.professions.utils.ISetup;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public interface IProfession extends Listener, Comparable<Profession> {
    /**
     * @return the unique ID of this profession
     */
    String getID();

    /**
     * Called when an event related to this profession occurs
     *
     * @param e       the event
     * @param <IType> the ItemType
     */
    <IType extends ItemType<?>> void onEvent(ProfessionEventWrapper<IType> e);

    /**
     * @return {@code true} if this profession is a subprofession, {@code false} otherwise
     */
    boolean isSubprofession();

    /**
     * @return the icon of this profession (for GUI purposes)
     */
    ItemStack getIcon();

    /**
     * @return the handled {@link ItemType}'s holders
     */
    Iterable<ItemTypeHolder<?, ?>> getItems();

    /**
     * @return the profession settings
     */
    ProfessionSettingsManager getProfessionSettings();

    /**
     * @return the profession type
     */
    ProfessionType getProfessionType();

    /**
     * The colored name of this profession. Use this instead of getName() as that method does not translate the colour.
     *
     * @return the colored name of this profession
     */
    String getColoredName();

    /**
     * @return the name of this profession
     */
    String getName();

    /**
     * Called after being loaded from a file
     */
    void onLoad();

    /**
     * Called when a user levels up
     *
     * @param upd the user profession data
     */
    void onLevelUp(UserProfessionData upd);

    /**
     * @return the required plugin IDs
     */
    Collection<String> getRequiredPlugins();

    /**
     * Adds a plugin requirement
     *
     * @param plugin the plugin ID
     */
    void addRequiredPlugin(String plugin);

    /**
     * Handles the called profession event from {@link ProfessionListener} <br> Cancels the event if the player does not
     * have this profession and is a rank lower than builder <br> If the player has this profession and the profession
     * event is correct, {@link #onEvent(ProfessionEventWrapper)} is called
     *
     * @param event   the profession event
     * @param <IType> the item type argument of the event (this prevents wildcards)
     */
    @EventHandler
    <IType extends ItemType<?>> void handleEvent(ProfessionEvent<IType> event);

    @Nullable
    List<String> getProfessionInformation(UserProfessionData upd);

    /**
     * @return a collection of subprofessions of this profession
     */
    Collection<Class<? extends Profession>> getSubprofessions();


}
