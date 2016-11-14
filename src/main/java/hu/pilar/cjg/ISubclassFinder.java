package hu.pilar.cjg;

import java.util.Set;

/**
 * Interface to find subclasses of a given class
 *
 * @author cserepj
 */
public interface ISubclassFinder {

    /**
     * Returns all the subclasses that extend the parameter parent class.
     *
     * @param parent
     * @return
     */
    Set<Class<?>> findClassesThatExtend(Class parent);
}
