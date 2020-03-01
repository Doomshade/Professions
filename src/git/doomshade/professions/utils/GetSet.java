package git.doomshade.professions.utils;

public class GetSet<T> {

    private T t;

    public GetSet(T t) {
        this.t = t;
    }

    public void set(T t) {
        this.t = t;
    }

    public T get() {
        return t;
    }
}
