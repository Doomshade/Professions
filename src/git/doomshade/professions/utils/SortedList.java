package git.doomshade.professions.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

/**
 * Sorts {@link ArrayList} based on given comparator
 *
 * @param <E> the type
 * @author Doomshade
 */
public class SortedList<E> extends ArrayList<E> {
    private transient final Comparator<? super E> c;

    /**
     * @param c the comparator to sort by
     */
    public SortedList(Comparator<? super E> c) {
        super();
        this.c = c;
    }

    @Override
    public E set(int index, E element) {
        E e = super.set(index, element);
        sort();
        return e;
    }

    @Override
    public boolean add(E e) {
        boolean add = super.add(e);
        sort();
        return add;
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        sort();
    }

    @Override
    public E remove(int index) {
        E e = super.remove(index);
        sort();
        return e;
    }

    @Override
    public boolean remove(Object o) {
        boolean remove = super.remove(o);
        sort();
        return remove;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean addAll = super.addAll(c);
        sort();
        return addAll;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        boolean addAll = super.addAll(index, c);
        sort();
        return addAll;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean removeAll = super.removeAll(c);
        sort();
        return removeAll;
    }

    /**
     * @deprecated calls {@link #sort()} instead
     */
    @Deprecated
    @Override
    public void sort(Comparator<? super E> c) {
        sort();
    }

    /**
     * Sorts based on passed {@link Comparator}
     */
    public void sort() {
        super.sort(c);
    }


}
