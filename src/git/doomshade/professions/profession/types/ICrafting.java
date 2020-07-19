package git.doomshade.professions.profession.types;

public interface ICrafting extends IProfessionType {

    @Override
    default String getDefaultName() {
        return "&bCrafting";
    }

}
