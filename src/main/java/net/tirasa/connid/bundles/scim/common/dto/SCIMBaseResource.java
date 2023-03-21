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

import java.io.Serializable;
import java.util.Set;

public interface SCIMBaseResource<AT, MT extends SCIMBaseMeta> extends Serializable {

    Set<String> getSchemas();

    void setSchemas(Set<String> schemas);

    MT getMeta();

    void setMeta(MT meta);

    String getId();

    void setId(String id);

    String getExternalId();

    void setExternalId(String externalId);

    String getBaseSchema();

    Set<AT> toAttributes() throws IllegalArgumentException, IllegalAccessException;

    void fromAttributes(Set<AT> attributes);

}
