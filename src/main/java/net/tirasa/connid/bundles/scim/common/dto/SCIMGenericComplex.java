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
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import org.identityconnectors.common.StringUtil;

public class SCIMGenericComplex<T extends Serializable> extends AbstractSCIMComplex {

    private static final long serialVersionUID = -5982485563252126677L;

    @JsonProperty(access = Access.READ_ONLY)
    private String display;

    @JsonProperty
    private T type;

    @JsonProperty
    private Boolean primary;

    @JsonProperty
    private String operation;

    public String getDisplay() {
        return display;
    }

    public T getType() {
        return type;
    }

    public String getOperation() {
        return operation;
    }

    public void setDisplay(final String display) {
        this.display = display;
    }

    public void setType(final T type) {
        this.type = type;
    }

    public Boolean isPrimary() {
        return primary;
    }

    public void setPrimary(final Boolean primary) {
        this.primary = primary;
    }

    public void setOperation(final String operation) {
        this.operation = operation;
    }

    @Override
    protected List<Field> getDeclaredFields() {
        return SCIMUtils.getAllFieldsList(this.getClass()).stream().
                filter(f -> !"LOG".equals(f.getName()) && !"serialVersionUID".equals(f.getName())).
                collect(Collectors.toList());
    }

    @Override
    protected String getAttributeName(
            final String id, final Field field, final SCIMConnectorConfiguration configuration) {
        return id.concat(".")
                .concat(type == null
                        ? (StringUtil.isBlank(configuration.getGenericComplexType())
                        ? StringUtil.EMPTY
                        : configuration.getGenericComplexType().concat("."))
                        : type.toString().concat("."))
                .concat(field.getName());
    }

    @Override
    public String toString() {
        return "SCIMGenericComplex{"
                + "display=" + display
                + ", type=" + type
                + ", primary=" + primary
                + ", operation=" + operation
                + ", value=" + value
                + '}';
    }

    public static final class Builder<T extends Serializable> {
        private String value;
        private String display;
        private T type;
        private Boolean primary;
        private String operation;

        public Builder() {
        }

        public static Builder aSCIMGenericComplex() {
            return new Builder();
        }

        public Builder value(final String value) {
            this.value = value;
            return this;
        }

        public Builder display(final String display) {
            this.display = display;
            return this;
        }

        public Builder type(final T type) {
            this.type = type;
            return this;
        }

        public Builder primary(final Boolean primary) {
            this.primary = primary;
            return this;
        }

        public Builder operation(final String operation) {
            this.operation = operation;
            return this;
        }

        public SCIMGenericComplex build() {
            SCIMGenericComplex sCIMGenericComplex = new SCIMGenericComplex();
            sCIMGenericComplex.setValue(value);
            sCIMGenericComplex.setDisplay(display);
            sCIMGenericComplex.setType(type);
            sCIMGenericComplex.setPrimary(primary);
            sCIMGenericComplex.setOperation(operation);
            return sCIMGenericComplex;
        }
    }
}
