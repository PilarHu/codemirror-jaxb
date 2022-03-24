package hu.pilar.cjg;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.reflections.util.ClasspathHelper.forClassLoader;
import static org.reflections.util.ClasspathHelper.forJavaClassPath;

public class ReflectionBasedSubclassFinder implements ISubclassFinder {

    private static final Logger LOG = LoggerFactory.getLogger(ReflectionBasedSubclassFinder.class);
    private final Reflections reflections;

    ReflectionBasedSubclassFinder() {
        LOG.info("Setting up reflection cache");
        final var urls = new ArrayList<URL>();
        urls.addAll(forJavaClassPath());
        urls.addAll(forClassLoader());
        reflections = new Reflections(new ConfigurationBuilder().
            setUrls(urls.stream().filter(Objects::nonNull)
                .collect(toList())).
            filterInputsBy(new FilterBuilder()
                .includePattern(".*"))
            .setScanners(Scanners.SubTypes));
    }

    public ReflectionBasedSubclassFinder(Reflections reflections) {
        this.reflections = reflections;
    }

    @Override
    public <T> Set<Class<? extends T>> findClassesThatExtend(Class<T> parent) {
        LOG.debug("Looking up subtypes for {}", parent.getSimpleName());
        return reflections.getSubTypesOf(parent);
    }

}
