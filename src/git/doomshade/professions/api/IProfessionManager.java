package git.doomshade.professions.api;

import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.api.item.ItemTypeHolder;
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
     * @throws IOException if the {@link ItemTypeHolder} couldn't be registered
     */
    default <T, IType extends ItemType<T>> void registerItemTypeHolder(Class<IType> itemType, Supplier<T> o) throws IOException {
        registerItemTypeHolder(itemType, o, null);
    }

    /**
     * @param itemType          the {@link ItemType} class
     * @param o                 the supplier of an example object of {@link ItemType} argument
     * @param additionalCommand the additional commands to be made before registration to file
     * @param <T>               the {@link ItemType} argument
     * @param <IType>           the {@link ItemType}
     * @throws IOException if the {@link ItemTypeHolder} couldn't be registered to file
     */
    default <T, IType extends ItemType<T>> void registerItemTypeHolder(Class<IType> itemType, Supplier<T> o, Consumer<IType> additionalCommand) throws IOException {
        registerItemTypeHolder(itemType, o.get(), additionalCommand);

    }

    /**
     * @param itemType the {@link ItemType} class
     * @param o        the example object of {@link ItemType} argument
     * @param <T>      the {@link ItemType} argument
     * @param <IType>  the {@link ItemType}
     * @throws IOException if the {@link ItemTypeHolder} couldn't be registered
     */
    default <T, IType extends ItemType<T>> void registerItemTypeHolder(Class<IType> itemType, T o) throws IOException {
        registerItemTypeHolder(itemType, o, null);
    }

    /**
     * @param itemType          the {@link ItemType} class
     * @param o                 the example object of {@link ItemType} argument
     * @param additionalCommand the additional commands to be made before registration to file
     * @param <T>               the {@link ItemType} argument
     * @param <IType>           the {@link ItemType}
     * @throws IOException if the {@link ItemTypeHolder} couldn't be registered to file
     */
    <T, IType extends ItemType<T>> void registerItemTypeHolder(Class<IType> itemType, T o, Consumer<IType> additionalCommand) throws IOException;

    /**
     * Registers a profession<br>
     * Make sure you only create a single instance of the profession, multiple instances are disallowed and will throw an exception
     *
     * @param prof the profession to register
     */
    void registerProfession(Profession prof);

    /**
     * @param clazz the {@link ItemTypeHolder} class to look for
     * @param <A>   the {@link ItemTypeHolder}'s {@link ItemType}
     * @return instance of {@link ItemTypeHolder}
     */
    <T, A extends ItemType<T>> ItemTypeHolder<T, A> getItemTypeHolder(Class<A> clazz) throws IllegalArgumentException;

    /**
     * @param id the{@link Profession#getID()} of {@link Profession}
     * @return the {@link Profession}
     * @throws IllegalArgumentException if the id doesn't exist
     */
    Optional<Profession> getProfessionById(String id);

    /**
     * @param profession the {@link Profession} class
     * @return the {@link Profession} if found
     * @throws RuntimeException if the profession is not registered
     */
    Optional<Profession> getProfession(Class<? extends Profession> profession);

    /**
     * @param name the {@link Profession#getName()} of {@link Profession}
     * @return the {@link Profession}
     * @throws IllegalArgumentException if the name doesn't exist
     */
    Optional<Profession> getProfessionByName(String name);

    /**
     * Calls {@link #getProfessionById(String)} with the {@link ItemStack}'s display name
     *
     * @param item the item to look for the profession from
     * @return the profession
     * @see #getProfessionById(String)
     */
    default Optional<Profession> getProfession(ItemStack item) {
        if (item == null || item.getItemMeta() == null || !item.getItemMeta().hasDisplayName()) {
            return Optional.empty();
        }
        return getProfessionById(item.getItemMeta().getDisplayName());
    }

    /**
     * @param idOrName the id or name of the profession
     * @return the profession based on either id or name in this order
     */
    default Optional<Profession> getProfession(String idOrName) {
        final Optional<Profession> id = getProfessionById(idOrName);
        if (id.isPresent()) {
            return id;
        }
        return getProfessionByName(idOrName);
    }
}
