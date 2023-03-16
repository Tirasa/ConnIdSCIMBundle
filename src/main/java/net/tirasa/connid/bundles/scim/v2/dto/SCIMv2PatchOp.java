/**
 * Copyright © 2018 ConnId (connid-dev@googlegroups.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.tirasa.connid.bundles.scim.v2.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SCIMv2PatchOp {

    @JsonProperty("schemas")
    private List<String> schemas;
    @JsonProperty("Operations")
    private List<SCIMv2PatchOperation> operations;

    @JsonProperty("schemas")
    public List<String> getSchemas() {
        return schemas;
    }

    @JsonProperty("schemas")
    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    @JsonProperty("Operations")
    public List<SCIMv2PatchOperation> getOperations() {
        return operations;
    }

    @JsonProperty("Operations")
    public void setOperations(List<SCIMv2PatchOperation> operations) {
        this.operations = operations;
    }

}
