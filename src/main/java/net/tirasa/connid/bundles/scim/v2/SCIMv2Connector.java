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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.tirasa.connid.bundles.scim.common.AbstractSCIMConnector;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.SCIMProvider;
import net.tirasa.connid.bundles.scim.common.dto.BaseResourceReference;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseAttribute;
import net.tirasa.connid.bundles.scim.common.dto.SCIMSchema;
import net.tirasa.connid.bundles.scim.common.dto.SCIMUserAddress;
import net.tirasa.connid.bundles.scim.common.types.AddressCanonicalType;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Attribute;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2EnterpriseUser;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Entitlement;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2EntitlementResource;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Group;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Patch;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2PatchImpl;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2PatchOperation;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2User;
import net.tirasa.connid.bundles.scim.v2.service.SCIMv2Client;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.common.security.SecurityUtil;
import org.identityconnectors.framework.common.objects.AttributeDelta;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.spi.ConnectorClass;

@ConnectorClass(displayNameKey = "SCIMv2Connector.connector.display",
        configurationClass = SCIMConnectorConfiguration.class,
        messageCatalogPaths = { "net.tirasa.connid.bundles.scim.common.Messages" })
public class SCIMv2Connector extends AbstractSCIMConnector<
        SCIMv2User, SCIMv2Group, SCIMv2EntitlementResource, SCIMv2Patch, SCIMv2PatchOperation, SCIMv2Client,
        SCIMv2EnterpriseUser.SCIMv2EnterpriseUserManager> {

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
    protected <T extends SCIMBaseAttribute<T>> SCIMv2User buildNewUserEntity(
            final Optional<SCIMSchema<T>> customSchema) {

        SCIMv2User newUser = new SCIMv2User();
        customSchema.ifPresent(cs -> newUser.getSchemas().add(cs.getId()));
        return newUser;
    }

    @Override
    protected SCIMv2Group buildNewGroupEntity() {
        return new SCIMv2Group();
    }

    @Override
    protected SCIMv2Patch buildMembersGroupPatch(final List<SCIMv2User> users, final String op) {
        SCIMv2PatchImpl.Builder builder = new SCIMv2PatchImpl.Builder();

        // due to the deviation of salesforce and WSO2 from the standard we need to adjust the members patch
        switch (provider) {
            // https://help.salesforce.com/s/articleView?id=sf.identity_scim_manage_groups.htm&type=5
            case SALESFORCE:
            case WSO2:
                builder.operations(CollectionUtil.newSet(new SCIMv2PatchOperation.Builder()
                        .op(op)
                        .path(SCIMProvider.SALESFORCE == provider ? SCIMAttributeUtils.SCIM_GROUP_MEMBERS : null)
                        .value(CollectionUtil.newMap(
                                SCIMAttributeUtils.SCIM_GROUP_MEMBERS,
                                users.stream().map(user -> buildPatchValue(user)).collect(Collectors.toList())))
                        .build()));
                break;

            default:
                builder.operations(CollectionUtil.newSet(new SCIMv2PatchOperation.Builder()
                        .op(op)
                        .path(SCIMAttributeUtils.SCIM_GROUP_MEMBERS)
                        // sometimes it is needed to append "[value eq \"" + user.getId() + "\"]" to retrieve the user
                        .value(users.stream().map(user -> buildPatchValue(user)).collect(Collectors.toList()))
                        .build()));
        }
        return builder.build();
    }

    @Override
    protected void fillGroupPatches(
            final SCIMv2User user,
            final Map<String, SCIMv2Patch> groupPatches,
            final List<String> groupsToAdd,
            final List<String> groupsToRemove) {

        groupsToAdd.forEach(grp -> groupPatches.put(grp,
                buildMembersGroupPatch(Collections.singletonList(user), SCIMAttributeUtils.SCIM_ADD)));
        groupsToRemove.forEach(grp -> groupPatches.put(grp,
                buildMembersGroupPatch(Collections.singletonList(user), SCIMAttributeUtils.SCIM_REMOVE)));
    }

    @Override
    protected SCIMv2Patch buildPatchFromGroup(final SCIMv2Group group) {
        // these information must not be included for some providers like AWS
        if (SCIMProvider.AWS == provider) {
            group.getMembers().clear();
        }
        if (SCIMProvider.AWS == provider || SCIMProvider.EGNYTE == provider) {
            group.getSchemas().clear();
            group.setMeta(null);
        }
        return new SCIMv2PatchImpl.Builder().operations(Collections.singleton(
                new SCIMv2PatchOperation.Builder()
                        .op(SCIMAttributeUtils.SCIM_REPLACE)
                        .value(group).build())).build();
    }

    @Override
    protected SCIMv2Patch buildUserPatch(
            final Set<AttributeDelta> modifications,
            final SCIMv2User currentUser,
            final boolean manageGroups) {

        SCIMv2Patch patch = new SCIMv2PatchImpl.Builder().build();
        for (AttributeDelta attrDelta : modifications) {
            if (attrDelta.is(Uid.NAME)) {
                throw new IllegalArgumentException(
                        "Changing the id attribute is not supported, nor recommended with patch");
            }

            if (attrDelta.is(Name.NAME) && !CollectionUtil.isEmpty(attrDelta.getValuesToReplace())) {
                patch.addOperation(new SCIMv2PatchOperation.Builder()
                        .op(SCIMAttributeUtils.SCIM_REPLACE)
                        .path(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME)
                        .value(attrDelta.getValuesToReplace().get(0))
                        .build());
            } else if (attrDelta.is(OperationalAttributes.PASSWORD_NAME)
                    && !CollectionUtil.isEmpty(attrDelta.getValuesToReplace())) {

                patch.addOperation(new SCIMv2PatchOperation.Builder()
                        .op(SCIMAttributeUtils.SCIM_REPLACE)
                        .path(SCIMAttributeUtils.USER_ATTRIBUTE_PASSWORD)
                        .value(SecurityUtil.decrypt((GuardedString) attrDelta.getValuesToReplace().get(0)))
                        .build());
            } else if (!attrDelta.getName().contains(SCIMAttributeUtils.SCIM_USER_ADDRESSES)
                    && !attrDelta.is(SCIMAttributeUtils.SCIM_USER_GROUPS)) {

                patch.addOperations(buildPatchOperations(attrDelta, null));
            }
        }
        // manage addresses patches
        buildAddressesPatchOperations(modifications, currentUser).forEach(patch::addOperation);

        // custom attributes
        if (StringUtil.isNotBlank(configuration.getCustomAttributesJSON())) {
            buildCustomAttributesPatchOperations(
                    modifications,
                    configuration.getUseColonOnExtensionAttributes()).
                    forEach(patch::addOperation);
        }
        // manage groups
        if (manageGroups) {
            buildGroupPatchOperations(modifications).forEach(patch::addOperation);
        }
        return patch;
    }

    @Override
    protected SCIMv2Patch buildGroupPatch(final Set<AttributeDelta> modifications) {
        SCIMv2Patch patch = new SCIMv2PatchImpl.Builder().build();
        for (AttributeDelta attrDelta : modifications) {
            if (attrDelta.is(Uid.NAME)) {
                throw new IllegalArgumentException(
                        "Changing the id attribute is not supported, nor recommended with patch");
            }

            if (attrDelta.is(Name.NAME) && !CollectionUtil.isEmpty(attrDelta.getValuesToReplace())) {
                patch.addOperation(new SCIMv2PatchOperation.Builder()
                        .op(SCIMAttributeUtils.SCIM_REPLACE)
                        .path(SCIMAttributeUtils.SCIM_GROUP_DISPLAY_NAME)
                        .value(attrDelta.getValuesToReplace().get(0))
                        .build());
            } else if (!attrDelta.getName().contains(SCIMAttributeUtils.SCIM_GROUP_MEMBERS)) {
                patch.addOperations(buildPatchOperations(attrDelta, null));
            }
        }

        // manage members, only values to add and remove are supported
        List<SCIMv2PatchOperation> memberOperations = new ArrayList<>();
        modifications.stream().filter(mod -> SCIMAttributeUtils.SCIM_GROUP_MEMBERS.equalsIgnoreCase(mod.getName()))
                .findFirst().ifPresent(mod -> {
                    // remove ops
                    if (!CollectionUtil.isEmpty(mod.getValuesToRemove())) {
                        memberOperations.add(new SCIMv2PatchOperation.Builder()
                                .op(SCIMAttributeUtils.SCIM_REMOVE)
                                .path(buildFilteredPath(mod.getName(), mod.getValuesToRemove(), "or", "eq"))
                                .build());
                    }
                    // add ops
                    if (!CollectionUtil.isEmpty(mod.getValuesToAdd())) {
                        memberOperations.add(new SCIMv2PatchOperation.Builder()
                                .op(SCIMAttributeUtils.SCIM_ADD)
                                .path(SCIMAttributeUtils.SCIM_GROUP_MEMBERS)
                                .value(mod.getValuesToAdd().stream().map(vta -> {
                                    SCIMv2User user = client.getUser(vta.toString());
                                    BaseResourceReference resRef = null;
                                    if (user == null) {
                                        LOG.error("Unable to add member {0} to the group, user does not exist", vta);
                                    } else {
                                        resRef = new BaseResourceReference.Builder().value(user.getId())
                                                .ref(configuration.getBaseAddress() + "Users/" + user.getId())
                                                .display(user.getDisplayName()).build();
                                    }
                                    return resRef;
                                }).filter(Objects::nonNull).collect(Collectors.toList()))
                                .build());
                    }
                    // replace ops
                    if (!CollectionUtil.isEmpty(mod.getValuesToReplace())) {
                        memberOperations.add(new SCIMv2PatchOperation.Builder()
                                .op(SCIMAttributeUtils.SCIM_REPLACE)
                                .path(SCIMAttributeUtils.SCIM_GROUP_MEMBERS)
                                .value(mod.getValuesToReplace().stream().map(vtr -> {
                                    SCIMv2User user = client.getUser(vtr.toString());
                                    BaseResourceReference resRef = null;
                                    if (user == null) {
                                        LOG.error(
                                                "Unable to replace member {0} on the group, user does not exist",
                                                vtr);
                                    } else {
                                        resRef = new BaseResourceReference.Builder().value(user.getId())
                                                .ref(configuration.getBaseAddress() + "User/" + user.getId())
                                                .display(user.getDisplayName()).build();
                                    }
                                    return resRef;
                                }).filter(Objects::nonNull).collect(Collectors.toList()))
                                .build());
                    }
                });
        patch.getOperations().addAll(memberOperations);
        return patch;
    }

    protected List<SCIMv2PatchOperation> buildCustomAttributesPatchOperations(
            final Set<AttributeDelta> modifications,
            final boolean useColon) {

        List<SCIMv2PatchOperation> operations = new ArrayList<>();
        SCIMUtils.extractSCIMSchemas(configuration.getCustomAttributesJSON(), SCIMv2Attribute.class).ifPresent(
                scimSchema -> {
                    for (SCIMv2Attribute customAttribute : scimSchema.getAttributes()) {
                        String extAttrName = SCIMv2Attribute.class.cast(customAttribute).getExtensionSchema().
                                concat(useColon ? ":" : ".").concat(customAttribute.getName());
                        // only single valued attributes are supported
                        modifications.stream().filter(mod -> mod.getName().equals(extAttrName)).findFirst()
                                .ifPresent(ad -> operations.addAll(buildPatchOperations(ad, customAttribute)));
                    }
                });
        return operations;
    }

    @Override
    protected List<SCIMv2PatchOperation> buildPatchOperations(
            final AttributeDelta currentDelta,
            final SCIMBaseAttribute<?> attributeDefinition) {

        List<SCIMv2PatchOperation> operations = new ArrayList<>();
        
        if (CollectionUtil.isEmpty(currentDelta.getValuesToReplace())) {
            if (!CollectionUtil.isEmpty(currentDelta.getValuesToAdd())) {
                SCIMv2PatchOperation addPatchOperation = new SCIMv2PatchOperation();
                addPatchOperation.setPath(SCIMAttributeUtils.getBaseAttributeName(currentDelta.getName()));
                addPatchOperation.setOp(SCIMAttributeUtils.SCIM_ADD);
                addPatchOperation.setValue(
                        buildPatchValue(currentDelta.getName(), currentDelta.getValuesToAdd(), attributeDefinition));
                operations.add(addPatchOperation);
            }
            // also add values to remove in a new operation, if any
            if (!CollectionUtil.isEmpty(currentDelta.getValuesToRemove())) {
                SCIMv2PatchOperation removePatchOperation = new SCIMv2PatchOperation();
                removePatchOperation.setOp(SCIMAttributeUtils.SCIM_REMOVE);
                // while removing specific attribute values we must use a filter like emails[value eq \"user
                // .secondary@example.com\"], if multiple values are present must filter in OR like
                // emails[value eq \"user.secondary@example.com\" or value eq \"user.tertiary@example.com\"]
                removePatchOperation.setPath(
                        buildFilteredPath(SCIMAttributeUtils.getBaseAttributeName(currentDelta.getName()),
                                currentDelta.getValuesToRemove(), "or", "eq"));
                removePatchOperation.setValue(
                        buildPatchValue(currentDelta.getName(), currentDelta.getValuesToRemove(), attributeDefinition));
                operations.add(removePatchOperation);
            }
        } else {
            SCIMv2PatchOperation replacePatchOperation = new SCIMv2PatchOperation();
            replacePatchOperation.setPath(SCIMAttributeUtils.getBaseAttributeName(currentDelta.getName()));
            replacePatchOperation.setOperation(SCIMAttributeUtils.SCIM_REPLACE);
            replacePatchOperation.setValue(
                    buildPatchValue(currentDelta.getName(), currentDelta.getValuesToReplace(), attributeDefinition));
            operations.add(replacePatchOperation);
        }

        return operations;
    }

    @Override
    protected List<SCIMv2PatchOperation> buildGroupPatchOperations(final Set<AttributeDelta> modifications) {
        List<SCIMv2PatchOperation> grpOperations = new ArrayList<>();
        modifications.stream().filter(mod -> SCIMAttributeUtils.SCIM_USER_GROUPS.equalsIgnoreCase(mod.getName()))
                .findFirst().ifPresent(mod -> {
                    // remove ops
                    if (!CollectionUtil.isEmpty(mod.getValuesToRemove())) {
                        grpOperations.add(new SCIMv2PatchOperation.Builder()
                                .op(SCIMAttributeUtils.SCIM_REMOVE)
                                .path(buildFilteredPath(mod.getName(), mod.getValuesToRemove(), "or", "eq"))
                                .build());
                    }
                    // add ops
                    if (!CollectionUtil.isEmpty(mod.getValuesToAdd())) {
                        grpOperations.add(new SCIMv2PatchOperation.Builder()
                                .op(SCIMAttributeUtils.SCIM_ADD)
                                .path(SCIMAttributeUtils.SCIM_USER_GROUPS)
                                .value(mod.getValuesToAdd().stream().map(vta -> {
                                    SCIMv2Group group = client.getGroup(vta.toString());
                                    BaseResourceReference resRef = null;
                                    if (group == null) {
                                        LOG.error("Unable to add group {0} to the user, group does not exist", vta);
                                    } else {
                                        resRef = new BaseResourceReference.Builder().value(group.getId())
                                                .ref(configuration.getBaseAddress() + "Groups/" + group.getId())
                                                .display(group.getDisplayName()).build();
                                    }
                                    return resRef;
                                }).filter(Objects::nonNull).collect(Collectors.toList()))
                                .build());
                    }
                    // replace ops
                    if (!CollectionUtil.isEmpty(mod.getValuesToReplace())) {
                        grpOperations.add(new SCIMv2PatchOperation.Builder()
                                .op(SCIMAttributeUtils.SCIM_REPLACE)
                                .path(SCIMAttributeUtils.SCIM_USER_GROUPS)
                                .value(mod.getValuesToReplace().stream().map(vtr -> {
                                    SCIMv2Group group = client.getGroup(vtr.toString());
                                    BaseResourceReference resRef = null;
                                    if (group == null) {
                                        LOG.error("Unable to replace group {0} to the user, group does not exist", vtr);
                                    } else {
                                        resRef = new BaseResourceReference.Builder().value(group.getId())
                                                .ref(configuration.getBaseAddress() + "Groups/" + group.getId())
                                                .display(group.getDisplayName()).build();
                                    }
                                    return resRef;
                                }).filter(Objects::nonNull).collect(Collectors.toList()))
                                .build());
                    }
                });
        return grpOperations;
    }

    @Override
    protected List<SCIMv2PatchOperation> buildAddressesPatchOperations(
            final Set<AttributeDelta> modifications,
            final SCIMv2User currentUser) {

        // 1. first manage removals
        List<SCIMv2PatchOperation> patchOperations = new ArrayList<>();
        modifications.stream()
                .filter(mod -> mod.getName().startsWith(SCIMAttributeUtils.SCIM_USER_ADDRESSES)
                && !CollectionUtil.isEmpty(mod.getValuesToRemove()))
                .collect(Collectors.toMap(AttributeDelta::getName, AttributeDelta::getValuesToRemove))
                .forEach((k, v) -> {
                    SCIMv2PatchOperation patchOperation = new SCIMv2PatchOperation();
                    patchOperation.setPath(buildFilteredPath(k, v, "or", "eq"));
                    patchOperation.setOp(SCIMAttributeUtils.SCIM_REMOVE);
                    patchOperations.add(patchOperation);
                });

        // 2. then manage additions
        if (modifications.stream().anyMatch(mod -> mod.getName().startsWith(SCIMAttributeUtils.SCIM_USER_ADDRESSES)
                && !CollectionUtil.isEmpty(mod.getValuesToAdd()))) {

            // default
            SCIMUserAddress defaultAddress = buildUserAddress(modifications, null, false);
            if (!defaultAddress.isEmpty()) {
                SCIMv2PatchOperation patchOperation = new SCIMv2PatchOperation();
                patchOperation.setPath(SCIMAttributeUtils.SCIM_USER_ADDRESSES);
                patchOperation.setOp(SCIMAttributeUtils.SCIM_ADD);
                patchOperation.setValue(defaultAddress);
                patchOperations.add(patchOperation);
            }
            // home
            SCIMUserAddress homeAddress = buildUserAddress(modifications, AddressCanonicalType.home, false);
            if (!homeAddress.isEmpty()) {
                SCIMv2PatchOperation patchOperation = new SCIMv2PatchOperation();
                patchOperation.setPath(SCIMAttributeUtils.SCIM_USER_ADDRESSES);
                patchOperation.setOp(SCIMAttributeUtils.SCIM_ADD);
                patchOperation.setValue(homeAddress);
                patchOperations.add(patchOperation);
            }
            // work
            SCIMUserAddress workAddress = buildUserAddress(modifications, AddressCanonicalType.work, false);
            if (!workAddress.isEmpty()) {
                SCIMv2PatchOperation patchOperation = new SCIMv2PatchOperation();
                patchOperation.setPath(SCIMAttributeUtils.SCIM_USER_ADDRESSES);
                patchOperation.setOp(SCIMAttributeUtils.SCIM_ADD);
                patchOperation.setValue(workAddress);
                patchOperations.add(patchOperation);
            }
            // other
            SCIMUserAddress otherAddress = buildUserAddress(modifications, AddressCanonicalType.other, false);
            if (!otherAddress.isEmpty()) {
                SCIMv2PatchOperation patchOperation = new SCIMv2PatchOperation();
                patchOperation.setPath(SCIMAttributeUtils.SCIM_USER_ADDRESSES);
                patchOperation.setOp(SCIMAttributeUtils.SCIM_ADD);
                patchOperation.setValue(otherAddress);
                patchOperations.add(patchOperation);
            }
        }
        // 3. finally manage replaces, must perform a read before to get previous values
        if (modifications.stream().anyMatch(mod -> mod.getName().startsWith(SCIMAttributeUtils.SCIM_USER_ADDRESSES)
                && !CollectionUtil.isEmpty(mod.getValuesToReplace()))) {

            // default
            addAddressToPatchOperations(patchOperations,
                    currentUser.getAddresses().stream().filter(a -> a.getType() == null).findFirst(),
                    buildUserAddress(modifications, null, true));
            // home
            addAddressToPatchOperations(patchOperations,
                    currentUser.getAddresses().stream().filter(a -> AddressCanonicalType.home == a.getType())
                            .findFirst(), buildUserAddress(modifications, AddressCanonicalType.home, true));
            // work
            addAddressToPatchOperations(patchOperations,
                    currentUser.getAddresses().stream().filter(a -> AddressCanonicalType.work == a.getType())
                            .findFirst(), buildUserAddress(modifications, AddressCanonicalType.work, true));
            // other
            addAddressToPatchOperations(patchOperations,
                    currentUser.getAddresses().stream().filter(a -> AddressCanonicalType.other == a.getType())
                            .findFirst(), buildUserAddress(modifications, AddressCanonicalType.other, true));
        }

        return patchOperations;
    }

    private static void addAddressToPatchOperations(
            final List<SCIMv2PatchOperation> patchOperations,
            final Optional<SCIMUserAddress> currentOtherAddress,
            final SCIMUserAddress newAddress) {

        if (!newAddress.isEmpty()) {
            SCIMv2PatchOperation patchOperation = new SCIMv2PatchOperation();
            patchOperation.setPath(SCIMAttributeUtils.SCIM_USER_ADDRESSES);
            patchOperation.setOp(SCIMAttributeUtils.SCIM_ADD);
            patchOperation.setValue(newAddress.fillFrom(currentOtherAddress));
            patchOperations.add(patchOperation);
        } else if (currentOtherAddress.isPresent()) {
            SCIMv2PatchOperation patchOperation = new SCIMv2PatchOperation();
            patchOperation.setPath(SCIMAttributeUtils.SCIM_USER_ADDRESSES);
            patchOperation.setOp(SCIMAttributeUtils.SCIM_ADD);
            patchOperation.setValue(currentOtherAddress.get());
            patchOperations.add(patchOperation);
        }
    }

    private SCIMUserAddress buildUserAddress(
            final Set<AttributeDelta> modifications,
            final AddressCanonicalType type,
            final boolean isReplace) {

        SCIMUserAddress address = new SCIMUserAddress();
        Optional.ofNullable(type).ifPresent(address::setType);
        // streetAddress
        setAddressAttribute(modifications,
                SCIMAttributeUtils.SCIM_USER_ADDRESSES + "."
                + (type == null ? StringUtil.EMPTY : (type.name() + "."))
                + SCIMAttributeUtils.SCIM_USER_STREET_ADDRESS, isReplace,
                address::setStreetAddress);
        // locality
        setAddressAttribute(modifications,
                SCIMAttributeUtils.SCIM_USER_ADDRESSES + "."
                + (type == null ? StringUtil.EMPTY : (type.name() + "."))
                + SCIMAttributeUtils.SCIM_USER_LOCALITY, isReplace,
                address::setLocality);
        // region
        setAddressAttribute(modifications,
                SCIMAttributeUtils.SCIM_USER_ADDRESSES + "."
                + (type == null ? StringUtil.EMPTY : (type.name() + "."))
                + SCIMAttributeUtils.SCIM_USER_REGION, isReplace,
                address::setRegion);
        // postalCode
        setAddressAttribute(modifications,
                SCIMAttributeUtils.SCIM_USER_ADDRESSES + "."
                + (type == null ? StringUtil.EMPTY : (type.name() + "."))
                + SCIMAttributeUtils.SCIM_USER_POSTAL_CODE, isReplace,
                address::setPostalCode);
        // country
        setAddressAttribute(modifications,
                SCIMAttributeUtils.SCIM_USER_ADDRESSES + "."
                + (type == null ? StringUtil.EMPTY : (type.name() + "."))
                + SCIMAttributeUtils.SCIM_USER_COUNTRY, isReplace,
                address::setCountry);
        return address;
    }

    private static void setAddressAttribute(
            final Set<AttributeDelta> modifications,
            final String addressAttrName,
            final boolean isReplace,
            final Consumer<String> setter) {

        modifications.stream().
                filter(mod -> mod.getName().equalsIgnoreCase(addressAttrName)
                && (isReplace && !CollectionUtil.isEmpty(mod.getValuesToReplace()))
                || (!isReplace && !CollectionUtil.isEmpty(mod.getValuesToAdd()))).
                findFirst().
                ifPresent(mod -> setter.accept(isReplace
                ? mod.getValuesToReplace().get(0).toString()
                : mod.getValuesToAdd().get(0).toString()));
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

    @Override
    protected SCIMv2EnterpriseUser.SCIMv2EnterpriseUserManager buildEnterpriseUserManager(final String value) {
        SCIMv2EnterpriseUser.SCIMv2EnterpriseUserManager manager =
                new SCIMv2EnterpriseUser.SCIMv2EnterpriseUserManager();
        manager.setValue(value);
        return manager;
    }

    private BaseResourceReference buildPatchValue(final SCIMv2User user) {
        BaseResourceReference.Builder builder = new BaseResourceReference.Builder();
        switch (provider) {
            case WSO2:
                builder.value(user.getId()).display(user.getUserName());
                break;

            case KEYCLOAK:
                builder.value(user.getId())
                        .ref(configuration.getBaseAddress() + "Users/" + user.getId())
                        .display(user.getDisplayName());
                break;

            default:
                builder.value(user.getId());
        }
        return builder.build();
    }
}
