package hu.pilar.cjg;

import java.util.Set;

/**
 * Interface to find subclasses of a given class
 *
 * @author cserepj
 */
@FunctionalInterface
public interface ISubclassFinder {

    /**
     * Returns all the subclasses that extend the parameter parent class.
     */
    <T> Set<Class<? extends T>> findClassesThatExtend(Class<T> parent);
}
