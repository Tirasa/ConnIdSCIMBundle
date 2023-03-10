/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package net.tirasa.connid.bundles.scim.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import org.identityconnectors.common.logging.Log;

public abstract class SCIMBaseResource<AT, MT extends SCIMBaseMeta> implements Serializable {

    private static final long serialVersionUID = 3673404125396687366L;

    protected static final Log LOG = Log.getLog(SCIMBaseResource.class);

    @JsonProperty
    protected MT meta;

    @JsonProperty
    private String id;

    @JsonProperty
    private String externalId;

    private String baseSchema;

    protected final Set<String> schemas = new TreeSet<>();

    public SCIMBaseResource() {
    }

    public SCIMBaseResource(String baseSchema, String resourceType, MT meta) {
        schemas.add(baseSchema);
        this.baseSchema = baseSchema;
        this.meta = meta;
    }

    public Set<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(final Set<String> schemas) {
        this.schemas.clear();
        this.schemas.addAll(schemas);
    }

    public MT getMeta() {
        return meta;
    }

    public void setMeta(MT meta) {
        this.meta = meta;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getBaseSchema() {
        return baseSchema;
    }

    public abstract Set<AT> toAttributes() throws IllegalArgumentException, IllegalAccessException;

    public abstract void fromAttributes(Set<AT> attributes);
}
