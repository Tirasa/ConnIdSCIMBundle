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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.tirasa.connid.bundles.scim.common.dto.BaseResourceReference;
import net.tirasa.connid.bundles.scim.common.dto.PagedResults;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseAttribute;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseMeta;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBasePatch;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseResource;
import net.tirasa.connid.bundles.scim.common.dto.SCIMDefaultComplex;
import net.tirasa.connid.bundles.scim.common.dto.SCIMEnterpriseUser;
import net.tirasa.connid.bundles.scim.common.dto.SCIMGenericComplex;
import net.tirasa.connid.bundles.scim.common.dto.SCIMGroup;
import net.tirasa.connid.bundles.scim.common.dto.SCIMSchema;
import net.tirasa.connid.bundles.scim.common.dto.SCIMUser;
import net.tirasa.connid.bundles.scim.common.service.SCIMService;
import net.tirasa.connid.bundles.scim.common.types.EmailCanonicalType;
import net.tirasa.connid.bundles.scim.common.types.IMCanonicalType;
import net.tirasa.connid.bundles.scim.common.types.PhoneNumberCanonicalType;
import net.tirasa.connid.bundles.scim.common.types.PhotoCanonicalType;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11EnterpriseUser;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMPatchOperation;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Attribute;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2EnterpriseUser;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.common.security.SecurityUtil;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeDelta;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.AttributesAccessor;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsIgnoreCaseFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateDeltaOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;

