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
package net.tirasa.connid.bundles.scim.common;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.ws.rs.core.MediaType;
import net.tirasa.connid.bundles.scim.common.dto.SCIMSchema;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.common.security.SecurityUtil;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.framework.spi.StatefulConfiguration;

/**
 * Connector configuration class. It contains all the needed methods for
 * processing the connector configuration.
 */
public class SCIMConnectorConfiguration extends AbstractConfiguration implements StatefulConfiguration {

    private static final Log LOG = Log.getLog(SCIMConnectorConfiguration.class);

    private String username;

    private GuardedString password;

    private String bearerToken;

    private String baseAddress;

    private String customAttributesJSON;

    private String updateUserMethod = "PUT";

    private String updateGroupMethod = "PUT";

    private Boolean explicitGroupAddOnCreate = false;

    private String accept = MediaType.APPLICATION_JSON;

    private String contentType = MediaType.APPLICATION_JSON;

    private String clientId;

    private String clientSecret;

    private String accessTokenNodeId = "access_token";

    private String accessTokenBaseAddress;

    private String accessTokenContentType = MediaType.APPLICATION_FORM_URLENCODED;

    private String accessTokenAccept = MediaType.APPLICATION_JSON;

    private String addressesType;

    private String genericComplexType;

    private Boolean manageComplexEntitlements = false;

    private String scimProvider = SCIMProvider.STANDARD.name();

    @ConfigurationProperty(order = 1, displayMessageKey = "baseAddress.display", helpMessageKey = "baseAddress.help",
            required = true)
    public String getBaseAddress() {
        return baseAddress;
    }

    public void setBaseAddress(final String baseAddress) {
        this.baseAddress = baseAddress;
    }

    @ConfigurationProperty(displayMessageKey = "accept.display", helpMessageKey = "accept.help", order = 2,
            required = true)
    public String getAccept() {
        return accept;
    }

    public void setAccept(final String accept) {
        this.accept = accept;
    }

    @ConfigurationProperty(displayMessageKey = "contentType.display", helpMessageKey = "contentType.help", order = 3,
            required = true)
    public String getContentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    @ConfigurationProperty(displayMessageKey = "username.display", helpMessageKey = "username.help", order = 4)
    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    @ConfigurationProperty(displayMessageKey = "password.display", helpMessageKey = "password.help", order = 5,
            confidential = true)
    public GuardedString getPassword() {
        return password;
    }

    public void setPassword(final GuardedString password) {
        this.password = password;
    }

    @ConfigurationProperty(displayMessageKey = "bearerToken.display", helpMessageKey = "bearerToken.help", order = 6,
            confidential = true)
    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(final String bearerToken) {
        this.bearerToken = bearerToken;
    }

