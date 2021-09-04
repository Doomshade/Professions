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
 * Cache that allows both read and write from a cache file
 *
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
     * @throws IOException              if it could not be flushed
     * @throws IllegalArgumentException if the cache contains null objects
     */
    public void save(Cacheable[] data) throws IOException, IllegalArgumentException {
        validate(data);

        // write the header
        writeHeader(data);

        // write the data
        writeData(data);

        // close the file
        close();
    }

    private void close() throws IOException {
        out.flush();
        out.close();
    }

    /**
     * Writes the objects to the output stream
     *
     * @param data the object data
     *
     * @throws IOException if something occurred during writing to the file
     */
    private void writeData(Cacheable[] data) throws IOException {
        for (Cacheable c : data) {
            for (Serializable s : c.cache()) {
                out.writeObject(s);
            }
        }
    }

    /**
     * Writes the header of the file to the output stream
     *
     * @param data the data to get the header from
     *
     * @throws IOException if something occurred during writing to the file
     */
    private void writeHeader(Cacheable[] data) throws IOException {
        // the amount of cacheables
        // for example 25 herbs
        out.writeInt(data.length);
        for (Cacheable c : data) {
            final Serializable[] cache = c.cache();

            // the inner array amount of each data
            // for example looped 25 times and saves each herb's amount of spawn points
            out.writeInt(cache.length);
        }
    }

    /**
     * Validates the cacheable array
     *
     * @param cache the cacheable array
     *
     * @throws IllegalArgumentException if some object is null
     */
    private void validate(Cacheable[] cache) throws NullPointerException {
        if (cache == null) {
            throw new IllegalArgumentException("Cache cannot be null!");
        }

        for (int i = 0; i < cache.length; i++) {
            final Cacheable c = cache[i];
            if (c == null) {
                throw new IllegalArgumentException(
                        String.format("A cacheable of the cache array at index %d is null!", i));
            }

            final Serializable[] ss = c.cache();
            if (ss == null) {
                throw new IllegalArgumentException(
                        String.format("An array of the cacheable object at index %d is null!",
                                i));
            }

            for (int j = 0; j < ss.length; j++) {
                final Serializable s = ss[j];
                if (s == null) {
                    throw new IllegalArgumentException(String.format("An object at index %d in cacheable %d is null!"
                            , j, i));
                }
            }
        }
    }

}
