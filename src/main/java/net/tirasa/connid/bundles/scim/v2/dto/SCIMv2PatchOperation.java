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
import java.io.Serializable;
import java.util.List;

public class SCIMv2PatchOperation implements Serializable {

    private static final long serialVersionUID = -1398745689025351659L;

    @JsonProperty("op")
    private String op;

    @JsonProperty("path")
    private String path;

    @JsonProperty("value")
    private List<SCIMv2PatchOperationValue> value;

    @JsonProperty("op")
    public String getOp() {
        return op;
    }

    @JsonProperty("op")
    public void setOp(final String op) {
        this.op = op;
    }

    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    @JsonProperty("path")
    public void setPath(final String path) {
        this.path = path;
    }

    @JsonProperty("value")
    public List<SCIMv2PatchOperationValue> getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(final List<SCIMv2PatchOperationValue> value) {
        this.value = value;
    }
}
