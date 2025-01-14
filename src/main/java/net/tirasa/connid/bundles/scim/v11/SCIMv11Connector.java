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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.tirasa.connid.bundles.scim.common.AbstractSCIMConnector;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.SCIMProvider;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseAttribute;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseResource;
import net.tirasa.connid.bundles.scim.common.dto.SCIMSchema;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11Attribute;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11BasePatch;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11EnterpriseUser;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11Group;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11GroupPatch;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11Meta;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11User;
import net.tirasa.connid.bundles.scim.v11.dto.Scimv11GroupPatchOperation;
import net.tirasa.connid.bundles.scim.v11.service.SCIMv11Client;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMPatchOperation;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.AttributeDelta;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.spi.ConnectorClass;

@ConnectorClass(displayNameKey = "SCIMv11Connector.connector.display", configurationClass =
        SCIMConnectorConfiguration.class)
public class SCIMv11Connector extends AbstractSCIMConnector<
        SCIMv11User, SCIMv11Group, SCIMBaseResource<SCIMv11Meta>, SCIMv11BasePatch, SCIMPatchOperation, SCIMv11Client, 
        SCIMv11EnterpriseUser.SCIMv11EnterpriseUserManager> {

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
                    configuration.getCustomAttributesJSON(), configuration.getManageComplexEntitlements(),
                    configuration.getUseColonOnExtensionAttributes(), SCIMv11Attribute.class);
        }
        return schema;
    }

    @Override
    public Set<AttributeDelta> updateDelta(
            final ObjectClass objectClass,
            final Uid uid,
            final Set<AttributeDelta> modifications,
            final OperationOptions options) {

        LOG.error("Update delta is not supported in version 1.1");
        throw new ConnectorException("Update delta is not supported in version 1.1");
    }

    @Override
    public SCIMv11Client getClient() {
        return client;
    }

    @Override
    protected <T extends SCIMBaseAttribute<T>> SCIMv11User buildNewUserEntity(
            final Optional<SCIMSchema<T>> customSchema) {

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
                buildMembersGroupPatch(Collections.singletonList(user), SCIMAttributeUtils.SCIM_ADD)));
        groupsToRemove.forEach(grp -> groupPatches.put(grp,
                buildMembersGroupPatch(Collections.singletonList(user), SCIMAttributeUtils.SCIM_REMOVE)));
    }

    @Override
    protected SCIMv11BasePatch buildMembersGroupPatch(final List<SCIMv11User> users, final String op) {
        return new SCIMv11GroupPatch.Builder().members(users.stream()
                .map(user -> new Scimv11GroupPatchOperation.Builder().operation(SCIMAttributeUtils.SCIM_ADD)
                .display(provider.equals(SCIMProvider.WSO2) ? user.getUserName() : user.getDisplayName())
                .value(user.getId())
                .build())
                .collect(Collectors.toList())).build();
    }

    @Override
    protected SCIMv11BasePatch buildPatchFromGroup(final SCIMv11Group group) {
        throw new IllegalArgumentException("Not implemented, yet");
    }

    @Override
    protected SCIMv11BasePatch buildUserPatch(
            final Set<AttributeDelta> modifications,
            final SCIMv11User currentUser,
            final boolean manageGroups) {

        throw new ConnectorException("Update delta is not supported in version 1.1");
    }

    @Override
    protected SCIMv11BasePatch buildGroupPatch(final Set<AttributeDelta> modifications) {
        throw new ConnectorException("Update delta is not supported in version 1.1");
    }

    @Override
    protected List<SCIMPatchOperation> buildPatchOperations(
            final AttributeDelta currentDelta,
            final SCIMBaseAttribute<?> attributeDefinition) {

        throw new ConnectorException("Update delta is not supported in version 1.1");
    }

    @Override
    protected List<SCIMPatchOperation> buildGroupPatchOperations(final Set<AttributeDelta> modifications) {
        throw new ConnectorException("Update delta is not supported in version 1.1");
    }

    @Override
    protected List<SCIMPatchOperation> buildAddressesPatchOperations(
            final Set<AttributeDelta> modifications,
            final SCIMv11User currentUser) {

        throw new ConnectorException("Update delta is not supported in version 1.1");
    }

    @Override
    protected void manageEntitlements(final SCIMv11User user, final List<String> values) {
        // in v11 only the default entitlement is supported
    }

    @Override
    protected SCIMv11EnterpriseUser.SCIMv11EnterpriseUserManager buildEnterpriseUserManager(final String value) {
        SCIMv11EnterpriseUser.SCIMv11EnterpriseUserManager manager =
                new SCIMv11EnterpriseUser.SCIMv11EnterpriseUserManager();
        if (value.contains(":")) {
            String[] splitValue = value.split(":");
            manager.setManagerId(splitValue[0]);
            manager.setManagerId(splitValue[1]);
        } else {
            manager.setDisplayName(value);
        }
        return manager;
    }
}
