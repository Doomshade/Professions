package git.doomshade.professions.data.cache;

import git.doomshade.professions.Professions;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class CacheUtils {

    private static final HashMap<String, ObjectOutputStream> CACHE = new HashMap<>();

    private CacheUtils() {
    }

    public static <T extends Cacheable> void cache(T data, String fileName, String folder) throws IOException {
        cache(Collections.singleton(data), fileName, folder);
    }

    public static <T extends Cacheable> void cache(Collection<T> data, String fileName, String folder) throws IOException {
        File dir = new File(Professions.getInstance().getCacheFolder() + File.separator + folder);

        boolean append = true;
        if (!dir.isDirectory()) {
            dir.mkdirs();
            append = false;
        }

        File file = new File(dir, fileName);
        if (!file.exists()) {
            file.createNewFile();
            append = false;
        }

        if (append)
            appendCache(data, file);
        else
            setupCache(data, file);
    }

    private static <T extends Cacheable> void appendCache(Collection<T> data, File file) throws IOException {
        ObjectOutputStream oos = CACHE.get(file.getAbsolutePath());
        for (T d : data)
            oos.writeObject(d);
        oos.flush();
    }

    private static <T extends Cacheable> void setupCache(Collection<T> data, File file) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file, true));
        for (T d : data)
            oos.writeObject(d);
        oos.flush();
        CACHE.put(file.getAbsolutePath(), oos);
    }

    @SuppressWarnings("all")
    public static <T extends Cacheable> Collection<T> readCache(String fileName, String folder) throws IOException {
        Collection<T> coll = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(Professions.getInstance().getCacheFolder() + File.separator + folder, fileName)))) {
            try {
                for (; ; ) {
                    coll.add((T) ois.readObject());
                }
            } catch (EOFException ignored) {
            } catch (Exception e) {
                Professions.logError(e);
            }
        }
        return coll;
    }

    public static void clearCache(String folder) {
        File files = new File(Professions.getInstance().getCacheFolder(), folder);

        if (files.isDirectory()) {
            for (File f : files.listFiles()) {
                ObjectOutputStream oos = CACHE.get(f.getAbsolutePath());
                if (oos != null) {
                    try {
                        oos.close();
                    } catch (IOException e) {
                        Professions.logError(e);
                    }
                }
                f.delete();
            }
        }
    }

    public static void clearCache(String fileName, String folder) {
        File f = new File(Professions.getInstance().getCacheFolder() + File.separator + folder, fileName);

        if (f.exists()) {
            ObjectOutputStream oos = CACHE.get(f.getAbsolutePath());
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    Professions.logError(e);
                }
            }
            f.delete();
        }
    }

    public static void closeCacheFiles() {
        CACHE.forEach((x, y) -> {
            try {
                y.flush();
                y.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
