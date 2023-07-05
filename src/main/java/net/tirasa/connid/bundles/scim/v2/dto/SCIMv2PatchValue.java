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

import com.fasterxml.jackson.annotation.JsonProperty;

public class SCIMv2PatchValue {

    @JsonProperty
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public static final class Builder {

        private String value;

        public Builder value(final String value) {
            this.value = value;
            return this;
        }

        public SCIMv2PatchValue build() {
            SCIMv2PatchValue scimv2PatchValue = new SCIMv2PatchValue();
            scimv2PatchValue.setValue(value);
            return scimv2PatchValue;
        }
    }

    @Override
    public String toString() {
        return "SCIMv2PatchValue{" + "value=" + value + '}';
    }
}
