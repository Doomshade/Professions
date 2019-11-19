package git.doomshade.professions.profession.types.mining;

import git.doomshade.professions.profession.types.IProfessionType;

/**
 * One of default Profession Types, specifically used in mining professions
 *
 * @author Doomshade
 * @see git.doomshade.professions.profession.professions.MiningProfession
 */
public interface IMining extends IProfessionType {

    @Override
    default String getDefaultName() {
        return "&9Mining";
    }
}
