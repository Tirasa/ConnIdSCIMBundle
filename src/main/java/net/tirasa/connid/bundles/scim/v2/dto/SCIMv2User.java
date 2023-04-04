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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import net.tirasa.connid.bundles.scim.common.dto.AbstractSCIMUser;
import net.tirasa.connid.bundles.scim.common.dto.ResourceReference;
import net.tirasa.connid.bundles.scim.common.dto.SCIMGenericComplex;
import net.tirasa.connid.bundles.scim.common.dto.SCIMSchema;
import net.tirasa.connid.bundles.scim.common.service.AbstractSCIMService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeUtil;

public class SCIMv2User
        extends AbstractSCIMUser<SCIMv2Attribute, ResourceReference, SCIMGenericComplex<String>, SCIMv2Meta,
        SCIMv2EnterpriseUser> {

    private static final long serialVersionUID = 7039988195599856857L;

    public static final String RESOURCE_NAME = "User";

    public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:User";

    @JsonProperty(SCIMv2EnterpriseUser.SCHEMA_URI)
    protected SCIMv2EnterpriseUser enterpriseUser;

    public SCIMv2User() {
        super(SCHEMA_URI, RESOURCE_NAME, new SCIMv2Meta(RESOURCE_NAME));
    }

    @Override
    protected void handleRoles(final Object value) {
        handleSCIMComplexObject(
                String.class.cast(value),
                this.roles,
                s -> s.setValue(String.class.cast(value)));
    }

    @Override
    protected void handlex509Certificates(final Object value) {
        handleSCIMComplexObject(
                String.class.cast(value),
                this.x509Certificates,
                s -> s.setValue(String.class.cast(value)));
    }

    @Override
    protected void handleEntitlements(final Object value) {
        handleSCIMComplexObject(
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
    @Override
    @JsonIgnore
    public void fillSCIMCustomAttributes(final Set<Attribute> attributes, final String customAttributesJSON) {
        SCIMSchema<SCIMv2Attribute> customAttributesObj =
                AbstractSCIMService.extractSCIMSchemas(customAttributesJSON, SCIMv2Attribute.class);
        if (customAttributesObj != null) {
            for (Attribute attribute : attributes) {
                if (!CollectionUtil.isEmpty(attribute.getValue())) {
                    for (SCIMv2Attribute customAttribute : customAttributesObj.getAttributes()) {
                        String externalAttributeName = SCIMv2Attribute.class.cast(customAttribute).getExtensionSchema()
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

    @Override
    public SCIMv2EnterpriseUser getEnterpriseUser() {
        return enterpriseUser;
    }

    @Override
    public void setEnterpriseUser(final SCIMv2EnterpriseUser enterpriseUser) {
        this.enterpriseUser = enterpriseUser;
    }

    @Override
    public void fillEnterpriseUser(final Set<Attribute> attributes) {
        this.enterpriseUser = new SCIMv2EnterpriseUser();
        attributes.stream().filter(a -> a.getName().startsWith(SCIMv2EnterpriseUser.SCHEMA_URI)).forEach(
                a -> {
                    SCIMv2EnterpriseUser.SCIMv2EnterpriseUserManager manager = enterpriseUser.getManager();
                    switch (StringUtils.replace(a.getName(), SCIMv2EnterpriseUser.SCHEMA_URI + ".",
                            StringUtils.EMPTY)) {
                        case "employeeNumber":
                            enterpriseUser.setEmployeeNumber(AttributeUtil.getAsStringValue(a));
                            break;
                        case "costCenter":
                            enterpriseUser.setCostCenter(AttributeUtil.getAsStringValue(a));
                            break;
                        case "organization":
                            enterpriseUser.setOrganization(AttributeUtil.getAsStringValue(a));
                            break;
                        case "division":
                            enterpriseUser.setDivision(AttributeUtil.getAsStringValue(a));
                            break;
                        case "department":
                            enterpriseUser.setDepartment(AttributeUtil.getAsStringValue(a));
                            break;
                        case "manager.value":
                            if (manager == null) {
                                manager = new SCIMv2EnterpriseUser.SCIMv2EnterpriseUserManager();
                                enterpriseUser.setManager(manager);
                            }
                            manager.setValue(AttributeUtil.getAsStringValue(a));
                            break;
                        case "manager.displayName":
                            if (manager == null) {
                                manager = new SCIMv2EnterpriseUser.SCIMv2EnterpriseUserManager();
                                enterpriseUser.setManager(manager);
                            }
                            manager.setDisplayName(AttributeUtil.getAsStringValue(a));
                            break;
                        default:
                            // do nothing
                    }
                }
        );
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).build();
    }
}
