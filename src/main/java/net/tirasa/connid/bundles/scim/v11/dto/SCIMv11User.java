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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.tirasa.connid.bundles.scim.common.dto.AbstractSCIMUser;
import net.tirasa.connid.bundles.scim.common.dto.SCIMSchema;
import net.tirasa.connid.bundles.scim.v11.service.SCIMv11Service;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.objects.Attribute;

public class SCIMv11User extends AbstractSCIMUser<SCIMv11Attribute, SCIMDefault, SCIMDefault, SCIMv11Meta> {

    private static final long serialVersionUID = -6868285123690771711L;

    public SCIMv11User() {
        super();
    }

    @Override
    protected void handleRoles(final Object value) {
        handleSCIMDefaultObject(
                String.class.cast(value),
                this.roles,
                s -> s.setValue(String.class.cast(value)));
    }

    @Override
    protected void handlex509Certificates(final Object value) {
        handleSCIMDefaultObject(
                String.class.cast(value),
                this.x509Certificates,
                s -> s.setValue(String.class.cast(value)));
    }

    @Override
    protected void handleEntitlements(final Object value) {
        handleSCIMDefaultObject(
                String.class.cast(value),
                this.entitlements,
                s -> s.setValue(String.class.cast(value)));
    }

    /**
     * Populate 'scimAttributes' map with custom attributes (taken from Connector configuration)
     * according to related values in current ConnId attributes
     *
     * @param attributes
     * @param customAttributesJSON
     */
    @JsonIgnore
    @Override
    public void fillSCIMCustomAttributes(final Set<Attribute> attributes, final String customAttributesJSON) {
        SCIMSchema<SCIMv11Attribute> customAttributesObj = SCIMv11Service.extractSCIMSchemas(customAttributesJSON);
        if (customAttributesObj != null) {
            for (Attribute attribute : attributes) {
                if (!CollectionUtil.isEmpty(attribute.getValue())) {
                    for (SCIMv11Attribute customAttribute : customAttributesObj.getAttributes()) {
                        String externalAttributeName = customAttribute.getSchema()
                                .concat(".")
                                .concat(customAttribute.getName());
                        if (externalAttributeName.equals(attribute.getName())) {
                            scimCustomAttributes.put(customAttribute, attribute.getValue());
                            break;
                        }
                    }
                }
            }
        }
    }

    @JsonIgnore
    private void handleSCIMDefaultObject(
            final String value, final List<SCIMDefault> list, final Consumer<SCIMDefault> setter) {

        SCIMDefault selected = null;
        for (SCIMDefault scimDefault : list) {
            if (scimDefault.getValue().equals(value)) {
                selected = scimDefault;
                break;
            }
        }
        if (selected == null) {
            selected = new SCIMDefault();
            list.add(selected);
        }

        setter.accept(selected);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).build();
    }
}
