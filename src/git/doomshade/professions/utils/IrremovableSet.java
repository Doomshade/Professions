package git.doomshade.professions.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

/**
 * This hashset ensures that the elements can be only added and/or read, not removed
 *
 * @param <E> the element type
 */
public class IrremovableSet<E> extends HashSet<E> {
    public IrremovableSet() {
        super();
    }

    public IrremovableSet(Collection<? extends E> c) {
        super(c);
    }

    public IrremovableSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public IrremovableSet(int initialCapacity) {
        super(initialCapacity);
    }

    @Deprecated
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public void clear() {
        throw new UnsupportedOperationException();

    }

    @Deprecated
    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
}
