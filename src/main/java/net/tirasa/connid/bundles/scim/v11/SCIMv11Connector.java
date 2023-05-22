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
package net.tirasa.connid.bundles.scim.v11;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.tirasa.connid.bundles.scim.common.AbstractSCIMConnector;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11Attribute;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11BasePatch;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11Group;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11GroupPatch;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11User;
import net.tirasa.connid.bundles.scim.v11.dto.Scimv11GroupPatchOperation;
import net.tirasa.connid.bundles.scim.v11.service.SCIMv11Client;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.spi.ConnectorClass;

@ConnectorClass(displayNameKey = "SCIMv11Connector.connector.display", configurationClass =
        SCIMConnectorConfiguration.class)
public class SCIMv11Connector
        extends AbstractSCIMConnector<SCIMv11User, SCIMv11Group, SCIMv11BasePatch, SCIMv11Client> {

    private static final Log LOG = Log.getLog(SCIMv11Connector.class);

    private Schema schema;

    @Override protected SCIMv11Client buildSCIMClient(final SCIMConnectorConfiguration configuration) {
        return new SCIMv11Client(configuration);
    }

    @Override public Schema schema() {
        LOG.ok("Building SCHEMA definition");

        if (schema == null) {
            schema = SCIMAttributeUtils.<SCIMv11Attribute>buildSchema(configuration.getCustomAttributesJSON(),
                    SCIMv11Attribute.class);
        }
        return schema;
    }

    @Override public SCIMv11Client getClient() {
        return client;
    }

    @Override protected SCIMv11User buildNewUserEntity() {
        return new SCIMv11User();
    }

    @Override protected SCIMv11Group buildNewGroupEntity() {
        return new SCIMv11Group();
    }

    @Override protected void fillGroupPatches(final SCIMv11User user, final Map<String, SCIMv11BasePatch> groupPatches,
            final List<String> groupsToAdd, final List<String> groupsToRemove) {
        // on group add SCIM v1.1 omits the operation 
        groupsToAdd.forEach(grp -> groupPatches.put(grp, new SCIMv11GroupPatch.Builder().members(
                Collections.singletonList(
                        new Scimv11GroupPatchOperation.Builder().display(user.getDisplayName()).value(user.getId())
                                .build())).build()));
        groupsToRemove.forEach(grp -> groupPatches.put(grp, new SCIMv11GroupPatch.Builder().members(
                Collections.singletonList(
                        new Scimv11GroupPatchOperation.Builder().operation(SCIMAttributeUtils.SCIM11_REMOVE)
                                .display(user.getDisplayName()).value(user.getId()).build())).build()));
    }

}
