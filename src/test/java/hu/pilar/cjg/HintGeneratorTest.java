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
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

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

    @XmlRootElement(name = "B")
    public static class TestB extends TestAbstract {
    }

    @XmlRootElement(name = "C")
    public static class TestClass {

        @XmlElementRef
        public List<TestAbstract> getList() {
            return null;
        }

        @XmlAttribute
        public String getValue3() {
            return "";
        }

    }

    /**
     * Test of findReturnType method, of class HintGenerator.
     */
    @Test
    public void test() {

        HintGenerator hg = new HintGenerator(new ObjectMapper());

        XmlHint hint = hg.getHintsFor(TestClass.class);
        assertNotNull(hint);
        assertEquals("var tags = {"
                + "\"!top\":[\"C\"],"
                + "\"!attrs\":{},"
                + "\"A\":{\"attrs\":{\"nextValue\":[\"ONE\",\"TWO\",\"THREE\"],\"value\":[\"ONE\",\"TWO\",\"THREE\"]},\"children\":[]},"
                + "\"B\":{\"attrs\":{\"nextValue\":[\"ONE\",\"TWO\",\"THREE\"],\"value\":[\"ONE\",\"TWO\",\"THREE\"]},\"children\":[]},"
                + "\"C\":{\"attrs\":{\"value3\":null},\"children\":[\"A\",\"B\"]}};",
                hint.toJson());

        hg = new HintGenerator(new ObjectMapper(), new IAttributeValueFactory() {
            @Override
            public Set<String> getValuesFor(String attributeName, Class type) {
                if ("value3".equals(attributeName)) {
                    return Sets.newHashSet("1979", "1980", "1981");
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
                + "\"B\":{\"attrs\":{\"nextValue\":[\"ONE\",\"TWO\",\"THREE\"],\"value\":[\"ONE\",\"TWO\",\"THREE\"]},\"children\":[]},"
                + "\"C\":{\"attrs\":{\"value3\":[\"1979\",\"1981\",\"1980\"]},\"children\":[\"A\",\"B\"]}};",
                hint.toJson());

    }

}
