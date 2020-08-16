package git.doomshade.professions.profession.types;

import git.doomshade.professions.profession.ICustomTypeNew;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Deprecated
public @interface SerializeAdditionalType {

    @Deprecated
    Class<? extends ICustomTypeNew<?>>[] value();
}
