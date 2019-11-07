package git.doomshade.professions.utils;

import com.avaje.ebean.validation.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class Utils {

    @NotNull
    public static <A> Set<A> findAllInIterable(Iterable<A> iterable, Predicate<A> condition) throws SearchNotFoundException {
        Set<A> set = new HashSet<>();
        for (A a : iterable) {
            if (condition.test(a)) {
                set.add(a);
            }
        }

        if (!set.isEmpty()) {
            return set;
        }
        throw new SearchNotFoundException();
    }

    public static <A> A findInIterable(Iterable<A> iterable, Predicate<A> condition) throws SearchNotFoundException {
        return findAllInIterable(iterable, condition).iterator().next();
    }

    public static class SearchNotFoundException extends Exception {

    }
}
