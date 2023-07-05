/**
 * Copyright (C) 2018 ConnId (connid-dev@googlegroups.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tirasa.connid.bundles.scim.v11.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseAttribute;

public class SCIMv11Attribute extends SCIMBaseAttribute<SCIMv11Attribute> {

    private static final long serialVersionUID = -3794205038896534173L;

    @JsonProperty
    private String multiValuedAttributeChildName;

    @JsonProperty
    private String schema;

    @JsonProperty
    private Boolean readOnly;

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public Boolean getMultiValued() {
        return multiValued;
    }

    @Override
    public void setMultiValued(final Boolean multiValued) {
        this.multiValued = multiValued;
    }

    public String getMultiValuedAttributeChildName() {
        return multiValuedAttributeChildName;
    }

    public void setMultiValuedAttributeChildName(final String multiValuedAttributeChildName) {
        this.multiValuedAttributeChildName = multiValuedAttributeChildName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(final String schema) {
        this.schema = schema;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(final Boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public Boolean getRequired() {
        return required;
    }

    @Override
    public void setRequired(final Boolean required) {
        this.required = required;
    }

    @Override
    public Boolean getCaseExact() {
        return caseExact;
    }

    @Override
    public void setCaseExact(final Boolean caseExact) {
        this.caseExact = caseExact;
    }

    @Override
    public List<String> getCanonicalValues() {
        return canonicalValues;
    }

    @Override
    public String toString() {
        return "SCIMv11Attribute{"
                + "multiValuedAttributeChildName=" + multiValuedAttributeChildName
                + ", schema=" + schema
                + ", readOnly=" + readOnly
                + ", type=" + type
                + ", name=" + name
                + ", multiValued=" + multiValued
                + ", description=" + description
                + ", required=" + required
                + ", caseExact=" + caseExact
                + ", canonicalValues=" + canonicalValues
                + ", subAttributes=" + subAttributes
                + '}';
    }
}
