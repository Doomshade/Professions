package git.doomshade.professions.profession.types.mining;

import git.doomshade.professions.profession.types.IProfessionType;

public interface IMining extends IProfessionType {

    @Override
    default String getDefaultName() {
        return "&9Mining";
    }
}
