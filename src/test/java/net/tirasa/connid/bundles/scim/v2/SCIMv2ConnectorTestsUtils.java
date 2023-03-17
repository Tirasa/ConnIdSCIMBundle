/**
 * Copyright © 2018 ConnId (connid-dev@googlegroups.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tirasa.connid.bundles.scim.v2;

import java.util.Map;
import java.util.Set;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Attribute;

public class SCIMv2ConnectorTestsUtils {

    private static final Log LOG = Log.getLog(SCIMv2ConnectorTestsUtils.class);

    public static final String VALUE_USERNAME = "ee026d943-4143-4043-8ef1-88994ef59ec4";

    public static final String VALUE_PASSWORD = "Password-01";

    public static final String VALUE_PHONE_NUMBER = "+31234567890";

    public static final String VALUE_FAMILY_NAME = "Family name";

    public static final String VALUE_GIVEN_NAME = "Given name";

    public static final String VALUE_NICK_NAME = "Nickname";

    public static final String VALUE_ROLE = "Test role";

    public static final String USER_ATTRIBUTE_FAMILY_NAME = "name.familyName";

    public static final String USER_ATTRIBUTE_NICK_NAME = "nickName";

    public static final String USER_ATTRIBUTE_EMAIL_WORK_VALUE = "emails.work.value";

    public static final String USER_ATTRIBUTE_ENTITLEMENTS_DEFAULT_VALUE = "entitlements.default.value";

    public static final String USER_ATTRIBUTE_ROLES_DEFAULT_VALUE = "roles.default.value";

    public static final String USER_ATTRIBUTE_PHONE_OTHER_VALUE = "phoneNumbers.other.value";
    public static final String USER_ATTRIBUTE_PHONE_HOME_VALUE = "phoneNumbers.home.value";

    public static final String USER_ATTRIBUTE_PHONE_OTHER_PRIMARY = "phoneNumbers.other.primary";
    public static final String USER_ATTRIBUTE_PHONE_HOME_PRIMARY = "phoneNumbers.home.primary";

    public static final String USER_ATTRIBUTE_PHONE_OTHER_OPERATION = "phoneNumbers.other.operation";

    public static final String USER_ATTRIBUTE_ADDRESS_WORK_STREET_ADDRESS = "addresses.work.streetAddress";

    public static SCIMConnectorConfiguration buildConfiguration(Map<String, String> configuration, int serverPort) {
        SCIMConnectorConfiguration connectorConfiguration = new SCIMConnectorConfiguration();

        for (Map.Entry<String, String> entry : configuration.entrySet()) {

            switch (entry.getKey()) {
                case "auth.baseAddress":
                    connectorConfiguration.setBaseAddress(entry.getValue());
                    connectorConfiguration.setBaseAddress("http://localhost:" + serverPort + "/v2/");
                    break;
                case "auth.password":
                    connectorConfiguration.setPassword(SCIMUtils.createProtectedPassword(entry.getValue()));
                    break;
                case "auth.username":
                    connectorConfiguration.setUsername(entry.getValue());
                    break;
                case "auth.contentType":
                    connectorConfiguration.setContentType(entry.getValue());
                    break;
                case "auth.accept":
                    connectorConfiguration.setAccept(entry.getValue());
                    break;
                case "auth.clientId":
                    connectorConfiguration.setCliendId(entry.getValue());
                    break;
                case "auth.clientSecret":
                    connectorConfiguration.setClientSecret(entry.getValue());
                    break;
                case "auth.accessTokenNodeId":
                    connectorConfiguration.setAccessTokenNodeId(entry.getValue());
                    break;
                case "auth.accessTokenBaseAddress":
                    connectorConfiguration.setAccessTokenBaseAddress(entry.getValue());
                    break;
                case "auth.accessTokenContentType":
                    connectorConfiguration.setAccessTokenContentType(entry.getValue());
                    break;
                case "auth.customAttributesJSON":
                    connectorConfiguration.setCustomAttributesJSON(entry.getValue());
                    break;
                case "auth.updateMethod":
                    connectorConfiguration.setUpdateMethod(entry.getValue());
                    break;
                default:
                    LOG.info("Occurrence of an non defined parameter");
                    break;
            }
        }
        return connectorConfiguration;
    }

    public static boolean isConfigurationValid(final SCIMConnectorConfiguration connectorConfiguration) {
        connectorConfiguration.validate();
        return true;
    }

    public static boolean hasAttribute(final Set<Attribute> attrs, final String name) {
        for (Attribute attr : attrs) {
            if (attr.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsAttribute(final Set<Attribute> attrs, final String name) {
        for (Attribute attr : attrs) {
            if (attr.getName().contains(name)) {
                return true;
            }
        }
        return false;
    }
}
