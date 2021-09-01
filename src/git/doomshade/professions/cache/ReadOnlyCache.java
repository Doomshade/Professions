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

import java.io.*;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class ReadOnlyCache {
    protected File file;
    protected ObjectOutputStream out;

    public ReadOnlyCache(File file) throws IOException {
        this.file = file;
        this.out = new ObjectOutputStream(new FileOutputStream(file));
    }

    /**
     * Reads the bytes from cached file
     *
     * @return the bytes from the file
     *
     * @throws IOException if the file does not exist, or it could not be read
     */
    public Serializable[][] load() throws IOException {
        Serializable[][] arr = new Serializable[0][];

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            final int[] header = readHeader(in);
            arr = new Serializable[header.length][];

            for (int i = 0; i < header.length; i++) {
                final int size = header[i];
                try {
                    arr[i] = readObjects(in, size);
                } catch (ClassNotFoundException e) {
                    throw new IOException("Could not read file!", e);
                }
            }
        }
        return arr;
    }

    /**
     * Reads the header from the input stream
     *
     * @param in the input stream
     *
     * @return the header
     *
     * @throws IOException if the data could not be read
     */
    private int[] readHeader(ObjectInputStream in) throws IOException {
        int cacheLen = in.readInt();
        int[] header = new int[cacheLen];
        for (int i = 0; i < cacheLen; i++) {
            header[i] = in.readInt();
        }
        return header;
    }

    /**
     * Reads the objects from the input stream
     *
     * @param in   the input stream
     * @param size the object amount
     *
     * @return the objects
     *
     * @throws IOException if the data could not be read
     */
    private Serializable[] readObjects(ObjectInputStream in, int size) throws IOException, ClassNotFoundException {
        Serializable[] objects = new Serializable[size];
        for (int j = 0; j < size; j++) {
            objects[j] = (Serializable) in.readObject();
        }
        return objects;
    }
}
