package git.doomshade.professions.profession.spawn;

import git.doomshade.professions.api.spawn.IElement;

public abstract class Element implements IElement {

    private final String id;

    public Element(String id) {
        this.id = id;
    }

    @Override
    public final String getId() {
        return id;
    }
}
