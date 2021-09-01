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

package git.doomshade.professions.data.cache;

import git.doomshade.professions.io.IOManager;
import git.doomshade.professions.io.ProfessionLogger;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Doomshade
 * @version 1.0
 * @since 1.0
 */
public class CacheUtils {

    private CacheUtils() {
    }

    /**
     * Caches single data to a file with given folder.
     *
     * @param data     the data to cache
     * @param fileName the file name
     * @param folder   the folder
     * @param <T>      the data type
     *
     * @throws IOException if cache was unsuccessful
     */
    public static <T extends Cacheable> void cache(T data, String fileName, String folder) throws IOException {
        cache(Collections.singleton(data), fileName, folder);
    }

    /**
     * Caches data to a file with given folder.
     *
     * @param data     the data to cache
     * @param fileName the file name
     * @param folder   the folder
     * @param <T>      the data type
     *
     * @throws IOException if cache was unsuccessful
     */
    public static <T extends Cacheable> void cache(Collection<T> data, String fileName, String folder)
            throws IOException {
        File dir = new File(IOManager.getCacheFolder() + File.separator + folder);

        // create folder and file if they don't exist
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }

        File file = new File(dir, fileName);
        if (!file.exists()) {
            file.createNewFile();
        }

        setupCache(data, file);
    }

    /**
     * Sets up cache file, writing the header first and then the data
     *
     * @param data the data to cache
     * @param file the file
     * @param <T>  the data type
     *
     * @throws IOException if setting up was unsuccessful
     */
    private static <T extends Cacheable> void setupCache(Collection<T> data, File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file, true))) {
            // save time, when this cache was created
            oos.writeObject(new CacheHeader());

            // then write data
            for (T d : data) {
                oos.writeObject(d);
            }
        }
    }

    /**
     * Reads the cache. The first index of this collection is
     *
     * @param fileName the file name
     * @param folder   the file folder
     * @param <T>      the data type
     *
     * @return the collection of deserialized cacheable data
     *
     * @throws IOException if deserialization was unsuccessful
     */
    @SuppressWarnings("all")
    public static <T extends Cacheable> Collection<T> readCache(String fileName, String folder) throws IOException {
        Collection<T> coll = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(new File(IOManager.getCacheFolder() + File.separator + folder, fileName)))) {
            try {
                for (; ; ) {
                    coll.add((T) ois.readObject());
                }
            } catch (EOFException ignored) {
            } catch (Exception e) {
                ProfessionLogger.logError(e);
            }
        }
        return coll;
    }

    /**
     * Clears the cache folder
     *
     * @param folder the folder to clear
     */
    public static void clearCache(String folder) {

        // ignore empty folder, this would delete the whole cache folder
        if (folder.isEmpty()) {
            return;
        }


        File files = new File(IOManager.getCacheFolder(), folder);

        if (files.isDirectory()) {
            for (File f : files.listFiles()) {
                f.delete();
            }
        }
    }

    public static void clearCache(String fileName, String folder) {

        // ignore empty folder/name
        if (folder.isEmpty() || fileName.isEmpty()) {
            return;
        }
        File f = new File(IOManager.getCacheFolder() + File.separator + folder, fileName);

        if (f.exists()) {
            f.delete();
        }
    }
}
