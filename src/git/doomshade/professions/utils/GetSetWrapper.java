package git.doomshade.professions.utils;

public class GetSetWrapper<T> {

    public T t;

    public GetSetWrapper(T t) {
        this.t = t;
    }

    public GetSetWrapper() {
        this(null);
    }

}
