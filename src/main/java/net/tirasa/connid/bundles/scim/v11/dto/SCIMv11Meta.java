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
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.ArrayList;
import java.util.List;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseMeta;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class SCIMv11Meta extends SCIMBaseMeta {

    private static final long serialVersionUID = -9098875348807174527L;

    @JsonProperty
    private List<String> attributes = new ArrayList<>();

    public List<String> getAttributes() {
        return attributes;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setAttributes(final List<String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("created", created)
                .append("lastModified", lastModified)
                .append("location", location)
                .append("version", version)
                .append("attributes", attributes)
                .toString();
    }
}
