package git.doomshade.professions.profession.types.enchanting;

import git.doomshade.professions.profession.types.IProfessionType;

public interface IEnchanting extends IProfessionType {

    @Override
    default String getDefaultName() {
        // TODO Auto-generated method stub
        return "&bEnchanting";
    }
}
