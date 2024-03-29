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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author cserepj
 */
class TagInfo {

    private static final Logger LOG = LoggerFactory.getLogger(TagInfo.class);
    private final String tag;
    /**
     * All possible atribute values
     */
    private final Map<String, Set<String>> attrs = new TreeMap<>();
    /**
     * What tags can be beneath this tag
     */
    private final Set<String> children = new TreeSet<>();
    /**
     * What other tags may be used instead of this one
     */
    private final Set<String> overrides = new HashSet<>();

    TagInfo(String tag) {
        this.tag = tag;
    }

    TagInfo(String tag, TagInfo clone) {
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

    void withAttribute(String name, Set<String> possibleValues) {
        LOG.debug("      Adding attribute {} to {}", name, this.tag);
        attrs.put(name, possibleValues);
    }

    void withChild(@Nonnull TagInfo child) {
        if (child.tag != null) {
            LOG.debug("      Adding tag {} to {}", child.tag, this.tag);
            children.add(child.tag);
        }
        if (!child.overrides.isEmpty()) {
            children.addAll(child.overrides);
        }
    }

    @Override
    public String toString() {
        return "TagInfo{" +
            "tag='" + tag + '\'' +
            ", attrs=" + attrs +
            ", children=" + children +
            ", overrides=" + overrides +
            '}';
    }
}
