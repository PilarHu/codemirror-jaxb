package hu.pilar.cjg;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toSet;

/**
 * This class generates xml code completion hints from a set of JAXB annotated
 * classes. Can be used as a spring bean or standalone - should be used as a
 * singleton in any case.
 *
 * @author cserepj
 */
public class HintGenerator {

    private static final Set<String> BOOLEAN_VALUES = new LinkedHashSet<>();
    private static final Logger LOG = LoggerFactory.getLogger(HintGenerator.class);

    static {
        BOOLEAN_VALUES.add("true");
        BOOLEAN_VALUES.add("false");
    }

    /**
     * Jackson JSON mapper to be used to output the taginfo structure as json
     */
    private final ObjectMapper mapper;
    /**
     * Adds the ability to add sets of strings as selectable defaults to each
     * XML attribute. By default we add defaults for booleans and enums - this
     * factory can be used to add additional values for other types of fields.
     */
    private final IAttributeValueFactory valueSetFactory;
    /**
     * By default the library uses reflections.org to find all possible subtypes
     * for each class to handle polymorphic tags and abstract classes. If your
     * application is already using this library, it is possible to inject the
     * data set into this class and reuse the information.
     */
    private final ISubclassFinder subclassFinder;

    public HintGenerator(ObjectMapper mapper) {
        this(mapper, (IAttributeValueFactory) null);
    }

    public HintGenerator(ObjectMapper mapper, IAttributeValueFactory valueSetFactory) {
        this(mapper, valueSetFactory, new ReflectionBasedSubclassFinder());
    }

    public HintGenerator(ObjectMapper mapper, ISubclassFinder subclassFinder) {
        this.mapper = mapper;
        this.valueSetFactory = null;
        this.subclassFinder = subclassFinder;
    }

    public HintGenerator(ObjectMapper mapper, IAttributeValueFactory valueSetFactory, ISubclassFinder subclassFinder) {
        this.mapper = mapper;
        this.valueSetFactory = valueSetFactory;
        this.subclassFinder = subclassFinder;
    }

    private static Optional<String> getTagName(Class<?> c) {
        XmlRootElement xre = c.getAnnotation(XmlRootElement.class);
        LOG.debug("Checking class {}", c.getSimpleName());
        if (xre == null) {
            LOG.debug("Returning empty");
            return empty();
        }
        var tagName = "##default".equals(xre.name()) ?
            c.getSimpleName().substring(0, 1)
                .toLowerCase() + c.getSimpleName().substring(1)
            : xre.name();

        return of(tagName);
    }

    private static Optional<Type> findReturnType(Method m) {
        var returnType = m.getReturnType();
        if (!Collection.class.isAssignableFrom(returnType)) {
            return of(returnType);
        }
        Type c = m.getGenericReturnType();
        if (c instanceof ParameterizedType pt) {
            for (Type tv : pt.getActualTypeArguments()) {
                if (tv instanceof ParameterizedType tvpt) {
                    return of(tvpt.getRawType());
                } else {
                    return of(tv);
                }
            }
        }
        LOG.warn("Return type is collection but raw type is erased or not present for method: {}", m.getName());
        return empty();
    }

    public Optional<XmlHint> getHintsFor(Class<?> c) {
        HintGeneratorContext ctx = new HintGeneratorContext();
        var t = getTagInfo(c, ctx);
        if (t.getTag() == null) return empty();
        XmlHint hint = new XmlHint(mapper, t);
        for (TagInfo ti : ctx.byTag.values()) {
            if (ti.getTag() != null) {
                hint.addTag(ti);
            }
        }
        return of(hint);
    }

    private TagInfo getTagInfo(final Type type, final HintGeneratorContext ctx) {
        var c = (Class<?>) (type);
        if (ctx.byClass.containsKey(c)) {
            return ctx.byClass.get(c);
        }
        TagInfo t = getTagName(c).map(TagInfo::new).orElse(new TagInfo(null));
        ctx.byClass.put(c, t);
        ctx.byTag.put(t.getTag(), t);
        addAttributes(t, c);
        addOverrides(t, c, ctx);
        addChildren(t, c, ctx);
        return t;
    }

    private void addAttributes(final TagInfo t, final Class<?> c) {
        LOG.debug("Adding attributes for class {} for tag {}", c.getSimpleName(), t.getTag());
        for (Method m : c.getMethods()) {
            if (m.isAnnotationPresent(XmlAttribute.class)) {
                XmlAttribute attrs = m.getAnnotation(XmlAttribute.class);
                if ("##default".equals(attrs.name())) {
                    String n
                        = m.getName().startsWith("get")
                        ? m.getName().substring(3, 4).toLowerCase() + m.getName().substring(4)
                        : m.getName().substring(2, 3).toLowerCase() + m.getName().substring(3);
                    LOG.debug("    Found XmlAttribute annotation with name {}", n);
                    t.withAttribute(n, findValues(m, n));
                } else {
                    LOG.debug("    Found XmlAttribute annotation with name {}", attrs.name());
                    t.withAttribute(attrs.name(), findValues(m, attrs.name()));
                }
            }
        }
    }

    private void addChildren(final TagInfo t, final Type type, final HintGeneratorContext ctx) {
        if (type instanceof Class<?> c) {
            LOG.debug("Adding child nodes for class {} for tag {}", c.getSimpleName(), t.getTag());
            for (Method m : c.getMethods()) {
                if (m.isAnnotationPresent(XmlElementRef.class)) {
                    findReturnType(m)
                        .map(ch -> getTagInfo(ch, ctx))
                        .ifPresent(t::withChild);
                } else if (m.isAnnotationPresent(XmlElement.class)) {
                    XmlElement ref = m.getAnnotation(XmlElement.class);
                    findReturnType(m)
                        .map(rt -> getTagInfo(rt, ctx))
                        .ifPresent(rtti -> {
                            final var clone = new TagInfo(ref.name(), rtti);
                            t.withChild(clone);
                            ctx.byTag.put(clone.getTag(), clone);
                        });
                }
            }
        }
    }

    private Set<String> findValues(Method m, String name) {
        return findReturnType(m)
            .filter(Class.class::isInstance)
            .map(type -> {
                Class<?> cl = (Class<?>) type;
                if (Boolean.class.equals(cl) || boolean.class.equals(cl)) {
                    return BOOLEAN_VALUES;
                }
                if (Enum.class.isAssignableFrom(cl)) {
                    final var c = (Class<? extends Enum>) cl;
                    EnumSet<?> set = EnumSet.allOf(c);
                    return set.stream().
                        map(Enum::name).
                        collect(toSet());
                }
                if (this.valueSetFactory != null) {
                    return valueSetFactory.getValuesFor(name, cl);
                }
                return null;
            }).orElse(Set.of());
    }

    private <T> void addOverrides(TagInfo t, Class<T> c, HintGeneratorContext ctx) {
        final var set = subclassFinder.findClassesThatExtend(c);
        set.stream().map(HintGenerator::getTagName)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(t.getOverrides()::add);
        set.forEach(x -> getTagInfo(x, ctx));
    }

    /**
     * inner class to keep record of classes that were already processed.
     */
    private static class HintGeneratorContext {
        private final Map<Class<?>, TagInfo> byClass = new HashMap<>();
        private final Map<String, TagInfo> byTag = new HashMap<>();
    }

}
