/**
 * Copyright (C) 2018 ConnId (connid-dev@googlegroups.com)
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
package net.tirasa.connid.bundles.scim.common.dto;

import java.io.Serializable;

public class BaseResourceReference implements Serializable {

    private static final long serialVersionUID = 9126588075353486789L;

    protected String value;

    protected String ref;

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
    
    @Override
    public String toString() {
        return new org.apache.commons.lang3.builder.ToStringBuilder(this)
                .append("value", value)
                .append("ref", ref)
                .toString();
    }

    public static final class Builder {
        private String value;
        private String ref;

        public Builder() {
        }

        public static Builder aBaseResourceReference() {
            return new Builder();
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder ref(String ref) {
            this.ref = ref;
            return this;
        }

        public BaseResourceReference build() {
            BaseResourceReference baseResourceReference = new BaseResourceReference();
            baseResourceReference.setValue(value);
            baseResourceReference.setRef(ref);
            return baseResourceReference;
        }
    }
}
