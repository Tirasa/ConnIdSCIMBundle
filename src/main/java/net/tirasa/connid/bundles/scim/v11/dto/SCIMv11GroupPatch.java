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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class SCIMv11GroupPatch extends SCIMv11BasePatch {

    private static final long serialVersionUID = -8123835603218705518L;

    public static final class Builder {

        private final SCIMv11GroupPatch instance = new SCIMv11GroupPatch();

        public Builder members(final List<Scimv11GroupPatchOperation> members) {
            instance.members.clear();
            instance.members.addAll(members);
            return this;
        }

        public SCIMv11GroupPatch build() {
            return instance;
        }
    }

    @JsonProperty("members")
    private final List<Scimv11GroupPatchOperation> members = new ArrayList<>();

    public List<Scimv11GroupPatchOperation> getMembers() {
        return members;
    }

    public SCIMv11GroupPatch() {
        super();
    }

    @Override
    public String toString() {
        return "SCIMv11GroupPatch{" + "members=" + members + ", baseSchema='" + baseSchema + '\'' + '}';
    }
}
