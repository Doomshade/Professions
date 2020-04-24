package git.doomshade.professions.event;

import git.doomshade.professions.profession.types.ItemType;

public class ProfessionEventWrapper<T extends ItemType<?>> {
    public final ProfessionEvent<T> event;

    public ProfessionEventWrapper(ProfessionEvent<T> event) {
        this.event = event;
    }
}
