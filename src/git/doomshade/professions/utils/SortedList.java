/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Jakub Šmrha
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package git.doomshade.professions.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

/**
 * Sorts {@link ArrayList} based on given comparator
 *
 * @param <E> the type
 *
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 * @deprecated not used
 */
@Deprecated
public final class SortedList<E> extends ArrayList<E> {
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
