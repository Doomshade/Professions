package git.doomshade.professions.profession.types.hunting;

import git.doomshade.professions.profession.types.IProfessionType;

/**
 * One of default Profession Types, specifically used in hunting professions
 *
 * @author Doomshade
 * @see git.doomshade.professions.profession.professions.SkinningProfession
 */
public interface IHunting extends IProfessionType {

    @Override
    default String getDefaultName() {
        return "&cHunting";
    }
}
