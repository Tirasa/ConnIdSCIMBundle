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
package net.tirasa.connid.bundles.scim.v2.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseAttribute;

public class SCIMv2Attribute extends SCIMBaseAttribute<SCIMv2Attribute> {

    private static final long serialVersionUID = 4869263621226124404L;

    @JsonProperty
    private Mutability mutability;

    @JsonProperty
    private String returned;

    @JsonProperty
    private Uniqueness uniqueness;

    @JsonProperty
    private List<String> referenceTypes = new ArrayList<>();

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

    public void setReturned(final String returned) {
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

    public void setReferenceTypes(final List<String> referenceTypes) {
        this.referenceTypes = referenceTypes;
    }

    public String getExtensionSchema() {
        return extensionSchema;
    }

    public void setExtensionSchema(final String extensionSchema) {
        this.extensionSchema = extensionSchema;
    }

    @Override
    public String toString() {
        return "SCIMv2Attribute{"
                + "mutability=" + mutability
                + ", returned=" + returned
                + ", uniqueness=" + uniqueness
                + ", referenceTypes=" + referenceTypes
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