public abstract class AbstractSCIMConnector<UT extends SCIMUser<? extends SCIMBaseMeta, ? extends SCIMEnterpriseUser<
        ?>>, GT extends SCIMGroup<? extends SCIMBaseMeta>, ERT extends SCIMBaseResource<? extends SCIMBaseMeta>,
        P extends SCIMBasePatch, PO extends SCIMPatchOperation, ST extends SCIMService<UT, GT, ERT, P>, 
        EUM extends Serializable>
        implements Connector, CreateOp, DeleteOp, SchemaOp, SearchOp<Filter>, TestOp, UpdateOp, UpdateDeltaOp {

    protected static final Log LOG = Log.getLog(AbstractSCIMConnector.class);

    protected SCIMConnectorConfiguration configuration;

    protected SCIMProvider provider;

    protected ST client;

    @Override
    public void init(final Configuration configuration) {
        LOG.ok("Init");

        this.configuration = (SCIMConnectorConfiguration) configuration;
        this.configuration.validate();
        // check whether PATCH is supported by the SCIM server, if so enable it in the configuration

        provider = SCIMProvider.valueOf(this.configuration.getScimProvider().toUpperCase());

        client = buildSCIMClient(SCIMConnectorConfiguration.class.cast(configuration));

        LOG.ok("Connector {0} successfully inited", getClass().getName());
    }

    @Override
    public void dispose() {
        LOG.ok("Configuration cleanup");

        configuration = null;
    }

    @Override
    public void executeQuery(
            final ObjectClass objectClass,
            final Filter query,
            final ResultsHandler handler,
            final OperationOptions options) {

        LOG.ok("Connector READ");

        Attribute key = null;
        if (query instanceof EqualsFilter || query instanceof EqualsIgnoreCaseFilter) {
            Attribute filterAttr = query instanceof EqualsFilter
                    ? ((EqualsFilter) query).getAttribute()
                    : ((EqualsIgnoreCaseFilter) query).getAttribute();

            if (filterAttr instanceof Uid
                    || ObjectClass.ACCOUNT.equals(objectClass) || ObjectClass.GROUP.equals(objectClass)) {

                key = filterAttr;
            }
        }

        Set<String> attributesToGet = new HashSet<>();
        if (options.getAttributesToGet() != null) {
            attributesToGet.addAll(Arrays.asList(options.getAttributesToGet()));
        }

        if (ObjectClass.ACCOUNT.equals(objectClass)) {
            if (key == null) {
                List<UT> users = null;
                int remainingResults = -1;
                int pagesSize = Optional.ofNullable(options.getPageSize()).orElse(-1);
                String cookie = options.getPagedResultsCookie();

                try {
                    if (pagesSize != -1) {
                        if (StringUtil.isNotBlank(cookie)) {
                            PagedResults<UT> pagedResult =
                                    client.getAllUsers(Integer.valueOf(cookie), pagesSize, attributesToGet);
                            users = pagedResult.getResources();

                            cookie = users.size() >= pagesSize ? String.valueOf(
                                    pagedResult.getStartIndex() + users.size()) : null;
                        } else {
                            PagedResults<UT> pagedResult = client.getAllUsers(1, pagesSize, attributesToGet);
                            users = pagedResult.getResources();

                            cookie = users.size() >= pagesSize ? String.valueOf(
                                    pagedResult.getStartIndex() + users.size()) : null;
                        }
                    } else {
                        users = client.getAllUsers(attributesToGet);
                    }
                } catch (Exception e) {
                    SCIMUtils.wrapGeneralError("While getting Users!", e);
                }

                for (UT user : users) {
                    handler.handle(fromUser(user, attributesToGet));
                }

                if (handler instanceof SearchResultsHandler) {
                    ((SearchResultsHandler) handler).handleResult(new SearchResult(cookie, remainingResults));
                }
            } else {
                UT result = null;
                if (Uid.NAME.equals(key.getName()) || SCIMAttributeUtils.ATTRIBUTE_ID.equals(key.getName())) {
                    result = null;
                    try {
                        result = client.getUser(AttributeUtil.getAsStringValue(key));
                    } catch (Exception e) {
                        SCIMUtils.wrapGeneralError(
                                "While getting User : " + key.getName() + " - " + AttributeUtil.getAsStringValue(key),
                                e);
                    }
                } else {
                    try {
                        List<UT> users = client.getAllUsers(
                                (Name.NAME.equals(key.getName()) ? "username" : key.getName()) + " eq \""
                                + AttributeUtil.getAsStringValue(key) + "\"", attributesToGet);
                        if (!users.isEmpty()) {
                            result = users.get(0);
                        }
                    } catch (Exception e) {
                        SCIMUtils.wrapGeneralError(
                                "While getting User : " + key.getName() + " - " + AttributeUtil.getAsStringValue(key),
                                e);
                    }
                }
                if (result != null) {
                    handler.handle(fromUser(result, attributesToGet));
                }
            }
        } else if (ObjectClass.GROUP.equals(objectClass)) {
            if (key == null) {
                List<GT> groups = null;
                int remainingResults = -1;
                int pagesSize = Optional.ofNullable(options.getPageSize()).orElse(-1);
                String cookie = options.getPagedResultsCookie();

                try {
                    if (pagesSize != -1) {
                        if (StringUtil.isNotBlank(cookie)) {
                            PagedResults<GT> pagedResult = client.getAllGroups(Integer.valueOf(cookie), pagesSize);
                            groups = pagedResult.getResources();

                            cookie = groups.size() >= pagesSize ? String.valueOf(
                                    pagedResult.getStartIndex() + groups.size()) : null;
                        } else {
                            PagedResults<GT> pagedResult = client.getAllGroups(1, pagesSize);
                            groups = pagedResult.getResources();

                            cookie = groups.size() >= pagesSize ? String.valueOf(
                                    pagedResult.getStartIndex() + groups.size()) : null;
                        }
                    } else {
                        groups = client.getAllGroups();
                    }
                } catch (Exception e) {
                    LOG.error(e, "Could not search for Groups");
                    SCIMUtils.wrapGeneralError("Could not search for Groups", e);
                }

                for (GT group : groups) {
                    handler.handle(fromGroup(group, attributesToGet));
                }

                if (handler instanceof SearchResultsHandler) {
                    ((SearchResultsHandler) handler).handleResult(new SearchResult(cookie, remainingResults));
                }
            } else {
                GT result = null;
                if (Uid.NAME.equals(key.getName()) || SCIMAttributeUtils.ATTRIBUTE_ID.equals(key.getName())) {
                    result = null;
                    try {
                        result = client.getGroup(AttributeUtil.getAsStringValue(key));
                    } catch (Exception e) {
                        SCIMUtils.wrapGeneralError(
                                "While getting Group : " + key.getName() + " - " + AttributeUtil.getAsStringValue(key),
                                e);
                    }
                } else {
                    try {
                        List<GT> groups = client.getAllGroups(
                                (Name.NAME.equals(key.getName()) ? SCIMAttributeUtils.SCIM_GROUP_DISPLAY_NAME
                                : key.getName()) + " eq \"" + AttributeUtil.getAsStringValue(key) + "\"");
                        if (!groups.isEmpty()) {
                            result = groups.get(0);
                        }
                    } catch (Exception e) {
                        SCIMUtils.wrapGeneralError(
                                "While getting Group : " + key.getName() + " - " + AttributeUtil.getAsStringValue(key),
                                e);
                    }
                }
                if (result != null) {
                    handler.handle(fromGroup(result, attributesToGet));
                }
            }
        } else {
            LOG.warn("Search of type {0} is not supported", objectClass.getObjectClassValue());
            throw new UnsupportedOperationException(
                    "Search of type" + objectClass.getObjectClassValue() + " is not supported");
        }
    }

    @Override
    public Uid create(
            final ObjectClass objectClass,
            final Set<Attribute> createAttributes,
            final OperationOptions options) {

        LOG.ok("Connector CREATE");

        if (createAttributes == null || createAttributes.isEmpty()) {
            SCIMUtils.handleGeneralError("Set of Attributes value is null or empty");
        }

        final AttributesAccessor accessor = new AttributesAccessor(createAttributes);

        if (ObjectClass.ACCOUNT.equals(objectClass)) {
            UT user = buildNewUserEntity(
                    SCIMUtils.extractSCIMSchemas(configuration.getCustomAttributesJSON(), SCIMv2Attribute.class));
            String username = accessor.findString(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME);
            if (username == null) {
                username = accessor.findString(Name.NAME);
            }

            GuardedString password = accessor.findGuardedString(OperationalAttributes.PASSWORD_NAME);
            Attribute status = accessor.find(OperationalAttributes.ENABLE_NAME);

            try {
                user.setUserName(username);
                Optional.ofNullable(accessor.findString(SCIMAttributeUtils.USER_ATTRIBUTE_EXTERNAL_ID))
                        .ifPresent(user::setExternalId);
                // manage groups
                List<String> groups = accessor.findStringList(SCIMAttributeUtils.SCIM_USER_GROUPS);
                LOG.info("Adding groups {0} to user {1}", groups, username);
                List<GT> scimGroups = groups == null ? Collections.emptyList()
                        : groups.stream().map(client::getGroup).filter(g -> g != null).collect(Collectors.toList());
                scimGroups.forEach(g -> user.getGroups().add(new BaseResourceReference.Builder().value(g.getId())
                        .ref(configuration.getBaseAddress() + "Groups/" + g.getId()).display(g.getDisplayName())
                        .build()));

                if (configuration.getManageComplexEntitlements()) {
                    // manage not default entitlements
                    List<String> entitlements = accessor.findStringList(SCIMAttributeUtils.SCIM_USER_ENTITLEMENTS);
                    LOG.info("Adding entitlements {0} to user {1}", entitlements, username);
                    manageEntitlements(user, entitlements);
                }

                if (password == null) {
                    LOG.warn("Missing password attribute");
                } else {
                    user.setPassword(SecurityUtil.decrypt(password));
                }

                if (status == null || status.getValue() == null || status.getValue().isEmpty()) {
                    LOG.warn("{0} attribute value not correct or not found, won't handle User status",
                            OperationalAttributes.ENABLE_NAME);
                } else {
                    user.setActive(Boolean.valueOf(status.getValue().get(0).toString()));
                }

                user.fromAttributes(createAttributes);

                // custom attributes
                if (StringUtil.isNotBlank(configuration.getCustomAttributesJSON())) {
                    user.fillSCIMCustomAttributes(createAttributes, configuration.getCustomAttributesJSON(),
                            configuration.getUseColonOnExtensionAttributes());
                }
                // enterprise user
                createAttributes.stream().
                        filter(ca -> ca.getName().contains(SCIMv2EnterpriseUser.SCHEMA_URI)).
                        findFirst().ifPresent(ca -> {
                            user.getSchemas().add(SCIMv2EnterpriseUser.SCHEMA_URI);
                            user.fillEnterpriseUser(createAttributes, configuration.getUseColonOnExtensionAttributes());
                        });

                client.createUser(user);
                // update also groups, if needed
                if (!scimGroups.isEmpty() && configuration.getExplicitGroupAddOnCreate()) {
                    LOG.info("Updating groups {0} explicitly adding user {1}", groups, user.getId());

                    scimGroups.forEach(group -> {
                        group.getMembers().add(SCIMUtils.buildGroupMember(user, provider));
                        if ("PATCH".equals(configuration.getUpdateGroupMethod())) {
                            client.updateGroup(group.getId(), buildMembersGroupPatch(
                                    Collections.singletonList(user), SCIMAttributeUtils.SCIM_ADD));
                        } else {
                            client.updateGroup(group);
                        }
                    });
                }
            } catch (Exception e) {
                LOG.error(e, "Unable to update user {0}", username);
                SCIMUtils.wrapGeneralError("Could not create User : " + username, e);
            }

            return new Uid(user.getId());
        }

        if (ObjectClass.GROUP.equals(objectClass)) {
            GT group = buildNewGroupEntity();
            String displayName = accessor.findString(SCIMAttributeUtils.SCIM_GROUP_DISPLAY_NAME);
            try {
                group.setDisplayName(displayName);
                group.fromAttributes(createAttributes, configuration.getReplaceMembersOnUpdate());
                client.createGroup(group);
            } catch (Exception e) {
                LOG.error(e, "Unable to create Group {0}", displayName);
                SCIMUtils.wrapGeneralError("Could not create Group : " + displayName, e);
            }

            return new Uid(group.getId());
        } else {
            LOG.warn("Create of type {0} is not supported", objectClass.getObjectClassValue());
            throw new UnsupportedOperationException(
                    "Create of type" + objectClass.getObjectClassValue() + " is not supported");
        }
    }

    @Override
    public Uid update(
            final ObjectClass objectClass,
            final Uid uid,
            final Set<Attribute> replaceAttributes,
            final OperationOptions options) {

        LOG.ok("Connector UPDATE object [{0}]", uid);

        if (replaceAttributes == null || replaceAttributes.isEmpty()) {
            SCIMUtils.handleGeneralError("Set of Attributes value is null or empty");
        }

        final AttributesAccessor accessor = new AttributesAccessor(replaceAttributes);

        Uid returnUid = uid;
        if (ObjectClass.ACCOUNT.equals(objectClass)) {
            Attribute status = accessor.find(OperationalAttributes.ENABLE_NAME);
            String username = accessor.findString(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME);
            if (username == null) {
                username = accessor.findString(Name.NAME);
            }

            UT user = buildNewUserEntity(
                    SCIMUtils.extractSCIMSchemas(configuration.getCustomAttributesJSON(), SCIMv2Attribute.class));
            user.setId(uid.getUidValue());
            user.setUserName(username);
            if (status == null || status.getValue() == null || status.getValue().isEmpty()) {
                LOG.warn("{0} attribute value not correct, can't handle User  status update",
                        OperationalAttributes.ENABLE_NAME);
            } else {
                user.setActive(Boolean.valueOf(status.getValue().get(0).toString()));
            }

            // custom attributes
            if (StringUtil.isNotBlank(configuration.getCustomAttributesJSON())) {
                user.fillSCIMCustomAttributes(replaceAttributes, configuration.getCustomAttributesJSON(),
                        configuration.getUseColonOnExtensionAttributes());
            }
            // enterprise user
            replaceAttributes.stream().filter(ca -> ca.getName().contains(SCIMv2EnterpriseUser.SCHEMA_URI)).findFirst()
                    .ifPresent(ca -> {
                        user.getSchemas().add(SCIMv2EnterpriseUser.SCHEMA_URI);
                        user.fillEnterpriseUser(replaceAttributes, configuration.getUseColonOnExtensionAttributes());
                    });

            try {
                user.fromAttributes(replaceAttributes);

                // manage groups
                final Map<String, P> groupPatches = new HashMap<>();
                if ("PATCH".equalsIgnoreCase(configuration.getUpdateGroupMethod())) {
                    // calculate groupsToAdd and groupsToRemove
                    List<String> groups =
                            Optional.ofNullable(accessor.findStringList(SCIMAttributeUtils.SCIM_USER_GROUPS))
                                    .orElse(Collections.emptyList());
                    List<String> currentGroups =
                            client.getUser(user.getId()).getGroups().stream().map(g -> g.getValue())
                                    .collect(Collectors.toList());
                    List<String> groupsToAdd =
                            groups.stream().filter(g -> !currentGroups.contains(g)).collect(Collectors.toList());
                    List<String> groupsToRemove =
                            currentGroups.stream().filter(g -> !groups.contains(g)).collect(Collectors.toList());
                    fillGroupPatches(user, groupPatches, groupsToAdd, groupsToRemove);
                } else {
                    List<String> groups = accessor.findStringList(SCIMAttributeUtils.SCIM_USER_GROUPS);
                    if (groups != null && !groups.isEmpty()) {
                        LOG.info("Updating groups {0} of user {1}", groups, user.getId());
                        groups.forEach(g -> {
                            GT group = client.getGroup(g);
                            if (group == null) {
                                LOG.error("Unable to add group {0} to the user, group does not exist", g);
                            } else {
                                user.getGroups().add(new BaseResourceReference.Builder().value(group.getId())
                                        .ref(configuration.getBaseAddress() + "Groups/" + group.getId())
                                        .display(group.getDisplayName()).build());
                            }
                        });
                    }
                }

                if (configuration.getManageComplexEntitlements()) {
                    // manage not default entitlements
                    List<String> entitlements = accessor.findStringList(SCIMAttributeUtils.SCIM_USER_ENTITLEMENTS);
                    LOG.info("Adding entitlements {0} on update to user {1}", entitlements, username);
                    manageEntitlements(user, entitlements);
                }
                GuardedString password = accessor.getPassword() != null ? accessor.getPassword()
                        : accessor.findGuardedString(OperationalAttributes.PASSWORD_NAME);
                if (password == null) {
                    LOG.info("No password to update");
                } else {
                    String decryptedPassword = SecurityUtil.decrypt(password);
                    user.setPassword(decryptedPassword);
                }

                client.updateUser(user);
                // if PATCH is enabled update also group with memberships previously calculated
                groupPatches.entrySet()
                        .forEach(patchEntry -> client.updateGroup(patchEntry.getKey(), patchEntry.getValue()));

                returnUid = new Uid(user.getId());
            } catch (Exception e) {
                LOG.error(e, "Could not update User {0} from attributes", uid.getUidValue());
                SCIMUtils.wrapGeneralError("Could not update User " + uid.getUidValue() + " from attributes ", e);
            }
        } else if (ObjectClass.GROUP.equals(objectClass)) {
            String displayName = accessor.findString(SCIMAttributeUtils.SCIM_GROUP_DISPLAY_NAME);
            if (displayName == null) {
                displayName = accessor.findString(Name.NAME);
            }

            GT group = buildNewGroupEntity();
            group.setId(uid.getUidValue());
            group.setDisplayName(displayName);
            try {
                group.fromAttributes(replaceAttributes, configuration.getReplaceMembersOnUpdate());

                if ("PATCH".equals(configuration.getUpdateGroupMethod())) {
                    client.updateGroup(uid.getUidValue(), buildPatchFromGroup(group));

                    if (configuration.getReplaceMembersOnUpdate()) {
                        // replace all members of the group on update
                        List<String> members = Optional.ofNullable(
                                accessor.findStringList(SCIMAttributeUtils.SCIM_GROUP_MEMBERS)).
                                orElse(Collections.emptyList());

                        // do not execute update if no users, some providers like AWS could complain about this
                        if (!CollectionUtil.isEmpty(members)) {
                            LOG.ok("Replacing all group [{0}] members with [{1}] during PATCH update",
                                    group.getId(), members);

                            List<UT> scimUsers = members.stream()
                                    .map(client::getUser)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());
                            client.updateGroup(
                                    group.getId(),
                                    buildMembersGroupPatch(scimUsers, SCIMAttributeUtils.SCIM_ADD));
                        }
                    }
                } else {
                    // add members to group
                    client.updateGroup(group);
                }

                returnUid = new Uid(group.getId());
            } catch (Exception e) {
                SCIMUtils.wrapGeneralError("Could not update Group " + uid.getUidValue() + " from attributes ", e);
            }
        } else {
            LOG.warn("Update of type {0} is not supported", objectClass.getObjectClassValue());
            throw new UnsupportedOperationException(
                    "Update of type" + objectClass.getObjectClassValue() + " is not supported");
        }
        return returnUid;
    }

    @Override
    public Set<AttributeDelta> updateDelta(
            final ObjectClass objectClass,
            final Uid uid,
            final Set<AttributeDelta> modifications,
            final OperationOptions options) {

        LOG.ok("Connector UPDATE DELTA object [{0}]", uid);

        if (modifications == null || modifications.isEmpty()) {
            SCIMUtils.handleGeneralError("Set of Attribute Deltas is null or empty");
        }

        if (ObjectClass.ACCOUNT.equals(objectClass)) {
            // 1. build and send a patch for the user
            UT currentUser = client.getUser(uid.getUidValue());
            if (currentUser == null) {
                SCIMUtils.handleGeneralError("Unable to update user because does not exist");
            }
            Map<String, P> groupPatches = new HashMap<>();
            boolean manageGroupsWithPatch = "PATCH".equalsIgnoreCase(configuration.getUpdateGroupMethod());
            if (manageGroupsWithPatch) {
                // only values to add and remove are supported
                modifications.stream().
                        filter(ad -> SCIMAttributeUtils.SCIM_USER_GROUPS.equalsIgnoreCase(ad.getName())).
                        findFirst().ifPresent(grmod -> fillGroupPatches(
                        currentUser,
                        groupPatches,
                        CollectionUtil.isEmpty(grmod.getValuesToAdd())
                        ? CollectionUtil.isEmpty(grmod.getValuesToReplace())
                        ? Collections.emptyList()
                        : grmod.getValuesToReplace().stream().map(String.class::cast)
                                .collect(Collectors.toList())
                        : CollectionUtil.isEmpty(grmod.getValuesToAdd())
                        ? Collections.emptyList()
                        : grmod.getValuesToAdd().stream().map(String.class::cast).collect(Collectors.toList()),
                        CollectionUtil.isEmpty(grmod.getValuesToRemove())
                        ? Collections.emptyList()
                        : grmod.getValuesToRemove().stream().map(String.class::cast).collect(Collectors.toList())));
            } else {
                LOG.warn("Group update method must be set to PATCH while updating through UPDATE_DELTA");
            }
            try {
                client.updateUser(
                        uid.getUidValue(),
                        buildUserPatch(modifications, currentUser, !manageGroupsWithPatch));
                // 2. if any modify also groups
                // if PATCH is enabled update also group with memberships previously calculated
                groupPatches.forEach((key, value) -> client.updateGroup(key, value));
            } catch (Exception e) {
                SCIMUtils.handleGeneralError("Error while updating user", e);
            }
        } else if (ObjectClass.GROUP.equals(objectClass)) {
            try {
                if ("PATCH".equals(configuration.getUpdateGroupMethod())) {
                    client.updateGroup(uid.getUidValue(), buildGroupPatch(modifications));
                } else {
                    LOG.warn("Group update method must be set to PATCH while updating through UPDATE_DELTA");
                }
            } catch (Exception e) {
                SCIMUtils.handleGeneralError("Error while updating group", e);
            }
        }
        return modifications;
    }

    @Override
    public void delete(final ObjectClass objectClass, final Uid uid, final OperationOptions options) {
        LOG.ok("Connector DELETE object [{0}]", uid);

        if (StringUtil.isBlank(uid.getUidValue())) {
            LOG.error("Uid not provided or empty ");
            throw new InvalidAttributeValueException("Uid value not provided or empty");
        }

        if (objectClass == null) {
            LOG.error("Object value not provided {0} ", objectClass);
            throw new InvalidAttributeValueException("Object value not provided");
        }

        if (ObjectClass.ACCOUNT.equals(objectClass)) {
            try {
                client.deleteUser(uid.getUidValue());
            } catch (Exception e) {
                LOG.error(e, "Could not delete User {0}", uid.getUidValue());
                SCIMUtils.wrapGeneralError("Could not delete User " + uid.getUidValue(), e);
            }
        } else if (ObjectClass.GROUP.equals(objectClass)) {
            try {
                client.deleteGroup(uid.getUidValue());
            } catch (Exception e) {
                LOG.error(e, "Could not delete Group {0}", uid.getUidValue());
                SCIMUtils.wrapGeneralError("Could not delete Group " + uid.getUidValue(), e);
            }
        } else {
            LOG.warn("Delete of type {0} is not supported", objectClass.getObjectClassValue());
            throw new UnsupportedOperationException(
                    "Delete of type" + objectClass.getObjectClassValue() + " is not supported");
        }
    }

    @Override
    public FilterTranslator<Filter> createFilterTranslator(final ObjectClass objectClass,
            final OperationOptions options) {
        return filter -> Collections.singletonList(filter);
    }

    @Override
    public void test() {
        LOG.ok("Connector TEST");

        if (configuration != null) {
            if (client != null && client.testService()) {
                LOG.ok("Test was successful");
            } else {
                SCIMUtils.handleGeneralError("Test error. Problems with client service");
            }
        } else {
            LOG.error("Test error. No instance of the configuration class");
        }
    }

    protected ConnectorObject fromUser(final UT user, final Set<String> attributesToGet) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setObjectClass(ObjectClass.ACCOUNT);
        builder.setUid(user.getId());
        builder.setName(user.getUserName());

        try {
            Set<Attribute> userAttributes = user.toAttributes(user.getClass(), configuration);

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

    protected ConnectorObject fromGroup(final GT group, final Set<String> attributesToGet) {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setObjectClass(ObjectClass.GROUP);
        builder.setUid(group.getId());
        builder.setName(group.getDisplayName());

        try {
            for (Attribute toAttribute : group.toAttributes(group.getClass(), configuration)) {
                String attributeName = toAttribute.getName();
                for (String attributeToGetName : attributesToGet) {
                    if (attributeName.equals(attributeToGetName)) {
                        builder.addAttribute(toAttribute);
                        break;
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            LOG.error(ex, "While converting to attributes for group", group);
        }

        return builder.build();
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    protected abstract <T extends SCIMBaseAttribute<T>> UT buildNewUserEntity(Optional<SCIMSchema<T>> customSchema);

    protected abstract GT buildNewGroupEntity();

    protected abstract ST getClient();

    protected abstract ST buildSCIMClient(SCIMConnectorConfiguration configuration);

    protected abstract void fillGroupPatches(
            UT user, Map<String, P> groupPatches, List<String> groupsToAdd, List<String> groupsToRemove);

    protected abstract P buildMembersGroupPatch(List<UT> user, String op);

    protected abstract P buildPatchFromGroup(GT group);

    protected abstract P buildUserPatch(Set<AttributeDelta> modifications, UT currentUser, boolean manageGroups);

    protected abstract P buildGroupPatch(Set<AttributeDelta> modifications);

    protected abstract PO buildPatchOperation(AttributeDelta currentDelta, SCIMBaseAttribute<?> attributeDefinition);

    protected abstract List<PO> buildGroupPatchOperations(Set<AttributeDelta> modifications);

    protected abstract List<PO> buildAddressesPatchOperations(Set<AttributeDelta> modifications, UT currentUser);

    protected Object buildPatchValue(
            final String name,
            final List<Object> values,
            final SCIMBaseAttribute<?> attributeDefinition) {

        Object patchValue;
        switch (name) {
            case "userName":
            case "name.formatted":
            case "name.familyName":
            case "name.givenName":
            case "name.middleName":
            case "name.honorificPrefix":
            case "name.honorificSuffix":
            case "displayName":
            case "nickName":
            case "profileUrl":
            case "title":
            case "userType":
            case "locale":
            case "preferredLanguage":
            case "timezone":
                patchValue = CollectionUtil.isEmpty(values) ? null : values.get(0).toString();
                break;
            case "active":
                patchValue = CollectionUtil.isEmpty(values) ? null : Boolean.valueOf(values.get(0).toString());
                break;
            case "emails.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<EmailCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    return value;
                }).collect(Collectors.toList());
                break;
            case "emails.work.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<EmailCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(EmailCanonicalType.work);
                    return value;
                }).collect(Collectors.toList());
                // emails.work.primary must be set with path including the email value
                break;

            case "emails.home.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<EmailCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(EmailCanonicalType.home);
                    return value;
                }).collect(Collectors.toList());
                break;

            case "emails.other.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<EmailCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(EmailCanonicalType.other);
                    return value;
                }).collect(Collectors.toList());
                break;

            case "phoneNumbers.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<PhoneNumberCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    return value;
                }).collect(Collectors.toList());
                break;

            case "phoneNumbers.work.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<PhoneNumberCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(PhoneNumberCanonicalType.work);
                    return value;
                }).collect(Collectors.toList());
                break;

            case "phoneNumbers.home.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<PhoneNumberCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(PhoneNumberCanonicalType.home);
                    return value;
                }).collect(Collectors.toList());
                break;

            case "phoneNumbers.other.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<PhoneNumberCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(PhoneNumberCanonicalType.other);
                    return value;
                }).collect(Collectors.toList());
                break;

            case "phoneNumbers.pager.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<PhoneNumberCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(PhoneNumberCanonicalType.pager);
                    return value;
                }).collect(Collectors.toList());
                break;

            case "phoneNumbers.fax.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<PhoneNumberCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(PhoneNumberCanonicalType.fax);
                    return value;
                }).collect(Collectors.toList());
                break;

            case "phoneNumbers.mobile.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<PhoneNumberCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(PhoneNumberCanonicalType.mobile);
                    return value;
                }).collect(Collectors.toList());
                break;

            case "ims.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<IMCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    return value;
                }).collect(Collectors.toList());
                break;

            case "ims.aim.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<IMCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(IMCanonicalType.aim);
                    return value;
                }).collect(Collectors.toList());
                break;

            case "ims.xmpp.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<IMCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(IMCanonicalType.xmpp);
                    return value;
                }).collect(Collectors.toList());
                break;

            case "ims.skype.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<IMCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(IMCanonicalType.skype);
                    return value;
                }).collect(Collectors.toList());
                break;

            case "ims.qq.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<IMCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(IMCanonicalType.qq);
                    return value;
                }).collect(Collectors.toList());
                break;

            case "ims.yahoo.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<IMCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(IMCanonicalType.yahoo);
                    return value;
                }).collect(Collectors.toList());
                break;

            case "ims.msn.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<IMCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(IMCanonicalType.msn);
                    return value;
                }).collect(Collectors.toList());
                break;

            case "ims.icq.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<IMCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(IMCanonicalType.icq);
                    return value;
                }).collect(Collectors.toList());
                break;

            case "ims.gtalk.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<IMCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(IMCanonicalType.gtalk);
                    return value;
                }).collect(Collectors.toList());
                break;

            case "photos.photo.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<PhotoCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(PhotoCanonicalType.photo);
                    return value;
                }).collect(Collectors.toList());
                break;

            case "photos.thumbnail.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<PhotoCanonicalType> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType(PhotoCanonicalType.thumbnail);
                    return value;
                }).collect(Collectors.toList());
                break;

            case "roles.default.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<String> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType("default");
                    return value;
                }).collect(Collectors.toList());
                break;

            case "entitlements.default.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMGenericComplex<String> value = new SCIMGenericComplex<>();
                    value.setValue(v.toString());
                    value.setType("default");
                    return value;
                }).collect(Collectors.toList());
                break;

            case "x509Certificates.default.value":
                patchValue = values.stream().filter(Objects::nonNull).map(v -> {
                    SCIMDefaultComplex value = new SCIMDefaultComplex();
                    value.setValue(v.toString());
                    return value;
                }).collect(Collectors.toList());
                break;
            case SCIMv11EnterpriseUser.SCHEMA_URI + "." + SCIMAttributeUtils.SCIM_ENTERPRISE_EMPLOYEE_MANAGER_VALUE:
            case SCIMv11EnterpriseUser.SCHEMA_URI + ":" + SCIMAttributeUtils.SCIM_ENTERPRISE_EMPLOYEE_MANAGER_VALUE:
            case SCIMv2EnterpriseUser.SCHEMA_URI + "." + SCIMAttributeUtils.SCIM_ENTERPRISE_EMPLOYEE_MANAGER_VALUE:
            case SCIMv2EnterpriseUser.SCHEMA_URI + ":" + SCIMAttributeUtils.SCIM_ENTERPRISE_EMPLOYEE_MANAGER_VALUE:
                patchValue =
                        CollectionUtil.isEmpty(values) ? null : buildEnterpriseUserManager(values.get(0).toString());
                break;

            default:
                // this is mainly useful to manage custom attributes
                patchValue = CollectionUtil.isEmpty(values) ? null
                        : attributeDefinition != null && attributeDefinition.getMultiValued() ? values
                        : values.get(0).toString();
                break;
        }

        return patchValue;
    }

    protected String buildFilteredPath(final String name, final List<Object> values, final String op,
            final String comparisonOp) {
        // emails[value eq \"user.secondary@example.com\"]
        if (CollectionUtil.isEmpty(values)) {
            return StringUtil.EMPTY;
        }
        StringBuilder builder = new StringBuilder(name).append("[value ").append(comparisonOp).append(" ").append("\"")
                .append(values.get(0)).append("\"");
        values.subList(1, values.size()).forEach(
                v -> builder.append(" ").append(op).append(" value ").append(comparisonOp).append(" ").append("\"")
                        .append(values.get(0)).append("\""));

        return builder.append("]").toString();
    }

    protected abstract void manageEntitlements(UT user, List<String> values);

    protected abstract EUM buildEnterpriseUserManager(String value);
}
