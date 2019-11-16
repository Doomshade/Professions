package git.doomshade.professions.profession.types;

import org.bukkit.event.Listener;

/**
 * This is an interface for profession types. This serves as a generic argument for {@link git.doomshade.professions.Profession}.
 *
 * @author Doomshade
 * @see git.doomshade.professions.profession.types.crafting.ICrafting
 * @see git.doomshade.professions.profession.types.enchanting.IEnchanting
 * @see git.doomshade.professions.profession.types.gathering.IGathering
 * @see git.doomshade.professions.profession.types.hunting.IHunting
 * @see git.doomshade.professions.profession.types.mining.IMining
 */
public interface IProfessionType extends Listener, IProfessionEventable {

    /**
     * @return the default name of this profession type (pretty much not needed)
     */
    String getDefaultName();

}
