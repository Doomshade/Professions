package git.doomshade.professions.utils;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;

public final class SerializationRegistry {
    private static boolean inited = false;

    public static void init() {
        if (inited) {
            return;
        }
        inited = true;
        for (Package p : Package.getPackages()) {
            getClasses(p.getName())
                    .stream()
                    .filter(x -> x != null && x.isAssignableFrom(ConfigurationSerializable.class))
                    .forEach(x -> register((Class<? extends ConfigurationSerializable>) x));
        }
    }

    private static Set<Class<?>> getClasses(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .collect(Collectors.toSet());
    }

    private static Class<?> getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            // handle the exception
        }
        return null;
    }

    private static <T extends ConfigurationSerializable> void register(Class<T> clazz) {
        ConfigurationSerialization.registerClass(clazz);
    }


}
