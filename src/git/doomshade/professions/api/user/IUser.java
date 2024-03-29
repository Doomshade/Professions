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

package git.doomshade.professions.api.user;

import git.doomshade.professions.api.Profession;
import git.doomshade.professions.api.item.ext.ItemType;
import git.doomshade.professions.exceptions.PlayerHasNoProfessionException;
import git.doomshade.professions.user.User;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Collection;

/**
 * Player with additional data and methods
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public interface IUser {
    /**
     * Unloads the player (used when player is logging off)
     */
    void unloadUser();

    /**
     * @param prof the profession to look for
     *
     * @return true if this user has already that profession
     */
    boolean hasProfession(Profession prof);

    /**
     * Professes the player
     *
     * @param prof profession to profess
     *
     * @return true if professed successfully, false otherwise
     */
    boolean profess(Profession prof);

    /**
     * Unprofesses the player
     *
     * @param prof profession to profess
     *
     * @return true if unprofessed successfully, false otherwise
     */
    boolean unprofess(Profession prof);

    /**
     * Adds exp to the player.
     *
     * @param exp    the amount of exp to give
     * @param prof   the profession of this user
     * @param source the item source
     *
     * @return {@link IUserProfessionData#addExp(double, ItemType)}
     */
    boolean addExp(double exp, Profession prof, ItemType<?> source);

    /**
     * Returns all user's professions
     *
     * @return collection of users profession data
     */
    Collection<IUserProfessionData> getProfessions();

    /**
     * @return the player
     */
    Player getPlayer();

    /**
     * Gets the profession data
     *
     * @param prof the profession
     *
     * @return the {@link User}'s {@link Profession} data if the user has the profession, null otherwise
     */
    IUserProfessionData getProfessionData(Profession prof) throws PlayerHasNoProfessionException;

    /**
     * Gets the profession data
     *
     * @param profClass the profession's class
     *
     * @return the {@link User}'s {@link Profession} data if the user has the profession, null otherwise
     *
     * @throws PlayerHasNoProfessionException if player does not have the profession
     */
    IUserProfessionData getProfessionData(Class<? extends Profession> profClass) throws PlayerHasNoProfessionException;

    /**
     * Saves the user's data
     *
     * @throws IOException ex
     */
    void save() throws IOException;

    /**
     * Used for bypassing level requirements
     *
     * @return {@code true} if the user bypasses level requirement, {@code false} otherwise
     */
    boolean isBypass();

    /**
     * Sets whether or not this user should bypass level requirement. Usable for admins. Sets {@link
     * #setSuppressExpEvent(boolean)} to {@code false} if {@code bypass} is {@code false}
     *
     * @param bypass the bypass
     */
    void setBypass(boolean bypass);

    /**
     * Used for suppressing the user from receiving experience
     *
     * @return {@code true} if the user is being suppressed from receiving exp, {@code false} otherwise
     */
    boolean isSuppressExpEvent();

    /**
     * Sets the player to suppress the player from receiving exp
     *
     * @param suppressExpEvent the suppressExpEvent
     */
    void setSuppressExpEvent(boolean suppressExpEvent);
}
