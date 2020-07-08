package git.doomshade.professions.utils;

import git.doomshade.professions.profession.ICustomType;
import git.doomshade.professions.profession.types.ItemType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for custom deserialization methods in {@link ItemType}
 *
 * @author Doomshade
 * @version 1.0
 * @see ICustomType ICustomType extensions
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DeserializeMethod {
}
