package git.doomshade.professions.profession.types.gathering;

import git.doomshade.professions.profession.types.IProfessionType;

public interface IGathering extends IProfessionType {

    @Override
    default String getDefaultName() {
        return "&fCollecting";
    }
}
