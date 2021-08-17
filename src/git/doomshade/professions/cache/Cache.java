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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class Cache extends ReadOnlyCache {

    public Cache(File file) throws IOException {
        super(file);
    }

    /**
     * Saves the data, flushes the bytes and closes the file
     *
     * @param data the data
     *
     * @throws IOException          if it could not be flushed
     * @throws NullPointerException if the cache contains null objects
     */
    public void save(Cacheable[] data) throws IOException, NullPointerException {
        validate(data);
        out.writeInt(data.length);
        out.writeInt(data[0].cache().length);
        for (Cacheable c : data) {
            for (Serializable s : c.cache()) {
                out.writeObject(s);
            }
        }
        out.flush();
        out.close();
    }

    /**
     * Validates the cacheable array
     *
     * @param cache the cacheable array
     *
     * @throws NullPointerException if some object is null
     */
    private void validate(Cacheable[] cache) throws NullPointerException {
        if (cache == null) {
            throw new NullPointerException("Cache cannot be null!");
        }

        for (int i = 0; i < cache.length; i++) {
            final Cacheable c = cache[i];
            if (c == null) {
                throw new NullPointerException(String.format("A cacheable of the cache array at index %d is null!", i));
            }

            final Serializable[] ss = c.cache();
            if (ss == null) {
                throw new NullPointerException(String.format("An array of the cacheable object at index %d is null!",
                        i));
            }

            for (int j = 0; j < ss.length; j++) {
                final Serializable s = ss[j];
                if (s == null) {
                    throw new NullPointerException(String.format("An object at index %d in cacheable %d is null!"
                            , j, i));
                }
            }
        }
    }

}
