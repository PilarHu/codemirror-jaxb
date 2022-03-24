package hu.pilar.cjg;

import java.util.Set;

/**
 * Interface to add additional customization to the attribute value handling
 * 
 * @author cserepj
 */
public interface IAttributeValueFactory {

    /**
     * Return a set of possible values for the code completion of the given
     * attribute. Can be used to customize the generated grammar.
     *
     * @param attributeName the name of the attribute in the XML schema
     * @param type the java type returned by the getter method for the attribute
     */
    Set<String> getValuesFor(String attributeName, Class<?> type);
}
