package git.doomshade.professions.profession.types;

import git.doomshade.professions.Profession;
import git.doomshade.professions.event.ProfessionEvent;
import org.bukkit.event.Listener;

/**
 * This is an interface for profession types. This serves as a generic argument for {@link Profession}.
 *
 * @author Doomshade
 * @see ICrafting
 * @see IEnchanting
 * @see IGathering
 * @see IHunting
 * @see IMining
 */
public interface IProfessionType extends Listener {

    /**
     * @return the default name of this profession type (pretty much not needed)
     */
    String getDefaultName();

    <T extends ItemType<?>> void onEvent(ProfessionEvent<T> e);

}
