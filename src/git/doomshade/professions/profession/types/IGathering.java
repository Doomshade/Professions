package git.doomshade.professions.profession.types;

import git.doomshade.professions.profession.professions.herbalism.HerbalismProfession;

/**
 * One of default Profession Types, specifically used for gathering professions
 *
 * @author Doomshade
 * @see HerbalismProfession
 */
public interface IGathering extends IProfessionType {

    @Override
    default String getDefaultName() {
        return "&fCollecting";
    }
}
