package git.doomshade.professions.profession.types.crafting;

import git.doomshade.professions.profession.types.IProfessionType;

public interface ICrafting extends IProfessionType {

    @Override
    default String getDefaultName() {
        return "&bCrafting";
    }

}
