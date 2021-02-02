package git.doomshade.professions.data.cache;

import java.io.Serializable;
import java.util.Objects;

public abstract class Cacheable implements Serializable {

    public abstract Serializable getId();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cacheable spawn = (Cacheable) o;
        return spawn.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
