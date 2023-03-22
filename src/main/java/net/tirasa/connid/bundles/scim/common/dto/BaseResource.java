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

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Attribute;

public abstract class BaseResource implements Serializable {

    protected static final Log LOG = Log.getLog(BaseResource.class);

    private static final long serialVersionUID = -7603956873008734403L;

    protected Set<String> schemas = new TreeSet<>();

    public BaseResource(final String baseSchema) {
        schemas.add(baseSchema);
    }

    public Set<String> getSchemas() {
        return schemas;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setSchemas(final Set<String> schemas) {
        this.schemas.clear();
        this.schemas.addAll(schemas);
    }

    public abstract Set<Attribute> toAttributes() throws IllegalArgumentException, IllegalAccessException;

    public abstract void fromAttributes(Set<Attribute> attributes);

}
