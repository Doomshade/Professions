package git.doomshade.professions.profession.types.gathering;

import git.doomshade.professions.profession.types.IProfessionType;

/**
 * One of default Profession Types, specifically used for gathering professions
 *
 * @author Doomshade
 * @see git.doomshade.professions.profession.professions.HerbalismProfession
 */
public interface IGathering extends IProfessionType {

    @Override
    default String getDefaultName() {
        return "&fCollecting";
    }
}
