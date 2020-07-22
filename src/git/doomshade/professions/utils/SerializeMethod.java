package git.doomshade.professions.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks a method as the serialization method of a class. The method's parameters must be empty and the method should return {@code Map<String, Object>}.
 *
 * @author Doomshade
 * @version 1.0
 * @see git.doomshade.professions.profession.ITrainable
 * @see git.doomshade.professions.profession.ICraftable
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SerializeMethod {
}
