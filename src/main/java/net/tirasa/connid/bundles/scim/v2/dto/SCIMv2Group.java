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

import java.util.List;
import net.tirasa.connid.bundles.scim.common.dto.AbstractSCIMGroup;
import net.tirasa.connid.bundles.scim.common.dto.BaseResourceReference;

public class SCIMv2Group extends AbstractSCIMGroup<SCIMv2Meta> {

    private static final long serialVersionUID = -4821376864305003206L;

    public static final class Builder {

        private final SCIMv2Group instance = new SCIMv2Group();

        public Builder meta(final SCIMv2Meta meta) {
            instance.setMeta(meta);
            return this;
        }

        public Builder id(final String id) {
            instance.setId(id);
            return this;
        }

        public Builder displayName(final String displayName) {
            instance.setDisplayName(displayName);
            return this;
        }

        public Builder members(final List<BaseResourceReference> members) {
            instance.members.clear();
            instance.members.addAll(members);
            return this;
        }

        public SCIMv2Group build() {
            return instance;
        }
    }

    public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:Group";

    public static final String RESOURCE_NAME = "Group";

    public SCIMv2Group() {
        super(SCHEMA_URI, new SCIMv2Meta(RESOURCE_NAME));
    }

    @Override
    public String toString() {
        return "SCIMv2Group{"
                + "displayName='" + displayName
                + ", members=" + members
                + ", schemas=" + schemas
                + ", baseSchema='" + baseSchema
                + ", meta=" + meta
                + ", id='" + id + '}';
    }
}
