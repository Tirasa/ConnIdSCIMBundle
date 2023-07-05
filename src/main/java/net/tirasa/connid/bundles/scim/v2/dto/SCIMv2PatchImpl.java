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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class SCIMv2PatchImpl implements SCIMv2Patch {

    public static final class Builder {

        private final SCIMv2PatchImpl instance = new SCIMv2PatchImpl();

        public Builder operations(final Set<SCIMv2PatchOperation> operations) {
            instance.operations.clear();
            instance.operations.addAll(operations);
            return this;
        }

        public SCIMv2PatchImpl build() {
            return instance;
        }
    }

    private static final long serialVersionUID = -8309238293109738832L;

    public static final String SCHEMA_URI = "urn:ietf:params:scim:api:messages:2.0:PatchOp";

    private final Set<String> schemas = new TreeSet<>();

    @JsonIgnore
    protected String baseSchema;

    @JsonProperty("Operations")
    private final Set<SCIMv2PatchOperation> operations = new HashSet<>();

    public SCIMv2PatchImpl() {
        this.baseSchema = SCHEMA_URI;
        schemas.add(baseSchema);
    }

    @Override
    public Set<String> getSchemas() {
        return schemas;
    }

    @Override
    public Set<SCIMv2PatchOperation> getOperations() {
        return operations;
    }

    @Override
    public void addOperation(final SCIMv2PatchOperation operation) {
        this.operations.add(operation);
    }

    @Override
    public String toString() {
        return "SCIMv2Patch{"
                + "schemas=" + schemas
                + ", baseSchema='" + baseSchema + '\''
                + ", operations=" + operations
                + '}';
    }
}
