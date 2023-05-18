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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SCIMv2PatchOperation<T extends Serializable> implements SCIMPatchOperation<T> {

    private static final long serialVersionUID = -1398745689025351659L;

    @JsonProperty("op") private String op;

    @JsonProperty("path") private String path;

    @JsonProperty("value") private final Map<String, List<SCIMv2PatchValue<T>>> values = new HashMap<>();

    public String getOp() {
        return op;
    }

    public void setOp(final String op) {
        this.op = op;
    }

    @JsonProperty("op") @Override public String getOperation() {
        return op;
    }

    @JsonProperty("op") @Override public void setOperation(final String op) {
        this.op = op;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public Map<String, List<SCIMv2PatchValue<T>>> getValues() {
        return values;
    }

    public static final class Builder<T extends Serializable> {
        private String op;
        private String path;
        private Map<String, List<SCIMv2PatchValue<T>>> values;

        public Builder() {
        }

        public Builder op(final String op) {
            this.op = op;
            return this;
        }

        public Builder path(final String path) {
            this.path = path;
            return this;
        }

        public Builder values(final Map<String, List<SCIMv2PatchValue<T>>> values) {
            this.values = values;
            return this;
        }

        public SCIMv2PatchOperation build() {
            SCIMv2PatchOperation sCIMv2PatchOperation = new SCIMv2PatchOperation();
            sCIMv2PatchOperation.setOp(op);
            sCIMv2PatchOperation.setPath(path);
            sCIMv2PatchOperation.values.clear();
            sCIMv2PatchOperation.values.putAll(values);
            return sCIMv2PatchOperation;
        }
    }

    @Override public String toString() {
        return "SCIMv2PatchOperation{" + "op='" + op + '\'' + ", path='" + path + '\'' + ", values=" + values + '}';
    }
}
