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

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;

public class SCIMDefaultComplex extends AbstractSCIMComplex {

    private static final long serialVersionUID = 4302319332020863582L;

    @Override
    protected List<Field> getDeclaredFields() {
        return SCIMUtils.getAllFieldsList(getClass()).stream().
                filter(f -> !"LOG".equals(f.getName()) && !"serialVersionUID".equals(f.getName())).
                collect(Collectors.toList());
    }

    @Override
    protected String getAttributeName(
            final String id,
            final Field field,
            final SCIMConnectorConfiguration configuration) {

        return id.concat(".")
                .concat("default")
                .concat(".")
                .concat(field.getName());
    }

    @Override
    public String toString() {
        return "SCIMDefaultComplex{"
                + "value=" + value
                + '}';
    }
}
