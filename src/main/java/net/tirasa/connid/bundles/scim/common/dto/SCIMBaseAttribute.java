/**
 * Copyright © 2018 ConnId (connid-dev@googlegroups.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tirasa.connid.bundles.scim.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SCIMBaseAttribute<SELF extends SCIMBaseAttribute> implements Serializable {

    @JsonProperty
    protected String type;

    @JsonProperty
    protected String name;

    @JsonProperty
    protected Boolean multiValued;

    @JsonProperty
    protected String description;

    @JsonProperty
    protected Boolean required;

    @JsonProperty
    protected Boolean caseExact;

    @JsonProperty
    protected List<String> canonicalValues = new ArrayList<>();

    @JsonProperty
    protected List<SELF> subAttributes = new ArrayList<>();

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Boolean getMultiValued() {
        return multiValued;
    }

    public void setMultiValued(final Boolean multiValued) {
        this.multiValued = multiValued;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(final Boolean required) {
        this.required = required;
    }

    public Boolean getCaseExact() {
        return caseExact;
    }

    public void setCaseExact(Boolean caseExact) {
        this.caseExact = caseExact;
    }

    public List<String> getCanonicalValues() {
        return canonicalValues;
    }

    public List<SELF> getSubAttributes() {
        return subAttributes;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setCanonicalValues(List<String> canonicalValues) {
        this.canonicalValues = canonicalValues;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setSubAttributes(List<SELF> subAttributes) {
        this.subAttributes = subAttributes;
    }
}
