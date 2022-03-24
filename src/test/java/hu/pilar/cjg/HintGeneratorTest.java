/*
 *    Copyright (c) 2006-2014 PILAR Kft. All Rights Reserved.
 *
 *   This software is the confidential and proprietary information of
 *   PILAR Kft. ("Confidential Information").
 *   You shall not disclose such Confidential Information and shall use it only in
 *   accordance with the terms of the license agreement you entered into
 *   with PILAR Kft.
 *
 *   PILAR MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 *   THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 *   TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 *   PARTICULAR PURPOSE, OR NON-INFRINGEMENT. PILAR SHALL NOT BE LIABLE FOR
 *   ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 *   DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.tor.
 */
package hu.pilar.cjg;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.annotation.*;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class HintGeneratorTest {

    @Test
    void testNonJaxb() {
        HintGenerator hg = new HintGenerator(new ObjectMapper());
        var hint = hg.getHintsFor(Object.class);
        assertFalse(hint.isPresent());
    }

    @Test
    void testConstructorWithSubClassFinder() {
        HintGenerator hg = new HintGenerator(new ObjectMapper(), new ISubclassFinder() {
            @Override
            public <T> Set<Class<? extends T>> findClassesThatExtend(final Class<T> parent) {
                return Set.of();
            }
        });
        var hint = hg.getHintsFor(DefaultNameTestClass.class);
        assertThat(hint.isPresent(), is(true));
        assertThat(hint.get().toJson(), equalTo(
            "var tags = {\"!top\":[\"defaultNameTestClass\"],\"!attrs\":{},\"defaultNameTestClass\":{\"attrs\":{},\"children\":[\"nestedParameterized\"]},\"nestedParameterized\":{\"attrs\":{},\"children\":[]}};"));
    }


    @Test
    void test() {
        HintGenerator hg = new HintGenerator(new ObjectMapper());

        XmlHint hint = hg.getHintsFor(TestClass.class).orElse(null);
        assertNotNull(hint);
        assertThat(hint.toJson(), equalTo("var tags = {"
            + "\"!top\":[\"C\"],"
            + "\"!attrs\":{},"
            + "\"A\":{\"attrs\":{\"nextValue\":[\"ONE\",\"TWO\",\"THREE\"],\"value\":[\"ONE\",\"TWO\",\"THREE\"]},\"children\":[]},"
            + "\"C\":{\"attrs\":{\"boole\":[\"true\",\"false\"],\"boole2\":[\"true\",\"false\"],\"value3\":null},\"children\":[\"A\",\"D\",\"b\"]},"
            + "\"D\":{\"attrs\":{\"nextValue\":[\"ONE\",\"TWO\",\"THREE\"],\"value\":[\"ONE\",\"TWO\",\"THREE\"]},\"children\":[]},"
            + "\"b\":{\"attrs\":{\"nextValue\":[\"ONE\",\"TWO\",\"THREE\"],\"value\":[\"ONE\",\"TWO\",\"THREE\"]},\"children\":[]}"
            + "};"));

        hg = new HintGenerator(new ObjectMapper(), (attributeName, type) -> {
            if ("value3".equals(attributeName)) {
                Set<String> s = new LinkedHashSet<>();
                s.add("1979");
                s.add("1980");
                s.add("1981");
                return s;
            }
            return null;
        });
        hint = hg.getHintsFor(TestClass.class).orElse(null);
        assertNotNull(hint);
        assertEquals("var tags = {"
                + "\"!top\":[\"C\"],"
                + "\"!attrs\":{},"
                + "\"A\":{\"attrs\":{\"nextValue\":[\"ONE\",\"TWO\",\"THREE\"],\"value\":[\"ONE\",\"TWO\",\"THREE\"]},\"children\":[]},"
                + "\"C\":{\"attrs\":{\"boole\":[\"true\",\"false\"],\"boole2\":[\"true\",\"false\"],\"value3\":[\"1979\",\"1980\",\"1981\"]},\"children\":[\"A\",\"D\",\"b\"]},"
                + "\"D\":{\"attrs\":{\"nextValue\":[\"ONE\",\"TWO\",\"THREE\"],\"value\":[\"ONE\",\"TWO\",\"THREE\"]},\"children\":[]},"
                + "\"b\":{\"attrs\":{\"nextValue\":[\"ONE\",\"TWO\",\"THREE\"],\"value\":[\"ONE\",\"TWO\",\"THREE\"]},\"children\":[]}"
                + "};",
            hint.toJson());

    }

    public enum TestEnum {
        ONE, TWO, THREE
    }

    @XmlTransient
    abstract static class TestAbstract {

        TestEnum value, key;

        @XmlAttribute
        public TestEnum getValue() {
            return value;
        }

        @XmlAttribute(name = "nextValue")
        public TestEnum getKey() {
            return key;
        }

    }

    @XmlRootElement(name = "A")
    static class TestA extends TestAbstract {
    }

    @XmlRootElement(name = "b")
    static class B extends TestAbstract {
    }

    @XmlRootElement(name = "C")
    static class TestClass {

        @XmlElementRef
        public List<TestAbstract> getList() {
            return null;
        }

        @XmlAttribute
        public Boolean getBoole() {
            return true;
        }

        @XmlAttribute
        public boolean isBoole2() {
            return true;
        }

        @XmlAttribute
        public String getValue3() {
            return "";
        }

        @XmlElement(name = "D")
        public TestA getEnum() {
            return null;
        }

    }

    @XmlRootElement()
    static class NestedParameterized<T> {

    }

    @XmlRootElement
    static class DefaultNameTestClass {

        @XmlElementRef()
        public List<NestedParameterized<TestA>> getParameterizedList() {
            return List.of();
        }

    }

}
