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
package net.tirasa.connid.bundles.scim.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import java.util.TreeSet;
import org.identityconnectors.common.logging.Log;

public abstract class AbstractSCIMBaseResource<MT extends SCIMBaseMeta> implements SCIMBaseResource<MT> {

    private static final long serialVersionUID = 3673404125396687366L;

    protected static final Log LOG = Log.getLog(AbstractSCIMBaseResource.class);

    @JsonProperty
    protected MT meta;

    @JsonProperty
    protected String id;

    protected final Set<String> schemas = new TreeSet<>();

    public AbstractSCIMBaseResource() {
    }

    public AbstractSCIMBaseResource(final MT meta) {
        this.meta = meta;
    }

    @Override
    public Set<String> getSchemas() {
        return schemas;
    }

    @Override
    public void setSchemas(final Set<String> schemas) {
        this.schemas.clear();
        this.schemas.addAll(schemas);
    }

    @Override
    public MT getMeta() {
        return meta;
    }

    @Override
    public void setMeta(final MT meta) {
        this.meta = meta;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(final String id) {
        this.id = id;
    }

}
