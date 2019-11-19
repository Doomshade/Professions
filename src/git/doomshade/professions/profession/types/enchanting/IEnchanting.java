package git.doomshade.professions.profession.types.enchanting;

import git.doomshade.professions.profession.types.IProfessionType;

/**
 * One of default Profession Types, used specifically in modifying (enchanting) items
 *
 * @author Doomshade
 * @see git.doomshade.professions.profession.professions.EnchantingProfession
 */
public interface IEnchanting extends IProfessionType {

    @Override
    default String getDefaultName() {
        return "&bEnchanting";
    }
}
