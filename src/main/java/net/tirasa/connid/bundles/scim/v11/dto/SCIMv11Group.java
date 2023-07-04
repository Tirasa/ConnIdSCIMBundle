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

import java.util.List;
import net.tirasa.connid.bundles.scim.common.dto.AbstractSCIMGroup;
import net.tirasa.connid.bundles.scim.common.dto.BaseResourceReference;

public class SCIMv11Group extends AbstractSCIMGroup<SCIMv11Meta> {

    private static final long serialVersionUID = -7962830213458613351L;

    public static final String SCHEMA_URI = "urn:scim:schemas:core:1.0";

    public SCIMv11Group() {
        super(SCHEMA_URI, new SCIMv11Meta());
    }

    public static final class Builder {

        private SCIMv11Meta meta;

        private String id;

        private String displayName;

        private List<BaseResourceReference> members;

        public Builder() {
        }

        public Builder meta(final SCIMv11Meta meta) {
            this.meta = meta;
            return this;
        }

        public Builder id(final String id) {
            this.id = id;
            return this;
        }

        public Builder displayName(final String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder members(final List<BaseResourceReference> members) {
            this.members = members;
            return this;
        }

        public SCIMv11Group build() {
            SCIMv11Group sCIMv11Group = new SCIMv11Group();
            sCIMv11Group.setMeta(meta);
            sCIMv11Group.setId(id);
            sCIMv11Group.setDisplayName(displayName);
            sCIMv11Group.members = this.members;
            return sCIMv11Group;
        }
    }
}
