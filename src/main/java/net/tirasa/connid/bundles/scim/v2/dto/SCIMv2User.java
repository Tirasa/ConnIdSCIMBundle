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
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.tirasa.connid.bundles.scim.common.dto.AbstractSCIMUser;
import net.tirasa.connid.bundles.scim.common.dto.ResourceReference;
import net.tirasa.connid.bundles.scim.common.dto.SCIMComplex;
import net.tirasa.connid.bundles.scim.common.dto.SCIMSchema;
import net.tirasa.connid.bundles.scim.v2.service.SCIMv2Service;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.objects.Attribute;

public class SCIMv2User extends AbstractSCIMUser<SCIMv2Attribute, ResourceReference, SCIMComplex<String>, SCIMv2Meta> {

    private static final long serialVersionUID = 7039988195599856857L;

    public static final String RESOURCE_NAME = "User";

    public static final String SCHEMA_URI = "urn:ietf:params:scim:schemas:core:2.0:User";

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
                SCIMv2Service.extractSCIMSchemas(customAttributesJSON, SCIMv2Attribute.class);
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
    protected void handleSCIMDefaultObject(final String value, final List list, final Consumer setter) {
        // DO NOTHING
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("active", active)
                .append("addresses", addresses)
                .append("displayName", displayName)
                .append("emails", emails)
                .append("entitlements", entitlements)
                .append("groups", groups)
                .append("ims", ims)
                .append("locale", locale)
                .append("name", name)
                .append("nickName", nickName)
                .append("phoneNumbers", phoneNumbers)
                .append("photos", photos)
                .append("profileUrl", profileUrl)
                .append("preferredLanguage", preferredLanguage)
                .append("roles", roles)
                .append("timezone", timezone)
                .append("title", title)
                .append("userName", userName)
                .append("userType", userType)
                .append("x509Certificates", x509Certificates)
                .append("scimCustomAttributes", scimCustomAttributes)
                .toString();
    }
}
