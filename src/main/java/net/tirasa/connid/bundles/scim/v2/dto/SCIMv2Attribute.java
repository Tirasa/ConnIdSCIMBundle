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
package net.tirasa.connid.bundles.scim.v2.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseAttribute;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SCIMv2Attribute extends SCIMBaseAttribute<SCIMv2Attribute> {

    @JsonProperty
    private Mutability mutability;

    @JsonProperty
    private String returned;

    @JsonProperty
    private Uniqueness uniqueness;

    @JsonProperty
    List<String> referenceTypes = new ArrayList<>();

    @JsonIgnore
    private String extensionSchema;

    public Mutability getMutability() {
        return mutability;
    }

    public void setMutability(final Mutability mutability) {
        this.mutability = mutability;
    }

    public String getReturned() {
        return returned;
    }

    public void setReturned(String returned) {
        this.returned = returned;
    }

    public Uniqueness getUniqueness() {
        return uniqueness;
    }

    public void setUniqueness(final Uniqueness uniqueness) {
        this.uniqueness = uniqueness;
    }

    public List<String> getReferenceTypes() {
        return referenceTypes;
    }

    public void setReferenceTypes(List<String> referenceTypes) {
        this.referenceTypes = referenceTypes;
    }

    public String getExtensionSchema() {
        return extensionSchema;
    }

    public void setExtensionSchema(String extensionSchema) {
        this.extensionSchema = extensionSchema;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("mutability", mutability)
                .append("returned", returned)
                .append("uniqueness", uniqueness)
                .append("referenceTypes", referenceTypes)
                .append("extensionSchema", extensionSchema)
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
