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

package git.doomshade.professions.cache;

import java.io.Serializable;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public interface Cacheable {

    /**
     * Extends the cache and copies the contents of the parent cache
     *
     * @param prev the parent cache
     * @param size the new size
     *
     * @return the extended cache
     */
    static Serializable[] prepareCache(Serializable[] prev, int size) {
        final Serializable[] cache = new Serializable[size];
        System.arraycopy(prev, 0, cache, 0, prev.length);
        return cache;
    }

    /**
     * Loads the data from a cache
     *
     * @param data the cache
     */
    void loadCache(Serializable[] data);

    /**
     * No data should be changed with each subsequent call of this method
     *
     * @return the data to be cached
     */
    Serializable[] cache();

    /**
     * The offset should always be equal to the parent offset ({@code super.getOffset()}) plus the current offset
     *
     * @return the offset in data for further caching
     */
    int getOffset();

    String getFileName();
}
