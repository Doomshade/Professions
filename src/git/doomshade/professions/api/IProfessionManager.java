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

import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.api.item.ItemTypeHolder;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Class responsible for registration of a profession
 *
 * @author Doomshade
 * @version 1.0
 */
public interface IProfessionManager {

    /**
     * @param itemType the {@link ItemType} class
     * @param o        the supplier of an example object of {@link ItemType} argument
     * @param <T>      the {@link ItemType} argument
     * @param <IType>  the {@link ItemType}
     *
     * @throws IOException if the {@link ItemTypeHolder} couldn't be registered
     */
    default <T extends ConfigurationSerializable, IType extends ItemType<T>> void registerItemTypeHolder(
            Class<IType> itemType,
            Supplier<T> o) throws IOException {
        registerItemTypeHolder(itemType, o, null);
    }

    /**
     * @param itemType          the {@link ItemType} class
     * @param o                 the supplier of an example object of {@link ItemType} argument
     * @param additionalCommand the additional commands to be made before registration to file
     * @param <T>               the {@link ItemType} argument
     * @param <IType>           the {@link ItemType}
     *
     * @throws IOException if the {@link ItemTypeHolder} couldn't be registered to file
     */
    default <T extends ConfigurationSerializable, IType extends ItemType<T>> void registerItemTypeHolder(
            Class<IType> itemType, Supplier<T> o, Consumer<IType> additionalCommand) throws IOException {
        registerItemTypeHolder(itemType, o.get(), additionalCommand);

    }

    /**
     * @param itemType          the {@link ItemType} class
     * @param o                 the example object of {@link ItemType} argument
     * @param additionalCommand the additional commands to be made before registration to file
     * @param <T>               the {@link ItemType} argument
     * @param <IType>           the {@link ItemType}
     *
     * @throws IOException if the {@link ItemTypeHolder} couldn't be registered to file
     */
    <T extends ConfigurationSerializable, IType extends ItemType<T>> void registerItemTypeHolder(Class<IType> itemType,
                                                                                                 T o,
                                                                                                 Consumer<IType> additionalCommand)
            throws IOException;

    /**
     * @param itemType the {@link ItemType} class
     * @param o        the example object of {@link ItemType} argument
     * @param <T>      the {@link ItemType} argument
     * @param <IType>  the {@link ItemType}
     *
     * @throws IOException if the {@link ItemTypeHolder} couldn't be registered
     */
    default <T extends ConfigurationSerializable, IType extends ItemType<T>> void registerItemTypeHolder(
            Class<IType> itemType, T o) throws IOException {
        registerItemTypeHolder(itemType, o, null);
    }

    /**
     * Registers a profession<br> Make sure you only create a single instance of the profession, multiple instances are
     * disallowed and will throw an exception
     *
     * @param prof the profession to register
     */
    void registerProfession(Profession prof);

    /**
     * @param clazz the {@link ItemTypeHolder} class to look for
     * @param <A>   the {@link ItemTypeHolder}'s {@link ItemType}
     *
     * @return instance of {@link ItemTypeHolder}
     */
    <T extends ConfigurationSerializable, A extends ItemType<T>> ItemTypeHolder<T, A> getItemTypeHolder(Class<A> clazz)
            throws IllegalArgumentException;

    /**
     * @param profession the {@link Profession} class
     *
     * @return the {@link Profession} if found
     *
     * @throws RuntimeException if the profession is not registered
     */
    Optional<Profession> getProfession(Class<? extends Profession> profession);

    /**
     * Calls {@link #getProfessionById(String)} with the {@link ItemStack}'s display name
     *
     * @param item the item to look for the profession from
     *
     * @return the profession
     *
     * @see #getProfessionById(String)
     */
    default Optional<Profession> getProfession(ItemStack item) {
        if (item == null || item.getItemMeta() == null || !item.getItemMeta().hasDisplayName()) {
            return Optional.empty();
        }
        return getProfessionById(item.getItemMeta().getDisplayName());
    }

    /**
     * @param id the{@link Profession#getID()} of {@link Profession}
     *
     * @return the {@link Profession}
     *
     * @throws IllegalArgumentException if the id doesn't exist
     */
    Optional<Profession> getProfessionById(String id);

    /**
     * @param idOrName the id or name of the profession
     *
     * @return the profession based on either id or name in this order
     */
    default Optional<Profession> getProfession(String idOrName) {
        final Optional<Profession> id = getProfessionById(idOrName);
        if (id.isPresent()) {
            return id;
        }
        return getProfessionByName(idOrName);
    }

    /**
     * @param name the {@link Profession#getName()} of {@link Profession}
     *
     * @return the {@link Profession}
     *
     * @throws IllegalArgumentException if the name doesn't exist
     */
    Optional<Profession> getProfessionByName(String name);
}
