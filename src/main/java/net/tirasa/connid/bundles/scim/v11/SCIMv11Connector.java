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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.tirasa.connid.bundles.scim.common.AbstractSCIMConnector;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseAttribute;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseResource;
import net.tirasa.connid.bundles.scim.common.dto.SCIMSchema;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11Attribute;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11BasePatch;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11Group;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11GroupPatch;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11Meta;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11User;
import net.tirasa.connid.bundles.scim.v11.dto.Scimv11GroupPatchOperation;
import net.tirasa.connid.bundles.scim.v11.service.SCIMv11Client;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.spi.ConnectorClass;

@ConnectorClass(displayNameKey = "SCIMv11Connector.connector.display", configurationClass =
        SCIMConnectorConfiguration.class)
public class SCIMv11Connector extends AbstractSCIMConnector<
        SCIMv11User, SCIMv11Group, SCIMBaseResource<SCIMv11Meta>, SCIMv11BasePatch, SCIMv11Client> {

    private Schema schema;

    @Override
    protected SCIMv11Client buildSCIMClient(final SCIMConnectorConfiguration configuration) {
        return new SCIMv11Client(configuration);
    }

    @Override
    public Schema schema() {
        LOG.ok("Building SCHEMA definition");

        if (schema == null) {
            schema = SCIMAttributeUtils.<SCIMv11Attribute>buildSchema(
                    configuration.getCustomAttributesJSON(),
                    configuration.getManageComplexEntitlements(), SCIMv11Attribute.class);
        }
        return schema;
    }

    @Override
    public SCIMv11Client getClient() {
        return client;
    }

    @Override
    protected <T extends SCIMBaseAttribute<T>> SCIMv11User buildNewUserEntity(
            final Optional<SCIMSchema<T>> customSchema) {
        // SCIM-16 add also custom schemas
        SCIMv11User newUser = new SCIMv11User();
        customSchema.ifPresent(cs -> newUser.getSchemas().add(cs.getId()));
        return newUser;
    }

    @Override
    protected SCIMv11Group buildNewGroupEntity() {
        return new SCIMv11Group();
    }

    @Override
    protected void fillGroupPatches(
            final SCIMv11User user,
            final Map<String, SCIMv11BasePatch> groupPatches,
            final List<String> groupsToAdd,
            final List<String> groupsToRemove) {

        // on group add SCIM v1.1 omits the operation 
        groupsToAdd.forEach(grp -> groupPatches.put(grp,
                buildMemberGroupPatch(user, SCIMAttributeUtils.SCIM2_ADD)));
        groupsToRemove.forEach(grp -> groupPatches.put(grp,
                buildMemberGroupPatch(user, SCIMAttributeUtils.SCIM2_REMOVE)));
    }

    @Override
    protected SCIMv11BasePatch buildMemberGroupPatch(final SCIMv11User user, final String op) {
        return new SCIMv11GroupPatch.Builder().members(
                CollectionUtil.newList(new Scimv11GroupPatchOperation.Builder().
                        operation(SCIMAttributeUtils.SCIM2_ADD).
                        display(user.getDisplayName()).value(user.getId()).build())).build();
    }

    @Override
    protected SCIMv11BasePatch buildPatchFromGroup(final SCIMv11Group group) {
        throw new IllegalArgumentException("Not implemented, yet");
    }

    @Override
    protected void manageEntitlements(final SCIMv11User user, final List<String> values) {
        // in v11 only the default entitlement is supported
    }
}
