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
package net.tirasa.connid.bundles.scim.common.service;

import java.util.List;
import java.util.Set;
import net.tirasa.connid.bundles.scim.common.dto.PagedResults;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseMeta;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBasePatch;
import net.tirasa.connid.bundles.scim.common.dto.SCIMEnterpriseUser;
import net.tirasa.connid.bundles.scim.common.dto.SCIMGroup;
import net.tirasa.connid.bundles.scim.common.dto.SCIMUser;

public interface SCIMService<UT extends SCIMUser<? extends SCIMBaseMeta, ? extends SCIMEnterpriseUser<?>>, 
        GT extends SCIMGroup<? extends SCIMBaseMeta>, P extends SCIMBasePatch> {

    PagedResults<UT> getAllUsers(Integer valueOf, Integer pagesSize, Set<String> attributesToGet);

    List<UT> getAllUsers(Set<String> attributesToGet);

    UT getUser(String userId);

    List<UT> getAllUsers(String s, Set<String> attributesToGet);

    UT createUser(UT user);

    void deleteUser(String userId);

    UT updateUser(UT user);

    void activateUser(String userId);

    boolean testService();

    PagedResults<GT> getAllGroups(Integer startIndex, Integer count);

    List<GT> getAllGroups();

    GT getGroup(String groupId);

    List<GT> getAllGroups(String filter);

    GT createGroup(GT group);

    void deleteGroup(String groupId);

    GT updateGroup(GT group);

    GT updateGroup(String groupId, P groupPatch);
}
