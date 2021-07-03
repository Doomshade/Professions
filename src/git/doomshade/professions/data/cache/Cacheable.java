package git.doomshade.professions.data.cache;

import java.io.Serializable;
import java.util.Objects;

public abstract class Cacheable implements Serializable {

    public abstract Serializable getData();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cacheable spawn = (Cacheable) o;
        return spawn.getData().equals(getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getData());
    }
}
