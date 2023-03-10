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

import java.util.Collections;
import java.util.Set;
import net.tirasa.connid.bundles.scim.common.AbstractSCIMConnector;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Attribute;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2User;
import net.tirasa.connid.bundles.scim.v2.service.SCIMv2Client;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConnectorClass;

@ConnectorClass(displayNameKey = "SCIMv2Connector.connector.display",
        configurationClass = SCIMv2ConnectorConfiguration.class)
public class SCIMv2Connector extends AbstractSCIMConnector<SCIMv2User, SCIMv2Client> {

    private static final Log LOG = Log.getLog(SCIMv2Connector.class);

    private SCIMv2ConnectorConfiguration configuration;

    private Schema schema;

    private SCIMv2Client client;

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void init(Configuration configuration) {
        LOG.ok("Init");

        this.configuration = (SCIMv2ConnectorConfiguration) configuration;
        this.configuration.validate();

        client = new SCIMv2Client(this.configuration);

        LOG.ok("Connector {0} successfully inited", getClass().getName());
    }

    @Override
    public void dispose() {
        LOG.ok("Configuration cleanup");

        configuration = null;
    }

    @Override
    public void test() {
        LOG.ok("Connector TEST");

        if (configuration != null) {
            if (client != null && client.testService()) {
                LOG.ok("Test was successfull");
            } else {
                SCIMUtils.<SCIMv2Attribute>handleGeneralError("Test error. Problems with client service");
            }
        } else {
            LOG.error("Test error. No instance of the configuration class");
        }
    }

    @Override
    public Schema schema() {
        LOG.ok("Building SCHEMA definition");

        if (schema == null) {
            schema = SCIMAttributeUtils.<SCIMv2Attribute>buildSchema(configuration.getCustomAttributesJSON(),
                    SCIMv2Attribute.class);
        }
        return schema;
    }

    @Override
    public FilterTranslator<Filter> createFilterTranslator(
            final ObjectClass objectClass,
            final OperationOptions options) {
        return filter -> Collections.singletonList(filter);
    }

    @Override
    public Uid create(ObjectClass objectClass, Set<Attribute> createAttributes, OperationOptions options) {
        LOG.ok("Connector CREATE");
        return null;
    }

    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
        LOG.ok("Connector DELETE");

    }

    @Override
    public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> replaceAttributes, OperationOptions options) {
        LOG.ok("Connector UPDATE");
        return null;
    }

    public SCIMv2Client getClient() {
        return client;
    }

    @Override
    protected ConnectorObject fromUser(final SCIMv2User user, final Set<String> attributesToGet) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setObjectClass(ObjectClass.ACCOUNT);
        builder.setUid(user.getId());
        builder.setName(user.getUserName());

        try {
            Set<Attribute> userAttributes = user.toAttributes();

            for (Attribute toAttribute : userAttributes) {
                String attributeName = toAttribute.getName();
                for (String attributeToGetName : attributesToGet) {
                    if (attributeName.equals(attributeToGetName)) {
                        builder.addAttribute(toAttribute);
                        break;
                    }
                }
            }

            // custom attributes
            if (StringUtil.isNotBlank(configuration.getCustomAttributesJSON())) {
                for (String customAttributeKey : user.getReturnedCustomAttributes().keySet()) {
                    builder.addAttribute(customAttributeKey,
                            user.getReturnedCustomAttributes().get(customAttributeKey));
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            LOG.error(ex, "While converting to attributes");
        }

        return builder.build();
    }

}
