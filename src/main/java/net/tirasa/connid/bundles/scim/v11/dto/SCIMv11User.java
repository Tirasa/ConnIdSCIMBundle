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
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.tirasa.connid.bundles.scim.common.dto.AbstractSCIMUser;
import net.tirasa.connid.bundles.scim.common.dto.BaseResourceReference;
import net.tirasa.connid.bundles.scim.common.dto.SCIMDefaultComplex;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeUtil;

public class SCIMv11User extends AbstractSCIMUser<
        SCIMv11Attribute, SCIMDefaultComplex, BaseResourceReference, SCIMv11Meta, SCIMv11EnterpriseUser> {

    private static final long serialVersionUID = -6868285123690771711L;

    @JsonProperty(SCIMv11EnterpriseUser.SCHEMA_URI)
    protected SCIMv11EnterpriseUser enterpriseUser;

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
    protected void handleDefaultEntitlement(final Object value) {
        handleBaseResourceReference(
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
        SCIMUtils.extractSCIMSchemas(customAttributesJSON, SCIMv11Attribute.class).ifPresent(customAttributesObj -> {
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
        });
    }

    @Override
    public SCIMv11EnterpriseUser getEnterpriseUser() {
        return this.enterpriseUser;
    }

    @Override
    public void setEnterpriseUser(final SCIMv11EnterpriseUser enterpriseUser) {
        this.enterpriseUser = enterpriseUser;
    }

    @Override
    public void fillEnterpriseUser(final Set<Attribute> attributes) {
        this.enterpriseUser = new SCIMv11EnterpriseUser();
        attributes.stream().filter(a -> a.getName().startsWith(SCIMv11EnterpriseUser.SCHEMA_URI)).forEach(
                a -> {
                    SCIMv11EnterpriseUser.SCIMv11EnterpriseUserManager manager;
                    switch (a.getName().replace(SCIMv11EnterpriseUser.SCHEMA_URI + ".", StringUtil.EMPTY)) {
                        case "employeeNumber":
                            enterpriseUser.setEmployeeNumber(AttributeUtil.getAsStringValue(a));
                            break;
                        case "manager.managerId":
                            manager = enterpriseUser.getManager();
                            if (manager == null) {
                                manager = new SCIMv11EnterpriseUser.SCIMv11EnterpriseUserManager();
                                enterpriseUser.setManager(manager);
                            }
                            manager.setManagerId(AttributeUtil.getAsStringValue(a));
                            break;
                        case "manager.displayName":
                            manager = enterpriseUser.getManager();
                            if (manager == null) {
                                manager = new SCIMv11EnterpriseUser.SCIMv11EnterpriseUserManager();
                                enterpriseUser.setManager(manager);
                            }
                            manager.setDisplayName(AttributeUtil.getAsStringValue(a));
                            break;
                        default:
                        // do nothing
                    }
                });
    }

    @JsonIgnore
    private void handleSCIMDefaultObject(
            final String value, final List<SCIMDefaultComplex> list, final Consumer<SCIMDefaultComplex> setter) {

        SCIMDefaultComplex selected = null;
        for (SCIMDefaultComplex scimDefault : list) {
            if (scimDefault.getValue().equals(value)) {
                selected = scimDefault;
                break;
            }
        }
        if (selected == null) {
            selected = new SCIMDefaultComplex();
            list.add(selected);
        }

        setter.accept(selected);
    }

    @JsonIgnore
    private void handleBaseResourceReference(
            final String value,
            final List<BaseResourceReference> list,
            final Consumer<BaseResourceReference> setter) {

        BaseResourceReference selected = null;
        for (BaseResourceReference scimDefault : list) {
            if (scimDefault.getValue().equals(value)) {
                selected = scimDefault;
                break;
            }
        }
        if (selected == null) {
            selected = new BaseResourceReference();
            list.add(selected);
        }

        setter.accept(selected);
    }

    @Override
    protected void entitlementsToAttribute(
            final List<BaseResourceReference> entitlementRefs,
            final Set<Attribute> attrs) {

        // only manage the default entitlement
        if (!entitlementRefs.isEmpty()) {
            attrs.add(AttributeBuilder.build("entitlements.default.value", entitlementRefs.get(0).getValue()));
        }
    }
}
