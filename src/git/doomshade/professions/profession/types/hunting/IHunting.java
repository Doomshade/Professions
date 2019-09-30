package git.doomshade.professions.profession.types.hunting;

import git.doomshade.professions.profession.types.IProfessionType;

public interface IHunting extends IProfessionType {

    @Override
    default String getDefaultName() {
        return "&cHunting";
    }
}
