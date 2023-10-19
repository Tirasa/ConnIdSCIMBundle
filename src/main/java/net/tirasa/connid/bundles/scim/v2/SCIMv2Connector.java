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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.tirasa.connid.bundles.scim.common.AbstractSCIMConnector;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.ScimProvider;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Attribute;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Entitlement;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2EntitlementResource;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Group;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Patch;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2PatchImpl;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2PatchOperation;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2PatchValue;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2User;
import net.tirasa.connid.bundles.scim.v2.service.SCIMv2Client;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.spi.ConnectorClass;

@ConnectorClass(displayNameKey = "SCIMv2Connector.connector.display",
        configurationClass = SCIMConnectorConfiguration.class,
        messageCatalogPaths = { "net.tirasa.connid.bundles.scim.common.Messages" })
public class SCIMv2Connector extends AbstractSCIMConnector<
        SCIMv2User, SCIMv2Group, SCIMv2EntitlementResource, SCIMv2Patch, SCIMv2Client> {

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
                    configuration.getManageComplexEntitlements(), SCIMv2Attribute.class);
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void fillGroupPatches(final SCIMv2User user, final Map<String, SCIMv2Patch> groupPatches,
            final List<String> groupsToAdd, final List<String> groupsToRemove) {

        // due to the deviation of salesforce from the standard we need to manage the members patch accordingly
        // https://help.salesforce.com/s/articleView?id=sf.identity_scim_manage_groups.htm&type=5
        groupsToAdd.forEach(grp -> groupPatches.put(grp, new SCIMv2PatchImpl.Builder().operations(CollectionUtil.newSet(
                ScimProvider.SALESFORCE == ScimProvider.valueOf(configuration.getScimProvider().toUpperCase())
                ? new SCIMv2PatchOperation.Builder().op(SCIMAttributeUtils.SCIM2_ADD)
                        .path(SCIMAttributeUtils.SCIM_GROUP_MEMBERS)
                        .value(CollectionUtil.newMap("members",
                                CollectionUtil.newList(new SCIMv2PatchValue.Builder().value(user.getId()).build())))
                        .build()
                : new SCIMv2PatchOperation.Builder().op(SCIMAttributeUtils.SCIM2_ADD)
                        .path(SCIMAttributeUtils.SCIM_GROUP_MEMBERS)
                        .value(CollectionUtil.newList(new SCIMv2PatchValue.Builder().value(user.getId()).build()))
                        .build())).build()));
        groupsToRemove.forEach(grp -> groupPatches.put(grp, new SCIMv2PatchImpl.Builder().operations(
                CollectionUtil.newSet(configuration.getBaseAddress().contains("salesforce.com")
                        ? new SCIMv2PatchOperation.Builder().op(SCIMAttributeUtils.SCIM2_REMOVE)
                                .path(SCIMAttributeUtils.SCIM_GROUP_MEMBERS)
                                .value(CollectionUtil.newMap(
                                        SCIMAttributeUtils.SCIM_GROUP_MEMBERS,
                                        CollectionUtil.newList(
                                                new SCIMv2PatchValue.Builder().value(user.getId()).build())))
                                .build()
                        : new SCIMv2PatchOperation.Builder().op(SCIMAttributeUtils.SCIM2_REMOVE)
                                .path(SCIMAttributeUtils.SCIM_GROUP_MEMBERS)
                                // in some cases is needed to
                                // append "[value eq \"" + user.getId() + "\"]" to retrieve the user
                                .value(CollectionUtil.newList(
                                        new SCIMv2PatchValue.Builder().value(user.getId()).build()))
                                .build()))
                .build()));
    }

    @Override
    protected void manageEntitlements(final SCIMv2User user, final List<String> values) {
        List<SCIMv2EntitlementResource> scimEntitlementRefs = values == null
                ? Collections.emptyList()
                : values.stream().map(client::getEntitlement).filter(g -> g != null).collect(Collectors.toList());
        scimEntitlementRefs.forEach(e -> user.getEntitlements().add(new SCIMv2Entitlement.Builder().value(e.getId())
                .ref(configuration.getBaseAddress() + "Entitlements/" + e.getId()).display(e.getDisplayName())
                .primary(true).type(e.getType()).build()));
    }
}