    @ConfigurationProperty(displayMessageKey = "clientId.display", helpMessageKey = "clientId.help", order = 7)
    public String getClientId() {
        return clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    @ConfigurationProperty(displayMessageKey = "clientSecret.display", helpMessageKey = "clientSecret.help", order = 8,
            confidential = true)
    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @ConfigurationProperty(displayMessageKey = "customAttributesJSON.display",
            helpMessageKey = "customAttributesJSON.help", order = 9)
    public String getCustomAttributesJSON() {
        return customAttributesJSON;
    }

    public void setCustomAttributesJSON(final String customAttributesJSON) {
        this.customAttributesJSON = customAttributesJSON;
    }

    @ConfigurationProperty(displayMessageKey = "updateUserMethod.display", helpMessageKey = "updateUserMethod.help",
            order = 10)
    public String getUpdateUserMethod() {
        return updateUserMethod;
    }

    public void setUpdateUserMethod(final String updateUserMethod) {
        this.updateUserMethod = updateUserMethod;
    }

    @ConfigurationProperty(displayMessageKey = "updateGroupMethod.display", helpMessageKey = "updateGroupMethod.help",
            order = 10)
    public String getUpdateGroupMethod() {
        return updateGroupMethod;
    }

    @ConfigurationProperty(displayMessageKey = "updateGroupMethod.display", helpMessageKey = "updateGroupMethod.help",
            order = 10)
    public void setUpdateGroupMethod(final String updateGroupMethod) {
        this.updateGroupMethod = updateGroupMethod;
    }

    @ConfigurationProperty(displayMessageKey = "explicitGroupAddOnCreate.display",
            helpMessageKey = "explicitGroupAddOnCreate.help", order = 11)
    public Boolean getExplicitGroupAddOnCreate() {
        return explicitGroupAddOnCreate;
    }

    public void setExplicitGroupAddOnCreate(final Boolean explicitGroupAddOnCreate) {
        this.explicitGroupAddOnCreate = explicitGroupAddOnCreate;
    }

    @ConfigurationProperty(displayMessageKey = "accessTokenNodeId.display",
            helpMessageKey = "accessTokenNodeId.help", order = 12)
    public String getAccessTokenNodeId() {
        return accessTokenNodeId;
    }

    public void setAccessTokenNodeId(final String accessTokenNodeId) {
        this.accessTokenNodeId = accessTokenNodeId;
    }

    @ConfigurationProperty(displayMessageKey = "accessTokenBaseAddress.display",
            helpMessageKey = "accessTokenBaseAddress.help", order = 13)
    public String getAccessTokenBaseAddress() {
        return accessTokenBaseAddress;
    }

    public void setAccessTokenBaseAddress(final String accessTokenBaseAddress) {
        this.accessTokenBaseAddress = accessTokenBaseAddress;
    }

    @ConfigurationProperty(displayMessageKey = "accessTokenContentType.display",
            helpMessageKey = "accessTokenContentType.help", order = 14)
    public String getAccessTokenContentType() {
        return accessTokenContentType;
    }

    public void setAccessTokenContentType(final String accessTokenContentType) {
        this.accessTokenContentType = accessTokenContentType;
    }

    @ConfigurationProperty(displayMessageKey = "accessTokenAccept.display",
            helpMessageKey = "accessTokenAccept.help", order = 15)
    public String getAccessTokenAccept() {
        return accessTokenAccept;
    }

    public void setAccessTokenAccept(final String accessTokenAccept) {
        this.accessTokenAccept = accessTokenAccept;
    }

    @ConfigurationProperty(displayMessageKey = "addressesType.display", helpMessageKey = "addressesType.help",
            order = 16)
    public String getAddressesType() {
        return addressesType;
    }

    public void setAddressesType(final String addressesType) {
        this.addressesType = addressesType;
    }

    @ConfigurationProperty(displayMessageKey = "genericComplexType.display", helpMessageKey = "genericComplexType.help",
            order = 17)
    public String getGenericComplexType() {
        return genericComplexType;
    }

    public void setManageComplexEntitlements(final Boolean manageComplexEntitlements) {
        this.manageComplexEntitlements = manageComplexEntitlements;
    }

    @ConfigurationProperty(displayMessageKey = "manageComplexEntitlements.display",
            helpMessageKey = "manageComplexEntitlements.help",
            order = 18)
    public Boolean getManageComplexEntitlements() {
        return manageComplexEntitlements;
    }

    public void setGenericComplexType(final String genericComplexType) {
        this.genericComplexType = genericComplexType;
    }

    @ConfigurationProperty(displayMessageKey = "scimProvider.display",
            helpMessageKey = "scimProvider.help",
            order = 19)
    public String getScimProvider() {
        return scimProvider;
    }

    public void setScimProvider(final String scimProvider) {
        this.scimProvider = scimProvider;
    }

    @Override
    public void validate() {
        if (StringUtil.isBlank(baseAddress)) {
            failValidation("Base address cannot be null or empty.");
        }
        try {
            new URL(baseAddress);
        } catch (MalformedURLException e) {
            LOG.error(e, "While validating baseAddress");
            failValidation("Base address must be a valid URL.");
        }
        if (StringUtil.isBlank(bearerToken)
                && StringUtil.isBlank(username)
                && StringUtil.isBlank(clientId)
                && StringUtil.isBlank(clientSecret)
                && StringUtil.isBlank(accessTokenNodeId)
                && StringUtil.isBlank(accessTokenBaseAddress)) {

            failValidation("Username cannot be null or empty since bearerToken, clientId, clientSecret, "
                    + "accessTokenNodeId and accessTokenBaseAddress are blank");
        }
        if (StringUtil.isBlank(bearerToken)
                && password != null && StringUtil.isBlank(SecurityUtil.decrypt(password))
                && StringUtil.isBlank(clientId)
                && StringUtil.isBlank(clientSecret)
                && StringUtil.isBlank(accessTokenNodeId)
                && StringUtil.isBlank(accessTokenBaseAddress)) {

            failValidation("Password cannot be null or empty since bearerToken, clientId, clientSecret, "
                    + "accessTokenNodeId and accessTokenBaseAddress are blank");
        }
        if (StringUtil.isNotBlank(customAttributesJSON)) {
            try {
                SCIMUtils.MAPPER.readValue(customAttributesJSON, SCIMSchema.class);
            } catch (IOException e) {
                LOG.error(e, "While validating customAttributesJSON");
                failValidation(
                        "'customAttributesJSON' parameter must be a valid " + "Resource Schema Representation JSON.");
            }
        }
        if (!"PATCH".equalsIgnoreCase(updateUserMethod) && !"PUT".equalsIgnoreCase(updateUserMethod)) {
            failValidation("Update method is not valid; must be 'PUT' or 'PATCH'.");
        }
        try {
            SCIMProvider.valueOf(scimProvider.toUpperCase());
        } catch (Exception e) {
            failValidation("Unsupported SCIM provider: " + scimProvider);
        }
    }

    @Override
    public void release() {
    }

    private void failValidation(final String key, final Object... args) {
        String message = getConnectorMessages().format(key, null, args);
        throw new ConfigurationException(message);
    }
}
