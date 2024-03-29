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
import java.io.Serializable;

public class Scimv11GroupPatchOperation implements Serializable {

    private static final long serialVersionUID = 1759455963046916983L;

    public static final class Builder {

        private final Scimv11GroupPatchOperation instance = new Scimv11GroupPatchOperation();

        public Builder display(final String display) {
            instance.setDisplay(display);
            return this;
        }

        public Builder value(final String value) {
            instance.setValue(value);
            return this;
        }

        public Builder operation(final String operation) {
            instance.setOperation(operation);
            return this;
        }

        public Scimv11GroupPatchOperation build() {
            return instance;
        }
    }

    @JsonProperty
    private String display;

    @JsonProperty
    private String value;

    @JsonProperty
    private String operation;

    public String getDisplay() {
        return display;
    }

    public void setDisplay(final String display) {
        this.display = display;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(final String operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "Scimv11GroupPatchOperation{"
                + "display='" + display + '\''
                + ", value='" + value + '\''
                + ", operation='" + operation + '\''
                + '}';
    }
}
