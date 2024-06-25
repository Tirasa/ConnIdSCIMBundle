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
package net.tirasa.connid.bundles.scim.common.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.tirasa.connid.bundles.scim.common.AbstractSCIMConnector;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseAttribute;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11Attribute;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11EnterpriseUser;
import net.tirasa.connid.bundles.scim.v2.dto.Mutability;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Attribute;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2EnterpriseUser;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfo;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationalAttributeInfos;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;

public final class SCIMAttributeUtils {

    public static final String ATTRIBUTE_ID = "id";

    public static final String USER_ATTRIBUTE_USERNAME = "userName";

    public static final String USER_ATTRIBUTE_PASSWORD = "password";

    public static final String USER_ATTRIBUTE_ACTIVE = "active";

    public static final String ENTERPRISE_USER = "active";

    public static final String USER_ATTRIBUTE_EXTERNAL_ID = "externalId";

    public static final String SCIM_USER_NAME = "name";

    public static final String SCIM_USER_META = "meta";

    public static final String SCIM_USER_ADDRESSES = "addresses";

    public static final String SCIM_USER_PHONE_NUMBERS = "phoneNumbers";

    public static final String SCIM_USER_IMS = "ims";

    public static final String SCIM_USER_EMAILS = "emails";

    public static final String SCIM_USER_ROLES = "roles";

    public static final String SCIM_USER_ENTITLEMENTS = "entitlements";

    public static final String SCIM_USER_X509CERTIFICATES = "x509Certificates";

    public static final String SCIM_USER_GROUPS = "groups";

    public static final String SCIM_USER_GROUPS_TO_ADD = "groupsToAdd";

    public static final String SCIM_USER_GROUPS_TO_REMOVE = "groupsToRemove";

    public static final String SCIM11_REMOVE = "delete";

    public static final String SCIM_REMOVE = "remove";

    public static final String SCIM_ADD = "add";

    public static final String SCIM_REPLACE = "replace";

    public static final String SCIM_USER_PHOTOS = "photos";

    public static final String SCIM_USER_SCHEMAS = "schemas";

    public static final String SCIM_SCHEMA_TYPE_COMPLEX = "complex";
    
    public static final String SCIM_SCHEMA_TYPE_DEFAULT = "default";
    
    public static final String SCIM_SCHEMA_TYPE_PROFILE = "Profile";

    public static final String SCIM_SCHEMA_EXTENSION = "extension";

    public static final String SCIM_ENTERPRISE_EMPLOYEE_NUMBER = "employeeNumber";

    public static final String SCIM_ENTERPRISE_EMPLOYEE_MANAGER_VALUE = "manager.value";

    public static final String SCIM_ENTERPRISE_EMPLOYEE_MANAGER_REF = "manager.ref";

    public static final String SCIM_ENTERPRISE_EMPLOYEE_MANAGER_DISPLAY_NAME = "manager.displayName";

    public static final String SCIM_GROUP_DISPLAY_NAME = "displayName";

    public static final String SCIM_GROUP_MEMBERS = "members";

