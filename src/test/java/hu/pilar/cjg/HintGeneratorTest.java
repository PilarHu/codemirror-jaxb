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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.LinkedHashSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

/**
 *
 * @author cserepj
 */
public class HintGeneratorTest {

    public static enum TestEnum {
        ONE, TWO, THREE
    }

    @XmlTransient
    public abstract static class TestAbstract {

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
    public static class TestA extends TestAbstract {
    }

    @XmlRootElement(name = "b")
    public static class B extends TestAbstract {
    }

    @XmlRootElement(name = "C")
    public static class TestClass {

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

    @Test
    public void testNonJaxb() {
        HintGenerator hg = new HintGenerator(new ObjectMapper());

        XmlHint hint = hg.getHintsFor(Object.class);
        assertNull(hint);
        hg = new HintGenerator(new ObjectMapper(), (Class parent) -> Collections.emptySet());
    }

    /**
     * Test of findReturnType method, of class HintGenerator.
     */
    @Test
    public void test() {

        HintGenerator hg = new HintGenerator(new ObjectMapper());

        XmlHint hint = hg.getHintsFor(TestClass.class);
        assertNotNull(hint);
        System.out.println(hint.toJson());
        assertEquals("var tags = {"
                + "\"!top\":[\"C\"],"
                + "\"!attrs\":{},"
                + "\"A\":{\"attrs\":{\"nextValue\":[\"ONE\",\"TWO\",\"THREE\"],\"value\":[\"ONE\",\"TWO\",\"THREE\"]},\"children\":[]},"
                + "\"C\":{\"attrs\":{\"boole\":[\"true\",\"false\"],\"boole2\":[\"true\",\"false\"],\"value3\":null},\"children\":[\"A\",\"D\",\"b\"]},"
                + "\"D\":{\"attrs\":{\"nextValue\":[\"ONE\",\"TWO\",\"THREE\"],\"value\":[\"ONE\",\"TWO\",\"THREE\"]},\"children\":[]},"
                + "\"b\":{\"attrs\":{\"nextValue\":[\"ONE\",\"TWO\",\"THREE\"],\"value\":[\"ONE\",\"TWO\",\"THREE\"]},\"children\":[]}"
                + "};",
                hint.toJson());

        hg = new HintGenerator(new ObjectMapper(), new IAttributeValueFactory() {
            @Override
            public Set<String> getValuesFor(String attributeName, Class type) {
                if ("value3".equals(attributeName)) {
                    Set<String> s = new LinkedHashSet<String>();
                    s.add("1979");
                    s.add("1980");
                    s.add("1981");
                    return s;
                }
                return null;
            }
        });
        hint = hg.getHintsFor(TestClass.class);
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

}
