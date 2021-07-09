package git.doomshade.professions.api;

import git.doomshade.professions.api.item.ItemType;
import git.doomshade.professions.api.item.ItemTypeHolder;

import java.util.Optional;

public interface IProfessionManager {

    /**
     * @param clazz the {@link ItemTypeHolder} class to look for
     * @param <A>   the {@link ItemTypeHolder}'s {@link ItemType}
     * @return instance of {@link ItemTypeHolder}
     */
    <A extends ItemType<?>> ItemTypeHolder<A> getItemTypeHolder(Class<A> clazz) throws IllegalArgumentException;

    /**
     * @param itemTypeHolder the {@link ItemTypeHolder} to register
     * @param <T>            the {@link ItemTypeHolder}
     */
    <T extends ItemTypeHolder<?>> void registerItemTypeHolder(T itemTypeHolder);

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