    public static <T extends SCIMBaseAttribute<T>> Schema buildSchema(
            final String customAttributes,
            final Boolean manageComplexEntitlements,
            final Class<T> attrType) {

        SchemaBuilder builder = new SchemaBuilder(AbstractSCIMConnector.class);

        ObjectClassInfoBuilder userBuilder = new ObjectClassInfoBuilder().setType(ObjectClass.ACCOUNT_NAME);
        ObjectClassInfo user;

        userBuilder.addAttributeInfo(Name.INFO);
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define(ATTRIBUTE_ID).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("externalId").setUpdateable(false).build());

        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("userName").setRequired(true).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("name.formatted").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("name.familyName").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("name.givenName").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("name.middleName").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("name.honorificPrefix").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("name.honorificSuffix").build());

        userBuilder.addAttributeInfo(OperationalAttributeInfos.ENABLE);
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("active").setType(Boolean.class).build());

        userBuilder.addAttributeInfo(OperationalAttributeInfos.PASSWORD);

        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("displayName").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("nickName").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("profileUrl").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("title").build());

        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("userType").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("locale").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("preferredLanguage").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("timezone").build());

        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("emails.work.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("emails.work.operation").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("emails.work.primary").setType(Boolean.class).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("emails.home.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("emails.home.operation").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("emails.home.primary").setType(Boolean.class).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("emails.other.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("emails.other.operation").build());
        userBuilder.addAttributeInfo(
                AttributeInfoBuilder.define("emails.other.primary").setType(Boolean.class).build());

        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("phoneNumbers.work.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("phoneNumbers.work.operation").build());
        userBuilder.addAttributeInfo(
                AttributeInfoBuilder.define("phoneNumbers.work.primary").setType(Boolean.class).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("phoneNumbers.other.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("phoneNumbers.other.operation").build());
        userBuilder.addAttributeInfo(
                AttributeInfoBuilder.define("phoneNumbers.other.primary").setType(Boolean.class).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("phoneNumbers.pager.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("phoneNumbers.pager.operation").build());
        userBuilder.addAttributeInfo(
                AttributeInfoBuilder.define("phoneNumbers.pager.primary").setType(Boolean.class).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("phoneNumbers.fax.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("phoneNumbers.fax.operation").build());
        userBuilder.addAttributeInfo(
                AttributeInfoBuilder.define("phoneNumbers.fax.primary").setType(Boolean.class).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("phoneNumbers.mobile.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("phoneNumbers.mobile.operation").build());
        userBuilder.addAttributeInfo(
                AttributeInfoBuilder.define("phoneNumbers.mobile.primary").setType(Boolean.class).build());

        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.aim.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.aim.operation").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.aim.primary").setType(Boolean.class).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.xmpp.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.xmpp.operation").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.xmpp.primary").setType(Boolean.class).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.skype.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.skype.operation").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.skype.primary").setType(Boolean.class).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.qq.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.qq.operation").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.qq.primary").setType(Boolean.class).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.yahoo.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.yahoo.operation").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.yahoo.primary").setType(Boolean.class).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.msn.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.msn.operation").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.msn.primary").setType(Boolean.class).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.icq.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.icq.operation").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.icq.primary").setType(Boolean.class).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.gtalk.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.gtalk.operation").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("ims.gtalk.primary").setType(Boolean.class).build());

        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("photos.photo.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("photos.photo.operation").build());
        userBuilder.addAttributeInfo(
                AttributeInfoBuilder.define("photos.photo.primary").setType(Boolean.class).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("photos.thumbnail.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("photos.thumbnail.operation").build());
        userBuilder.addAttributeInfo(
                AttributeInfoBuilder.define("photos.thumbnail.primary").setType(Boolean.class).build());

        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("addresses.work.streetAddress").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("addresses.work.locality").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("addresses.work.region").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("addresses.work.postalCode").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("addresses.work.country").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("addresses.work.formatted").build());
        userBuilder.addAttributeInfo(
                AttributeInfoBuilder.define("addresses.work.primary").setType(Boolean.class).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("addresses.home.streetAddress").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("addresses.home.locality").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("addresses.home.region").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("addresses.home.postalCode").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("addresses.home.country").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("addresses.home.formatted").build());
        userBuilder.addAttributeInfo(
                AttributeInfoBuilder.define("addresses.home.primary").setType(Boolean.class).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("addresses.other.streetAddress").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("addresses.other.locality").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("addresses.other.region").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("addresses.other.postalCode").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("addresses.other.country").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("addresses.other.formatted").build());
        userBuilder.addAttributeInfo(
                AttributeInfoBuilder.define("addresses.other.primary").setType(Boolean.class).build());

        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("groups.default.value").setUpdateable(false).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("roles.default.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("entitlements.default.value").build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("x509Certificates.default.value").build());

        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("schemas").setMultiValued(true).build());

        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("meta.created").setUpdateable(false).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("meta.lastModified").setUpdateable(false).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("meta.location").setUpdateable(false).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("meta.version").setUpdateable(false).build());
        userBuilder.addAttributeInfo(AttributeInfoBuilder.define("meta.attributes").setMultiValued(true).build());

        // custom attributes
        SCIMUtils.extractSCIMSchemas(customAttributes, attrType).ifPresent(scimSchema -> {
            for (T attribute : scimSchema.getAttributes()) {
                AttributeInfoBuilder attributeInfoBuilder = AttributeInfoBuilder.define(
                        attribute instanceof SCIMv11Attribute
                                ? SCIMv11Attribute.class.cast(attribute).getSchema()
                                        .concat(".").concat(attribute.getName())
                                : SCIMv2Attribute.class.cast(attribute).getExtensionSchema()
                                        .concat(".").concat(attribute.getName()));
                attributeInfoBuilder.setMultiValued(attribute.getMultiValued()).setRequired(attribute.getRequired())
                        .setUpdateable(attribute instanceof SCIMv11Attribute
                                ? !SCIMv11Attribute.class.cast(attribute).getReadOnly()
                                : Mutability.readWrite == SCIMv2Attribute.class.cast(attribute).getMutability());
                switch (attribute.getType()) {
                    case "boolean":
                        attributeInfoBuilder.setType(Boolean.class);
                        break;

                    case "string":
                    default:
                        attributeInfoBuilder.setType(String.class);
                }
                userBuilder.addAttributeInfo(attributeInfoBuilder.build());
            }
        });

        // enterprise user
        if (SCIMv2Attribute.class.equals(attrType)) {
            userBuilder.addAttributeInfo(
                    AttributeInfoBuilder.define(SCIMv2EnterpriseUser.SCHEMA_URI + ".employeeNumber")
                            .setMultiValued(false).build());
            userBuilder.addAttributeInfo(
                    AttributeInfoBuilder.define(SCIMv2EnterpriseUser.SCHEMA_URI + ".costCenter").setMultiValued(false)
                            .build());
            userBuilder.addAttributeInfo(
                    AttributeInfoBuilder.define(SCIMv2EnterpriseUser.SCHEMA_URI + ".organization").setMultiValued(false)
                            .build());
            userBuilder.addAttributeInfo(
                    AttributeInfoBuilder.define(SCIMv2EnterpriseUser.SCHEMA_URI + ".division").setMultiValued(false)
                            .build());
            userBuilder.addAttributeInfo(
                    AttributeInfoBuilder.define(SCIMv2EnterpriseUser.SCHEMA_URI + ".department").setMultiValued(false)
                            .build());
            userBuilder.addAttributeInfo(AttributeInfoBuilder.define(SCIMv2EnterpriseUser.SCHEMA_URI + ".manager.value")
                    .setMultiValued(false).build());
            userBuilder.addAttributeInfo(
                    AttributeInfoBuilder.define(SCIMv2EnterpriseUser.SCHEMA_URI + ".manager.ref").setMultiValued(false)
                            .build());
            userBuilder.addAttributeInfo(
                    AttributeInfoBuilder.define(SCIMv2EnterpriseUser.SCHEMA_URI + ".manager.displayName")
                            .setMultiValued(false).build());
            if (manageComplexEntitlements) {
                userBuilder.addAttributeInfo(
                        AttributeInfoBuilder.define(SCIMAttributeUtils.SCIM_USER_ENTITLEMENTS).setMultiValued(false)
                                .build());
            }
        } else {
            userBuilder.addAttributeInfo(
                    AttributeInfoBuilder.define(SCIMv11EnterpriseUser.SCHEMA_URI + ".employeeNumber")
                            .setMultiValued(false).build());
            userBuilder.addAttributeInfo(
                    AttributeInfoBuilder.define(SCIMv11EnterpriseUser.SCHEMA_URI + ".manager.managerId")
                            .setMultiValued(false).build());
            userBuilder.addAttributeInfo(
                    AttributeInfoBuilder.define(SCIMv11EnterpriseUser.SCHEMA_URI + ".manager.displayName")
                            .setMultiValued(false).build());
        }

        user = userBuilder.build();
        builder.defineObjectClass(user);

        // Group
        ObjectClassInfoBuilder groupBuilder = new ObjectClassInfoBuilder().setType(ObjectClass.GROUP_NAME);
        groupBuilder.addAttributeInfo(
                AttributeInfoBuilder.define(SCIMAttributeUtils.SCIM_GROUP_DISPLAY_NAME).setMultiValued(false).build());
        builder.defineObjectClass(groupBuilder.build());

        return builder.build();
    }

    public static AttributeBuilder buildAttributeFromClassField(final Field field, final Object that)
            throws IllegalArgumentException, IllegalAccessException {

        return doBuildAttributeFromClassField(field.get(that), field.getName(), field.getType());
    }

    public static AttributeBuilder doBuildAttributeFromClassField(final Object value, final String name,
            final Class<?> clazz) {

        AttributeBuilder attributeBuilder = new AttributeBuilder();
        if (value != null) {
            if (clazz == boolean.class || clazz == Boolean.class) {
                attributeBuilder.addValue(Boolean.class.cast(value));
            } else if (value instanceof List<?>) {
                ArrayList<?> list = new ArrayList<>((List<?>) value);
                if (list.size() > 1) {
                    for (Object elem : list) {
                        attributeBuilder.addValue(
                            doBuildAttributeFromClassField(elem, name, elem.getClass()).getValue());
                    }
                } else if (!list.isEmpty() && list.get(0) != null) {
                    attributeBuilder.addValue(list.get(0).toString());
                }
            } else {
                attributeBuilder.addValue(value.toString());
            }
        }
        if (name != null) {
            attributeBuilder.setName(name);
        }
        return attributeBuilder;
    }

    public static String defaultAttributesToGet() {
        return USER_ATTRIBUTE_USERNAME.concat(",").concat(ATTRIBUTE_ID).concat(",").concat(SCIM_USER_NAME);
    }

    public static void addAttribute(final Set<Attribute> toAttrs, final Set<Attribute> attrs, final Class<?> type) {
        for (Attribute toAttribute : toAttrs) {
            attrs.add(SCIMAttributeUtils.doBuildAttributeFromClassField(toAttribute.getValue(), toAttribute.getName(),
                    type).build());
        }
    }

    private SCIMAttributeUtils() {
        // private constructor for static utility class
    }
}
