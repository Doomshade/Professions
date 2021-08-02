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
import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.event.ProfessionExpGainEvent;

/**
 * User's profession data
 *
 * @author Doomshade
 * @version 1.0
 */
public interface IUserProfessionData {

    /**
     * Saves the profession data
     */
    void save();

    /**
     * @return the profession
     */
    Profession getProfession();

    /**
     * @return the user
     */
    IUser getUser();

    /**
     * @return the current exp
     */
    double getExp();

    /**
     * Sets current exp to the value. Adds a level if {@code exp > } {@link #getRequiredExp()}
     *
     * @param exp the exp to set
     */
    void setExp(double exp);

    /**
     * @return the current level
     */
    int getLevel();

    /**
     * Sets current level to the value. This method also ensures you won't be able to go over the {@link
     * #getLevelCap()}.
     *
     * @param level the level to set to
     */
    void setLevel(int level);

    /**
     * Adds exp to the user's profession
     *
     * @param exp    the amount of exp to give
     * @param source the source of exp ({@code null} for command source)
     *
     * @return {@code true} if {@code exp > 0} and the {@link ProfessionExpGainEvent#isCancelled()} returns {@code
     * false}.
     */
    boolean addExp(double exp, ItemType<?> source);

    /**
     * This method calculates every time it is called, so be careful.
     *
     * @return required exp for next level
     */
    int getRequiredExp();

    /**
     * @return the level cap
     */
    int getLevelCap();

    /**
     * Adds levels to the user's profession data
     *
     * @param level the level to add
     *
     * @return {@code true} if the level was added, {@code false} otherwise
     */
    boolean addLevel(int level);

    /**
     * This method also ensures that, if in any case the {@link IUserProfessionData#getLevel()} is greater than {@link
     * IUserProfessionData#getLevelCap()}, the level is set to the level cap.
     *
     * @return {@code true} if current level == {@link #getLevelCap()}.
     */
    boolean isMaxLevel();

    /**
     * Trains a user something new. This is saved as {@code extras} in user data file.
     *
     * @param trainable the trainable to train
     *
     * @return {@code true} if the user has successfully trained the item (has enough money and {@link
     * IUserProfessionData#hasTrained(ItemType)} returns {@code false}), false otherwise
     *
     * @see IUserProfessionData#addExtra(String)
     */
    boolean train(ItemType<?> trainable);

    /**
     * @param trainable the trainable to check for
     *
     * @return {@code true} if the user has already trained this, {@code false} otherwise
     *
     * @see #hasExtra(String)
     */
    boolean hasTrained(ItemType<?> trainable);

    /**
     * Adds something extra to the user for later usage. Used in {@link IUserProfessionData#train(ItemType)}.
     *
     * @param extra the extra to add
     */
    void addExtra(String extra);

    /**
     * Used in {@link IUserProfessionData#hasTrained(ItemType)}.
     *
     * @param extra the extra to look for
     *
     * @return {@code true} if the user has this extra, {@code false} otherwise
     */
    boolean hasExtra(String extra);
}
