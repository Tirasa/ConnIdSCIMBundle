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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
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
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);

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
            final boolean useColon,
            final Class<T> attrType) {

        return cleanAttributesToGet(attributesToGet, customAttributesJSON, attrType, true, useColon);
    }

    public static <T extends SCIMBaseAttribute<T>> String cleanAttributesToGet(
            final Set<String> attributesToGet,
            final String customAttributesJSON,
            final Class<T> attrType,
            final boolean addCustomAttrsToQueryParams,
            final boolean useColon) {

        if (attributesToGet.isEmpty()) {
            return SCIMAttributeUtils.defaultAttributesToGet();
        }

        SCIMSchema<T> customAttributesObj = StringUtil.isBlank(customAttributesJSON)
                ? null
                : extractSCIMSchemas(customAttributesJSON, attrType).orElse(null);
        StringBuilder result = new StringBuilder();
        for (String attributeToGet : attributesToGet) {
            if (attributeToGet.contains("__")
                    || attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_META + ".")
                    || attributeToGet.toLowerCase().contains("password")) {
                // nothing to do
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_NAME + ".")) {
                result.append(SCIMAttributeUtils.SCIM_USER_NAME).append(",");
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_ADDRESSES + ".")) {
                result.append(SCIMAttributeUtils.SCIM_USER_ADDRESSES).append(",");
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_PHONE_NUMBERS + ".")) {
                result.append(SCIMAttributeUtils.SCIM_USER_PHONE_NUMBERS).append(",");
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_IMS + ".")) {
                result.append(SCIMAttributeUtils.SCIM_USER_IMS).append(",");
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_EMAILS + ".")) {
                result.append(SCIMAttributeUtils.SCIM_USER_EMAILS).append(",");
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_ROLES + ".")) {
                result.append(SCIMAttributeUtils.SCIM_USER_ROLES).append(",");
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_GROUPS + ".")) {
                result.append(SCIMAttributeUtils.SCIM_USER_GROUPS).append(",");
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_PHOTOS + ".")) {
                result.append(SCIMAttributeUtils.SCIM_USER_PHOTOS).append(",");
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_X509CERTIFICATES + ".")) {
                result.append(SCIMAttributeUtils.SCIM_USER_X509CERTIFICATES).append(",");
            } else if (attributeToGet.contains(SCIMAttributeUtils.SCIM_USER_ENTITLEMENTS + ".")) {
                result.append(SCIMAttributeUtils.SCIM_USER_ENTITLEMENTS).append(",");
            } else if (attributeToGet.startsWith(SCIMv2EnterpriseUser.SCHEMA_URI)) {
                result.append(SCIMv2EnterpriseUser.SCHEMA_URI)
                        .append(attributeToGet.replace(SCIMv2EnterpriseUser.SCHEMA_URI, StringUtil.EMPTY)).append(",");
            } else if (customAttributesObj == null) {
                result.append(attributeToGet).append(",");
            } else if (!isCustomAttribute(customAttributesObj, attributeToGet, useColon)) {
                result.append(attributeToGet).append(",");
            }
        }

        if (customAttributesObj != null && addCustomAttrsToQueryParams) {
            for (T attribute : customAttributesObj.getAttributes()) {
                String attributeName = attribute instanceof SCIMv2Attribute
                        ? SCIMv2Attribute.class.cast(attribute).getExtensionSchema() + (useColon ? ":" : ".")
                        + attribute.getName() : attribute.getName();
                if (!result.toString().contains(attributeName)) {
                    result.append(attributeName).append(",");
                }
            }
        }

        if (!result.toString().contains(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME + ",")) {
            result.append(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME).append(",");
        }
        if (!result.toString().contains(SCIMAttributeUtils.ATTRIBUTE_ID + ",")) {
            result.append(SCIMAttributeUtils.ATTRIBUTE_ID).append(",");
        }
        if (!result.toString().contains(SCIMAttributeUtils.SCIM_USER_NAME + ",")) {
            result.append(SCIMAttributeUtils.SCIM_USER_NAME).append(",");
        }

        return result.length() == 0
                ? SCIMAttributeUtils.defaultAttributesToGet()
                : result.substring(0, result.length() - 1);
    }

    private static <T extends SCIMBaseAttribute<T>> boolean isCustomAttribute(
            final SCIMSchema<T> customAttributes,
            final String attribute,
            final boolean useColon) {
        for (T customAttribute : customAttributes.getAttributes()) {
            String externalAttributeName = customAttribute instanceof SCIMv11Attribute
                    ? ((SCIMv11Attribute) customAttribute).getSchema()
                            .concat(useColon ? ":" : ".")
                            .concat(customAttribute.getName())
                    : ((SCIMv2Attribute) customAttribute).getExtensionSchema()
                            .concat(useColon ? ":" : ".")
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

    public static String getPath(final String id, final SCIMConnectorConfiguration config) {
        return config.getEnableURLPathEncoding() ? URLEncoder.encode(id, StandardCharsets.UTF_8) : id;
    }

    public static String getTypeFromAttributeName(final String attributeName) {
        if (StringUtil.isBlank(attributeName)) {
            return null;
        }
        Matcher matcher = Pattern.compile("^[^.]+\\.([^.]+)\\.[^.]+$").matcher(attributeName);
        return matcher.find() ? matcher.group(1) : null;
    }

    private SCIMUtils() {
        // private constructor for static utility class
    }
}
