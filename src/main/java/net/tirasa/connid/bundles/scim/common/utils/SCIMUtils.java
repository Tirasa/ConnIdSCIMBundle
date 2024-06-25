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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.tirasa.connid.bundles.scim.common.SCIMProvider;
import net.tirasa.connid.bundles.scim.common.dto.BaseResourceReference;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseAttribute;
import net.tirasa.connid.bundles.scim.common.dto.SCIMSchema;
import net.tirasa.connid.bundles.scim.common.dto.SCIMUser;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11Attribute;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Attribute;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2EnterpriseUser;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

public final class SCIMUtils {

    private static final Log LOG = Log.getLog(SCIMUtils.class);

    public static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    public static List<Field> getAllFieldsList(final Class<?> cls) {
        List<Field> allFields = new ArrayList<>();
        Class<?> currentClass = cls;
        while (currentClass != null) {
            Field[] declaredFields = currentClass.getDeclaredFields();
            Collections.addAll(allFields, declaredFields);
            currentClass = currentClass.getSuperclass();
        }
        return allFields;
    }

    public static void handleGeneralError(final String message) {
        LOG.error("General error : {0}", message);
        throw new ConnectorException(message);
    }

    public static void handleGeneralError(final String message, final Exception ex) {
        LOG.error(ex, message);
        throw new ConnectorException(message, ex);
    }

    public static void wrapGeneralError(final String message, final Exception ex) {
        LOG.error(ex, message);
        throw ConnectorException.wrap(ex);
    }

    public static boolean isEmptyObject(final Object obj) {
        return obj == null
                || (obj instanceof List ? new ArrayList<>((List<?>) obj).isEmpty() : false)
                || (obj instanceof String ? StringUtil.isBlank(String.class.cast(obj)) : false);
    }

    public static <T extends SCIMBaseAttribute<T>> String cleanAttributesToGet(
            final Set<String> attributesToGet,
            final String customAttributesJSON,
            final Class<T> attrType) {

        return cleanAttributesToGet(attributesToGet, customAttributesJSON, attrType, true);
    }

