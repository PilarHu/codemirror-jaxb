package hu.pilar.cjg;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class generates xml code completion hints from a set of JAXB annotated
 * classes. Can be used as a spring bean or standalone - should be used as a
 * singleton in any case.
 *
 *
 * @author cserepj
 */
public class HintGenerator {

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

    private static final Set<String> BOOLEAN_VALUES = new LinkedHashSet<>();

    static {
        BOOLEAN_VALUES.add("true");
        BOOLEAN_VALUES.add("false");
    }

    /**
     * inner class to keep record of classes that were already processed.
     */
    private static class HintGeneratorContext {

        private final Map<Class, TagInfo> byClass = new HashMap<>();
        private final Map<String, TagInfo> byTag = new HashMap<>();
    }

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

    public XmlHint getHintsFor(Class c) {
        HintGeneratorContext ctx = new HintGeneratorContext();
        TagInfo t = getTagInfo(c, ctx);
        if (t == null || t.getTag() == null) {
            return null;
        }
        XmlHint hint = new XmlHint(mapper, t);
        for (TagInfo ti : ctx.byTag.values()) {
            if (ti.getTag() != null) {
                hint.addTag(ti);
            }
        }
        return hint;
    }

    private TagInfo getTagInfo(Class c, HintGeneratorContext ctx) {
        if (ctx.byClass.containsKey(c)) {
            return ctx.byClass.get(c);
        }
        TagInfo t = new TagInfo(getTagName(c));
        ctx.byClass.put(c, t);
        ctx.byTag.put(t.getTag(), t);
        addAttributes(t, c);
        addOverrides(t, c, ctx);
        addChildren(t, c, ctx);
        return t;
    }

    private static String getTagName(Class c) {
        XmlRootElement xre = (XmlRootElement) c.getAnnotation(XmlRootElement.class);
        if (xre == null) {
            return null;
        }
        if ("##default".equals(xre.name())) {
            return c.getSimpleName().substring(0, 1).toLowerCase() + c.getSimpleName().substring(1);
        }
        return xre.name();
    }

    private TagInfo addAttributes(TagInfo t, Class c) {
        for (Method m : c.getMethods()) {
            if (m.isAnnotationPresent(XmlAttribute.class)) {
                XmlAttribute attrs = m.getAnnotation(XmlAttribute.class);
                if ("##default".equals(attrs.name())) {
                    String n
                            = m.getName().startsWith("get")
                            ? m.getName().substring(3, 4).toLowerCase() + m.getName().substring(4)
                            : m.getName().substring(2, 3).toLowerCase() + m.getName().substring(3);
                    t.withAttribute(n, findValues(m, n));
                } else {
                    t.withAttribute(attrs.name(), findValues(m, attrs.name()));
                }
            }
        }
        return t;
    }

    private TagInfo addChildren(TagInfo t, Class c, HintGeneratorContext ctx) {
        for (Method m : c.getMethods()) {
            if (m.isAnnotationPresent(XmlElementRef.class)) {
                Class ch = findReturnType(m);
                TagInfo ti = getTagInfo(ch, ctx);
                t.withChild(ti);
            } else if (m.isAnnotationPresent(XmlElement.class)) {
                XmlElement ref = m.getAnnotation(XmlElement.class);
                Class rt = findReturnType(m);
                TagInfo temp = getTagInfo(rt, ctx);
                temp = new TagInfo(ref.name(), temp);
                t.withChild(temp);
                ctx.byTag.put(temp.getTag(), temp);
            }
        }
        return t;
    }

    /**
     * Returns the method return type if it's not a collection If it is a
     * collection, then the generic type of the collection is returned
     *
     * @param m
     * @return
     */
    private static Class<?> findReturnType(Method m) {
        Class r = m.getReturnType();
        Type c = m.getGenericReturnType();
        if (c instanceof ParameterizedType && Collection.class.isAssignableFrom(r)) {
            for (Type tv : ((ParameterizedType) c).getActualTypeArguments()) {
                if (tv instanceof ParameterizedType) {
                    return (Class) ((ParameterizedType) tv).getRawType();
                } else {
                    return (Class) tv;
                }
            }
        }
        return r;
    }

    private Set<String> findValues(Method m, String name) {
        Class<?> type = findReturnType(m);
        if (Boolean.class.equals(type) || boolean.class.equals(type)) {
            return BOOLEAN_VALUES;
        }
        if (Enum.class.isAssignableFrom(type)) {
            Class<? extends Enum> c = (Class<? extends Enum>) type;
            EnumSet<? extends Enum> set = EnumSet.allOf(c);
            return set.stream().
                    map(Enum::name).
                    collect(Collectors.toSet());
        }

        if (this.valueSetFactory != null) {
            return valueSetFactory.getValuesFor(name, type);
        }
        return null;
    }

    /**
     * Finds and adds to the TagInfo objects all subclasses of class c that look
     * like JAXB classes implementing XML elements that may be used instead of
     * class c in a hierarchy
     *
     * @param t
     * @param c
     * @param ctx
     */
    private void addOverrides(TagInfo t, Class c, HintGeneratorContext ctx) {
        Set<Class<?>> set = subclassFinder.findClassesThatExtend(c);
        set.stream().map(HintGenerator::getTagName).
                filter(Objects::nonNull).
                forEach(t.getOverrides()::add);
        set.forEach(x -> getTagInfo(x, ctx));
    }

}
