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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import org.identityconnectors.framework.common.objects.Attribute;

public abstract class AbstractSCIMComplex implements SCIMComplexAttribute {

    private static final long serialVersionUID = 4302319332020863582L;

    @JsonProperty
    protected String value;

    @Override
    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Set<Attribute> toAttributes(final String id, final SCIMConnectorConfiguration configuration)
            throws IllegalArgumentException, IllegalAccessException {

        Set<Attribute> attrs = new HashSet<>();
        for (Field field : getDeclaredFields()) {
            if (!field.isAnnotationPresent(JsonIgnore.class)) {
                field.setAccessible(true);
                attrs.add(SCIMAttributeUtils.doBuildAttributeFromClassField(
                        field.get(this),
                        getAttributeName(id, field, configuration),
                        field.getType()).build());
            }
        }
        return attrs;
    }

    protected abstract List<Field> getDeclaredFields();

    protected abstract String getAttributeName(String id, Field field, SCIMConnectorConfiguration configuration);
}
