package git.doomshade.professions.profession.types;

import git.doomshade.professions.profession.professions.mining.MiningProfession;

/**
 * One of default Profession Types, specifically used in mining professions
 *
 * @author Doomshade
 * @see MiningProfession
 */
public interface IMining extends IProfessionType {

    @Override
    default String getDefaultName() {
        return "&9Mining";
    }
}
