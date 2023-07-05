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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;
import java.util.TreeSet;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBasePatch;

public class SCIMv11BasePatch implements SCIMBasePatch {

    public static final String SCHEMA_URI = "urn:scim:schemas:core:1.0";

    private final Set<String> schemas = new TreeSet<>();

    @JsonIgnore
    protected String baseSchema;

    public SCIMv11BasePatch() {
        this.baseSchema = SCHEMA_URI;
        schemas.add(baseSchema);
    }

    @Override
    public Set<String> getSchemas() {
        return schemas;
    }
}
