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
package net.tirasa.connid.bundles.scim.v2.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import net.tirasa.connid.bundles.scim.v11.dto.PagedResults;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Attribute;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2User;
import org.apache.cxf.jaxrs.client.WebClient;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Attribute;

public class SCIMv2Client extends SCIMv2Service {

    private static final Log LOG = Log.getLog(SCIMv2Client.class);

    public SCIMv2Client(final SCIMConnectorConfiguration config) {
        super(config);
    }

    /**
     * @param attributesToGet
     * @return List of Users
     */
    @Override
    public List<SCIMv2User> getAllUsers(final Set<String> attributesToGet) {
        Map<String, String> params = new HashMap<>();
        if (!attributesToGet.isEmpty()) {
            params.put("attributes",
                    SCIMUtils.cleanAttributesToGet(
                            attributesToGet, config.getCustomAttributesJSON(), SCIMv2Attribute.class));
        }
        WebClient webClient = getWebclient("Users", params);
        return doGetAllUsers(webClient).getResources();
    }

    /**
     * @param filterQuery to filter results
     * @param attributesToGet
     * @return Filtered list of Users
     */
    @Override
    public List<SCIMv2User> getAllUsers(final String filterQuery, final Set<String> attributesToGet) {
        Map<String, String> params = new HashMap<>();
        params.put("filter", filterQuery);
        if (!attributesToGet.isEmpty()) {
            params.put("attributes", SCIMUtils.cleanAttributesToGet(
                    attributesToGet, config.getCustomAttributesJSON(), SCIMv2Attribute.class));
        }
        WebClient webClient = getWebclient("Users", params);
        return doGetAllUsers(webClient).getResources();
    }

    /**
     * @param startIndex
     * @param count
     * @param attributesToGet
     * @return Paged list of Users
     */
    @Override
    public PagedResults<SCIMv2User> getAllUsers(final Integer startIndex, final Integer count,
            final Set<String> attributesToGet) {
        Map<String, String> params = new HashMap<>();
        params.put("startIndex", String.valueOf(startIndex));
        if (count != null) {
            params.put("count", String.valueOf(count));
        }
        if (!attributesToGet.isEmpty()) {
            params.put("attributes", SCIMUtils.cleanAttributesToGet(
                    attributesToGet, config.getCustomAttributesJSON(), SCIMv2Attribute.class));
        }
        WebClient webClient = getWebclient("Users", params);
        return doGetAllUsers(webClient);
    }

    /**
     * @param filterQuery
     * @param startIndex
     * @param count
     * @param attributesToGet
     * @return Paged and Filtered list of Users
     */
    public PagedResults<SCIMv2User> getAllUsers(
            final String filterQuery,
            final Integer startIndex,
            final Integer count,
            final Set<String> attributesToGet) {

        Map<String, String> params = new HashMap<>();
        params.put("startIndex", String.valueOf(startIndex));
        if (count != null) {
            params.put("count", String.valueOf(count));
        }
        params.put("filter", filterQuery);
        params.put("attributes", SCIMUtils.cleanAttributesToGet(
                attributesToGet, config.getCustomAttributesJSON(), SCIMv2Attribute.class));
        WebClient webClient = getWebclient("Users", params);
        return doGetAllUsers(webClient);
    }

    /**
     * @param userId
     * @return User with userId id
     */
    @Override
    public SCIMv2User getUser(final String userId) {
        WebClient webClient = getWebclient("Users", null).path(userId);
        return doGetUser(webClient);
    }

    /**
     * @param user
     * @return Created User
     */
    @Override
    public SCIMv2User createUser(final SCIMv2User user) {
        return SCIMv2User.class.cast(doCreateUser(user));
    }

    /**
     * @param user
     * @param replaceAttributes
     * @return Update User
     */
    public SCIMv2User updateUser(final SCIMv2User user, final Set<Attribute> replaceAttributes) {
        return SCIMv2User.class.cast(doUpdateUser(user, replaceAttributes));
    }

    @Override
    public SCIMv2User updateUser(final SCIMv2User user) {
        return SCIMv2User.class.cast(doUpdateUser(user, Collections.emptySet()));
    }

    /**
     * @param userId
     */
    @Override
    public void deleteUser(final String userId) {
        doDeleteUser(userId, getWebclient("Users", null).path(userId));
    }

    /**
     * @param userId
     */
    public void activateUser(final String userId) {
        doActivateUser(userId);
    }

    @Override
    public boolean testService() {
        Set<String> attributesToGet = new HashSet<>();
        attributesToGet.add(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME);
        return getAllUsers(1, 1, attributesToGet) != null;
    }

    private PagedResults<SCIMv2User> doGetAllUsers(final WebClient webClient) {
        PagedResults<SCIMv2User> pagedResults = null;
        JsonNode node = doGet(webClient);
        if (node == null) {
            SCIMUtils.handleGeneralError("While retrieving Users from service");
        }

        try {
            pagedResults = SCIMUtils.MAPPER.readValue(
                    node.toString(),
                    new TypeReference<PagedResults<SCIMv2User>>() {
            });
        } catch (IOException ex) {
            LOG.error(ex, "While converting from JSON to Users");
        }

        if (pagedResults == null) {
            SCIMUtils.handleGeneralError("While retrieving Users from service");
        } else {
            // check custom attributes
            if (!pagedResults.getResources().isEmpty()) {
                readCustomAttributes(pagedResults, node.get(RESPONSE_RESOURCES));
            }
        }

        return pagedResults;
    }

    private SCIMv2User doGetUser(final WebClient webClient) {
        SCIMv2User user = null;
        JsonNode node = doGet(webClient);
        if (node == null) {
            SCIMUtils.handleGeneralError("While retrieving User from service");
        }

        try {
            user = SCIMUtils.MAPPER.readValue(node.toString(), SCIMv2User.class);
        } catch (IOException ex) {
            LOG.error(ex, "While converting from JSON to User");
        }

        if (user == null) {
            SCIMUtils.handleGeneralError("While retrieving user from service after create");
        } else {
            // check custom attributes
            readCustomAttributes(user, node, SCIMv2Attribute.class);
        }

        return user;
    }

    private SCIMv2User doCreateUser(final SCIMv2User user) {
        doCreate(user, getWebclient("Users", null));
        return user;
    }

    private SCIMv2User doUpdateUser(final SCIMv2User user, final Set<Attribute> replaceAttributes) {
        if (StringUtil.isBlank(user.getId())) {
            SCIMUtils.handleGeneralError("Missing required user id attribute for update");
        }

        SCIMv2User updated = null;
        JsonNode node = config.getUpdateMethod().equalsIgnoreCase("PATCH")
                && !replaceAttributes.isEmpty()
                ? doUpdatePatch(replaceAttributes, getWebclient("Users", null).path(user.getId()))
                : doUpdate(user, getWebclient("Users", null).path(user.getId()));
        if (node == null) {
            SCIMUtils.handleGeneralError("While running update on service");
        }

        try {
            updated = SCIMUtils.MAPPER.readValue(
                    node.toString(),
                    SCIMv2User.class
            );
        } catch (IOException ex) {
            LOG.error(ex, "While converting from JSON to User");
        }

        if (updated == null) {
            SCIMUtils.handleGeneralError("While retrieving user from service after update");
        }

        return updated;
    }

    private void doDeleteUser(final String userId, final WebClient webClient) {
        doDelete(userId, webClient);
    }

    private void doActivateUser(final String userId) {
        doActivate(userId, getWebclient("activation", null).path("tokens"));
    }
}