    public static <T extends SCIMBaseAttribute<T>> String cleanAttributesToGet(
            final Set<String> attributesToGet,
            final String customAttributesJSON,
            final Class<T> attrType,
            final boolean addCustomAttrsToQueryParams) {

        if (attributesToGet.isEmpty()) {
            return SCIMAttributeUtils.defaultAttributesToGet();
        }

        SCIMSchema<T> customAttributesObj = StringUtil.isBlank(customAttributesJSON)
                ? null
                : extractSCIMSchemas(customAttributesJSON, attrType).orElse(null);
        String result = "";
        for (String attributeToGet : attributesToGet) {
            if (attributeToGet.contains("__")
                    || attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_META + ".")
                    || attributeToGet.toLowerCase().contains("password")) {
                // nothing to do
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_NAME + ".")) {
                result += SCIMAttributeUtils.SCIM_USER_NAME.concat(",");
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_ADDRESSES + ".")) {
                result += SCIMAttributeUtils.SCIM_USER_ADDRESSES.concat(",");
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_PHONE_NUMBERS + ".")) {
                result += SCIMAttributeUtils.SCIM_USER_PHONE_NUMBERS.concat(",");
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_IMS + ".")) {
                result += SCIMAttributeUtils.SCIM_USER_IMS.concat(",");
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_EMAILS + ".")) {
                result += SCIMAttributeUtils.SCIM_USER_EMAILS.concat(",");
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_ROLES + ".")) {
                result += SCIMAttributeUtils.SCIM_USER_ROLES.concat(",");
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_GROUPS + ".")) {
                result += SCIMAttributeUtils.SCIM_USER_GROUPS.concat(",");
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_PHOTOS + ".")) {
                result += SCIMAttributeUtils.SCIM_USER_PHOTOS.concat(",");
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_X509CERTIFICATES + ".")) {
                result += SCIMAttributeUtils.SCIM_USER_X509CERTIFICATES.concat(",");
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_ENTITLEMENTS + ".")) {
                result += SCIMAttributeUtils.SCIM_USER_ENTITLEMENTS.concat(",");
            } else if (attributeToGet.startsWith(SCIMv2EnterpriseUser.SCHEMA_URI)) {
                result += SCIMv2EnterpriseUser.SCHEMA_URI
                        + (attributeToGet.replace(SCIMv2EnterpriseUser.SCHEMA_URI, StringUtil.EMPTY)
                                .replaceFirst(".", ":").concat(","));
            } else if (customAttributesObj == null) {
                result += attributeToGet.concat(",");
            } else if (!isCustomAttribute(customAttributesObj, attributeToGet)) {
                result += attributeToGet.concat(",");
            }
        }

        if (customAttributesObj != null && addCustomAttrsToQueryParams) {
            for (T attribute : customAttributesObj.getAttributes()) {
                String attributeName = attribute instanceof SCIMv2Attribute
                        ? SCIMv2Attribute.class.cast(attribute).getExtensionSchema() + ":" + attribute.getName()
                        : attribute.getName();
                if (!result.contains(attributeName)) {
                    result += attributeName.concat(",");
                }
            }
        }

        if (!result.contains(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME + ",")) {
            result += SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME.concat(",");
        }
        if (!result.contains(SCIMAttributeUtils.ATTRIBUTE_ID + ",")) {
            result += SCIMAttributeUtils.ATTRIBUTE_ID.concat(",");
        }
        if (!result.contains(SCIMAttributeUtils.SCIM_USER_NAME + ",")) {
            result += SCIMAttributeUtils.SCIM_USER_NAME.concat(",");
        }

        return StringUtil.isBlank(result)
                ? SCIMAttributeUtils.defaultAttributesToGet()
                : result.substring(0, result.length() - 1);
    }

    private static <T extends SCIMBaseAttribute<T>> boolean isCustomAttribute(
            final SCIMSchema<T> customAttributes,
            final String attribute) {
        for (T customAttribute : customAttributes.getAttributes()) {
            String externalAttributeName = customAttribute instanceof SCIMv11Attribute
                    ? SCIMv11Attribute.class.cast(customAttribute).getSchema()
                            .concat(".")
                            .concat(customAttribute.getName())
                    : SCIMv2Attribute.class.cast(customAttribute).getExtensionSchema()
                            .concat(".")
                            .concat(customAttribute.getName());
            if (externalAttributeName.equals(attribute)) {
                return true;
            }
        }
        return false;
    }

    public static <T extends SCIMBaseAttribute<T>> Optional<SCIMSchema<T>> extractSCIMSchemas(
            final String json, final Class<T> attrType) {

        if (StringUtil.isBlank(json)) {
            return Optional.empty();
        }

        try {
            SCIMSchema<T> scimSchema = SCIMUtils.MAPPER.readValue(json,
                    SCIMUtils.MAPPER.getTypeFactory().constructParametricType(SCIMSchema.class, attrType));
            // if SCIMv2Attribute populate transient field extensionSchema of the attribute since from SCIM 2.0 "schema"
            // attribute has been removed
            // refer to https://datatracker.ietf.org/doc/html/rfc7643#section-8.7.1
            if (SCIMv2Attribute.class.equals(attrType)) {
                scimSchema.getAttributes()
                        .forEach(attr -> SCIMv2Attribute.class.cast(attr).setExtensionSchema(scimSchema.getId()));
            }

            return scimSchema.getAttributes().isEmpty() ? Optional.empty() : Optional.of(scimSchema);
        } catch (IOException ex) {
            LOG.error(ex, "While parsing custom attributes JSON object, taken from connector configuration");
        }

        return Optional.empty();
    }

    public static <UT extends SCIMUser<?, ?>> BaseResourceReference buildGroupMember(
            final UT user, final SCIMProvider scimProvider) {

        BaseResourceReference.Builder groupMemberBuilder = new BaseResourceReference.Builder();
        switch (scimProvider) {
            case WSO2:
                groupMemberBuilder.value(user.getId()).display(user.getUserName());
                break;

            default:
                groupMemberBuilder.value(user.getId());
        }
        return groupMemberBuilder.build();
    }

    private SCIMUtils() {
        // private constructor for static utility class
    }
}
