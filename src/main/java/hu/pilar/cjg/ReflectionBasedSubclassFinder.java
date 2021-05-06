package hu.pilar.cjg;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cserepj
 */
public class ReflectionBasedSubclassFinder implements ISubclassFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionBasedSubclassFinder.class);
    private final Reflections reflections;

    ReflectionBasedSubclassFinder() {
        LOGGER.info("Setting up reflection cache");
        Collection<URL> urls = new ArrayList<>();
        urls.addAll(ClasspathHelper.forJavaClassPath());
        urls.addAll(ClasspathHelper.forClassLoader());
        reflections = new Reflections(new ConfigurationBuilder().
                setUrls(urls.stream().filter(x -> x != null).collect(Collectors.toList())).
                filterInputsBy(new FilterBuilder.Include(
                        FilterBuilder.prefix("")))
                .setScanners(new SubTypesScanner()));
    }

    public ReflectionBasedSubclassFinder(Reflections reflections) {
        this.reflections = reflections;
    }

    @Override
    public Set<Class<?>> findClassesThatExtend(Class parent) {
        return reflections.getSubTypesOf(parent);
    }

}
