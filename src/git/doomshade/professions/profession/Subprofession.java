package git.doomshade.professions.profession;

public abstract class Subprofession extends Profession {

    /**
     * Not needed for now
     *
     * @return
     */
    public abstract Class<? extends Profession> getMainprofession();
}
