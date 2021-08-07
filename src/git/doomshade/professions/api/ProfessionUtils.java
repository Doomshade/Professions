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

import git.doomshade.professions.Professions;
import git.doomshade.professions.api.item.ext.ItemType;
import git.doomshade.professions.enums.Messages;
import git.doomshade.professions.event.ProfessionEvent;
import git.doomshade.professions.event.ProfessionEventWrapper;
import git.doomshade.professions.user.User;
import git.doomshade.professions.user.UserProfessionData;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Optional;

/**
 * Profession utilities
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public final class ProfessionUtils {
    private final Profession profession;

    public ProfessionUtils(Profession profession) {
        this.profession = profession;
    }

    /**
     * Casts desired event to another one
     *
     * @param event the profession event to cast
     * @param <A>   the generic argument of the event
     * @param clazz the generic argument class
     *
     * @return the casted event
     *
     * @throws ClassCastException if the event couldn't be cast
     */
    public <A extends ItemType<?>> ProfessionEvent<A> getEventUnsafe(ProfessionEvent<?> event, Class<A> clazz)
            throws ClassCastException {
        return getEvent(event, clazz).orElse(null);
    }

    /**
     * Casts desired event to another one
     *
     * @param event the profession event to cast
     * @param <A>   the generic argument of the event
     * @param clazz the generic argument class
     *
     * @return the casted event
     *
     * @throws ClassCastException if the event couldn't be cast
     */
    @SuppressWarnings({"unchecked"})
    public <A extends ItemType<?>> Optional<ProfessionEvent<A>> getEvent(ProfessionEvent<?> event,
                                                                         Class<A> clazz)
            throws ClassCastException {
        try {
            return Optional.of((ProfessionEvent<A>) event);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Casts desired event to another one
     *
     * @param event the profession event to cast
     * @param <A>   the generic argument of the event
     * @param clazz the generic argument class
     *
     * @return the casted event
     *
     * @throws ClassCastException if the event couldn't be cast
     */
    public <A extends ItemType<?>> ProfessionEvent<A> getEventUnsafe(ProfessionEventWrapper<?> event,
                                                                     Class<A> clazz)
            throws ClassCastException {
        return getEvent(event, clazz).orElse(null);
    }

    /**
     * Casts desired event to another one
     *
     * @param event the profession event to cast
     * @param <A>   the generic argument of the event
     * @param clazz the generic argument class
     *
     * @return the casted event
     *
     * @throws ClassCastException if the event couldn't be cast
     */
    public <A extends ItemType<?>> Optional<ProfessionEvent<A>> getEvent(ProfessionEventWrapper<?> event,
                                                                         Class<A> clazz)
            throws ClassCastException {
        return getEvent(event.event, clazz);
    }

    /**
     * @param e the event to check the player's requirements in
     *
     * @return {@code true} if the player meets all requirements, {@code false} otherwise
     */
    public final <ItemTypeClass extends ItemType<?>> boolean playerMeetsLevelRequirements(
            ProfessionEvent<ItemTypeClass> e) {
        if (!playerHasProfession(e)) {
            return false;
        }
        ItemTypeClass obj = e.getItemType();
        UserProfessionData upd = getUserProfessionData(e.getPlayer());
        return obj.meetsLevelReq(upd.getLevel()) || upd.getUser().isBypass();
    }

    /**
     * @param user the user to get the profession data from
     *
     * @return the user profession data
     *
     * @see User#getProfessionData(Profession)
     */
    public final UserProfessionData getUserProfessionData(User user) {
        return user.getProfessionData(profession);
    }

    /**
     * @param e event to check for
     *
     * @return {@code true} if the player has a profession of this called event, {@code false} otherwise
     */
    public final <ItemTypeClass extends ItemType<?>> boolean playerHasProfession(ProfessionEvent<ItemTypeClass> e) {
        return e.getPlayer().hasProfession(profession);
    }

    /**
     * @param e event to check for
     *
     * @return {@code true} if event has registered an object of item type, {@code false} otherwise
     */
    public final <ItemTypeClass extends ItemType<?>> boolean isValidEvent(ProfessionEvent<ItemTypeClass> e) {
        return isValidEvent(e, true);
    }

    /**
     * @param e            event to check for
     * @param errorMessage whether or not to log error message
     *
     * @return {@code true} if event has registered an object of item type, {@code false} otherwise
     */
    public final <ItemTypeClass extends ItemType<?>> boolean isValidEvent(ProfessionEvent<ItemTypeClass> e,
                                                                          boolean errorMessage) {
        final boolean playerHasProf = playerHasProfession(e);
        if (!playerHasProf) {
            e.setCancelled(true);
            if (errorMessage) {
                final User player = e.getPlayer();
                player.sendMessage(new Messages.MessageBuilder(Messages.Global.PROFESSION_REQUIRED_FOR_THIS_ACTION)
                        .player(player)
                        .profession(profession)
                        .build());
            }
        }
        return playerHasProf;
    }

    /**
     * Adds the exp to the user based on the event given.
     *
     * @param e the event
     *
     * @return {@code true} if the exp was given, {@code false} otherwise
     */
    public final boolean addExp(ProfessionEvent<?> e) {
        return addExp(e.getExp(), e.getPlayer(), e.getItemType());
    }

    /**
     * Adds exp to the user based on the source
     *
     * @param exp    the exp to add
     * @param user   the user to add the exp to
     * @param source the source of exp
     *
     * @return {@code true} if the exp was added successfully, {@code false} otherwise
     *
     * @see User#addExp(double, Profession, ItemType)
     */
    public final boolean addExp(double exp, User user, ItemType<?> source) {
        if (!user.isSuppressExpEvent()) {
            return user.addExp(exp, profession, source);
        }
        return false;
    }

    /**
     * Adds {@link ItemType}s to this profession to handle in
     *
     * @param items the items
     */
    public final <T extends ConfigurationSerializable, A extends ItemType<T>> void addItems(Class<A> items) {
        profession.items.add(Professions.getProfMan().getItemTypeHolder(items));
    }
}
