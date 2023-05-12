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
package net.tirasa.connid.bundles.scim.v2;

import net.tirasa.connid.bundles.scim.common.AbstractSCIMConnector;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Attribute;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Group;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2User;
import net.tirasa.connid.bundles.scim.v2.service.SCIMv2Client;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.spi.ConnectorClass;

@ConnectorClass(displayNameKey = "SCIMv2Connector.connector.display",
        configurationClass = SCIMConnectorConfiguration.class,
        messageCatalogPaths = {"net.tirasa.connid.bundles.scim.common.Messages"})
public class SCIMv2Connector extends AbstractSCIMConnector<SCIMv2User, SCIMv2Group, SCIMv2Client> {

    private static final Log LOG = Log.getLog(SCIMv2Connector.class);

    private Schema schema;

    @Override
    protected SCIMv2Client buildSCIMClient(final SCIMConnectorConfiguration configuration) {
        return new SCIMv2Client(configuration);
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
    public SCIMv2Client getClient() {
        return client;
    }

    @Override
    protected SCIMv2User buildNewUserEntity() {
        return new SCIMv2User();
    }

    @Override
    protected SCIMv2Group buildNewGroupEntity() {
        return new SCIMv2Group();
    }

}
