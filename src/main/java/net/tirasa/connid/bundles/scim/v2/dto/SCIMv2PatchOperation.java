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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SCIMv2PatchOperation implements SCIMPatchOperation {

    private static final long serialVersionUID = -1398745689025351659L;

    public static final class Builder {

        private final SCIMv2PatchOperation instance = new SCIMv2PatchOperation();

        public Builder op(final String op) {
            instance.setOp(op);
            return this;
        }

        public Builder path(final String path) {
            instance.setPath(path);
            return this;
        }

        public Builder value(final Object value) {
            instance.setValue(value);
            return this;
        }

        public SCIMv2PatchOperation build() {
            return instance;
        }
    }

    @JsonProperty("op")
    private String op;

    @JsonProperty("path")
    private String path;

    @JsonProperty("value")
    private Object value;

    public String getOp() {
        return op;
    }

    public void setOp(final String op) {
        this.op = op;
    }

    @JsonProperty("op")
    @Override
    public String getOperation() {
        return op;
    }

    @JsonProperty("op")
    @Override
    public void setOperation(final String op) {
        this.op = op;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(final String path) {
        this.path = path;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(final Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "SCIMv2PatchOperation{" + "op='" + op + '\'' + ", path='" + path + '\'' + ", value=" + value + '}';
    }
}
