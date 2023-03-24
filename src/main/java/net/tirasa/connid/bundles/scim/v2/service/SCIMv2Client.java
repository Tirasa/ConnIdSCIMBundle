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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.Set;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.service.AbstractSCIMService;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import net.tirasa.connid.bundles.scim.v11.dto.PagedResults;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2Attribute;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2User;
import org.identityconnectors.framework.common.objects.Attribute;

public class SCIMv2Client extends AbstractSCIMService<SCIMv2User> {

    public SCIMv2Client(final SCIMConnectorConfiguration config) {
        super(config);
    }

    /**
     * @param userId
     * @return User with userId id
     */
    @Override
    public SCIMv2User getUser(final String userId) {
        return doGetUser(getWebclient("Users", null).path(userId), SCIMv2User.class, SCIMv2Attribute.class);
    }

    /**
     * @param user
     * @return Created User
     */
    @Override
    public SCIMv2User createUser(final SCIMv2User user) {
        return doCreateUser(user);
    }

    /**
     * @param user
     * @param replaceAttributes
     * @return Update User
     */
    public SCIMv2User updateUser(final SCIMv2User user, final Set<Attribute> replaceAttributes) {
        return doUpdateUser(user, replaceAttributes, SCIMv2User.class);
    }

    @Override
    public SCIMv2User updateUser(final SCIMv2User user) {
        return doUpdateUser(user, Collections.emptySet(), SCIMv2User.class);
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
    protected PagedResults<SCIMv2User> deserializePagedResults(final String node) throws JsonProcessingException {
        return SCIMUtils.MAPPER.readValue(node, new TypeReference<PagedResults<SCIMv2User>>() {
        });
    }

}
