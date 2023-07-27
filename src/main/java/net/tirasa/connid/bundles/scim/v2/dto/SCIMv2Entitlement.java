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

import net.tirasa.connid.bundles.scim.common.dto.BaseResourceReference;

public class SCIMv2Entitlement extends BaseResourceReference {

    private static final long serialVersionUID = 9126588075353486789L;

    private String type;

    private Boolean primary;

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public Boolean isPrimary() {
        return primary;
    }

    public void setPrimary(final Boolean primary) {
        this.primary = primary;
    }

    @Override
    public String toString() {
        return "SCIMv2Entitlement{" + "type='" + type + '\'' + ", primary=" + primary + ", value='" + value + '\''
                + ", ref='" + ref + '\'' + ", display='" + display + '\'' + '}';
    }

    public static final class Builder {
        private String value;
        private String ref;
        private String display;
        private String type;
        private Boolean primary;

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

        public Builder type(final String type) {
            this.type = type;
            return this;
        }

        public Builder primary(final Boolean primary) {
            this.primary = primary;
            return this;
        }

        public SCIMv2Entitlement build() {
            SCIMv2Entitlement sCIMv2Entitlement = new SCIMv2Entitlement();
            sCIMv2Entitlement.setValue(value);
            sCIMv2Entitlement.setRef(ref);
            sCIMv2Entitlement.setDisplay(display);
            sCIMv2Entitlement.setType(type);
            sCIMv2Entitlement.setPrimary(primary);
            return sCIMv2Entitlement;
        }
    }
}
