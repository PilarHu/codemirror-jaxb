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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author cserepj
 */
class TagInfo {

    /**
     * The XML tag name
     */
    private final Class cl;

    private final String tag;
    /**
     * All possible atribute values
     */
    private final Map<String, Set<String>> attrs = new HashMap<>();
    /**
     * What tags can be beneath this tag
     */
    private final Set<String> children = new TreeSet<>();
    /**
     * What other tags may be used instead of this one
     */
    private final Set<String> overrides = new HashSet<>();

    TagInfo(Class cl, String tag) {
        this.cl = cl;
        this.tag = tag;
    }

    TagInfo(Class cl, String tag, TagInfo clone) {
        this.cl = cl;
        this.tag = tag;
        this.children.addAll(clone.children);
        this.attrs.putAll(clone.attrs);
        this.overrides.addAll(clone.overrides);
    }

    @JsonIgnore
    String getTag() {
        return tag;
    }

    @JsonProperty(value = "attrs")
    Map<String, Set<String>> getAttrs() {
        return attrs;
    }

    @JsonIgnore
    Set<String> getOverrides() {
        return overrides;
    }

    @JsonProperty("children")
    Set<String> getChildren() {
        return children;
    }

    TagInfo withAttribute(String name, Set<String> possibleValues) {
        attrs.put(name, possibleValues);
        return this;
    }

    TagInfo withAttribute(String name) {
        attrs.put(name, null);
        return this;
    }

    TagInfo withChild(TagInfo child) {
        if (child != null) {
            if (child.tag != null) {
                children.add(child.tag);
            }
            if (!child.overrides.isEmpty()) {
                children.addAll(child.overrides);
            }
        }
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.cl);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TagInfo other = (TagInfo) obj;
        if (!Objects.equals(this.cl, other.cl)) {
            return false;
        }
        return true;
    }

}
