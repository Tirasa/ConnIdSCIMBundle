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
package net.tirasa.connid.bundles.scim.v11.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.service.AbstractSCIMService;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import net.tirasa.connid.bundles.scim.v11.dto.PagedResults;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11Attribute;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11User;
import org.apache.cxf.jaxrs.client.WebClient;

public class SCIMv11Client extends AbstractSCIMService<SCIMv11User> {

    public SCIMv11Client(final SCIMConnectorConfiguration config) {
        super(config);
    }

    /**
     * @param userId
     * @return User with userId id
     */
    public SCIMv11User getUser(final String userId) {
        WebClient webClient = getWebclient("Users", null).path(userId);
        return doGetUser(webClient, SCIMv11User.class, SCIMv11Attribute.class);
    }

    /**
     * @param user
     * @return Created User
     */
    public SCIMv11User createUser(final SCIMv11User user) {
        return SCIMv11User.class.cast(doCreateUser(user));
    }

    /**
     * @param user
     * @return Update User
     */
    public SCIMv11User updateUser(final SCIMv11User user) {
        return doUpdateUser(user, Collections.emptySet(), SCIMv11User.class);
    }

    /**
     * @param userId
     */
    public void deleteUser(final String userId) {
        WebClient webClient = getWebclient("Users", null)
                .path(userId);
        doDeleteUser(userId, webClient);
    }

    /**
     * @param userId
     */
    public void activateUser(final String userId) {
        doActivateUser(userId);
    }

    public boolean testService() {
        Set<String> attributesToGet = new HashSet<>();
        attributesToGet.add(SCIMAttributeUtils.USER_ATTRIBUTE_USERNAME);
        return getAllUsers(1, 1, attributesToGet) != null;
    }

    @Override
    protected PagedResults<SCIMv11User> deserializePagedResults(final String node) throws JsonProcessingException {
        return SCIMUtils.MAPPER.readValue(node, new TypeReference<PagedResults<SCIMv11User>>() {
        });
    }

}
