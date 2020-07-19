package git.doomshade.professions.profession.types;

import git.doomshade.professions.profession.professions.skinning.SkinningProfession;

/**
 * One of default Profession Types, specifically used in hunting professions
 *
 * @author Doomshade
 * @see SkinningProfession
 */
public interface IHunting extends IProfessionType {

    @Override
    default String getDefaultName() {
        return "&cHunting";
    }
}
