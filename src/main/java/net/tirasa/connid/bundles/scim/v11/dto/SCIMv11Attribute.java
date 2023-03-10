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
package net.tirasa.connid.bundles.scim.v11.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseAttribute;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SCIMv11Attribute extends SCIMBaseAttribute<SCIMv11Attribute> {

    @JsonProperty
    private String multiValuedAttributeChildName;

    @JsonProperty
    private String schema;

    @JsonProperty
    private Boolean readOnly;

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

    public String getMultiValuedAttributeChildName() {
        return multiValuedAttributeChildName;
    }

    public void setMultiValuedAttributeChildName(final String multiValuedAttributeChildName) {
        this.multiValuedAttributeChildName = multiValuedAttributeChildName;
    }

    public String getDescription() {
        return description;
    }

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

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(final Boolean required) {
        this.required = required;
    }

    public Boolean getCaseExact() {
        return caseExact;
    }

    public void setCaseExact(final Boolean caseExact) {
        this.caseExact = caseExact;
    }

    public List<String> getCanonicalValues() {
        return canonicalValues;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("multiValuedAttributeChildName", multiValuedAttributeChildName)
                .append("schema", schema)
                .append("readOnly", readOnly)
                .append("type", type)
                .append("name", name)
                .append("multiValued", multiValued)
                .append("description", description)
                .append("required", required)
                .append("caseExact", caseExact)
                .append("canonicalValues", canonicalValues)
                .append("subAttributes", subAttributes)
                .toString();
    }
}
