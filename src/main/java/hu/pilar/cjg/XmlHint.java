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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author {cserepj@pilar.hu}
 */
public class XmlHint {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlHint.class);
    private List<String> topElements = new ArrayList<>();
    private final Map<String, Set<String>> attrs = new HashMap<>();
    private Map<String, TagInfo> any = new HashMap<>();

    private ObjectMapper mapper;

    XmlHint(ObjectMapper mapper, TagInfo tag) {
        addTag(tag, true);
        this.mapper = mapper;
    }

    @JsonProperty("!top")
    List<String> getTopElements() {
        return topElements;
    }

    @JsonProperty("!attrs")
    Map<String, Set<String>> getAttrs() {
        return attrs;
    }

    XmlHint addTag(TagInfo tag) {
        return addTag(tag, false);
    }

    XmlHint addTag(TagInfo tag, boolean topLevel) {
        any.put(tag.getTag(), tag);
        if (topLevel) {
            topElements.add(tag.getTag());
        }
        return this;
    }

    @JsonAnyGetter
    Map<String, TagInfo> getAny() {
        return new TreeMap<>(any);
    }

    public String toJson() {
        try {
            return "var tags = " + mapper.writeValueAsString(this) + ";";
        } catch (JsonProcessingException ex) {
            LOGGER.error("", ex);
            return "";
        }
    }

}
