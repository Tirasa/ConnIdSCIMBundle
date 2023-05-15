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
package net.tirasa.connid.bundles.scim.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class BaseResourceReference implements Serializable {

    private static final long serialVersionUID = 9126588075353486789L;

    protected String value;

    @JsonProperty("$ref")
    protected String ref;

    protected String display;

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }


    public String getRef() {
        return ref;
    }

    public void setRef(final String ref) {
        this.ref = ref;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(final String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return new org.apache.commons.lang3.builder.ToStringBuilder(this)
                .append("value", value)
                .append("ref", ref)
                .append("display", display)
                .toString();
    }


    public static final class Builder {
        private String value;
        private String ref;
        private String display;

        public Builder() {
        }

        public Builder value(final String value) {
            this.value = value;
            return this;
        }

        public Builder ref(final String ref) {
            this.ref = ref;
            return this;
        }

        public Builder display(final String display) {
            this.display = display;
            return this;
        }

        public BaseResourceReference build() {
            BaseResourceReference baseResourceReference = new BaseResourceReference();
            baseResourceReference.setValue(value);
            baseResourceReference.setRef(ref);
            baseResourceReference.setDisplay(display);
            return baseResourceReference;
        }
    }

}
