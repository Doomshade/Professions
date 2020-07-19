package git.doomshade.professions.profession.types;

import git.doomshade.professions.profession.professions.enchanting.EnchantingProfession;

/**
 * One of default Profession Types, used specifically in modifying (enchanting) items
 *
 * @author Doomshade
 * @see EnchantingProfession
 */
public interface IEnchanting extends IProfessionType {

    @Override
    default String getDefaultName() {
        return "&bEnchanting";
    }
}
