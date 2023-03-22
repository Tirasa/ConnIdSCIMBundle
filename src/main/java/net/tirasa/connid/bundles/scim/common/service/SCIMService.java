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
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseMeta;
import net.tirasa.connid.bundles.scim.common.dto.SCIMUser;
import net.tirasa.connid.bundles.scim.v11.dto.PagedResults;
import org.identityconnectors.framework.common.objects.Attribute;

public interface SCIMService<T extends SCIMUser<Attribute, ? extends SCIMBaseMeta>> {

    PagedResults<T> getAllUsers(Integer valueOf, Integer pagesSize, Set<String> attributesToGet);

    List<T> getAllUsers(Set<String> attributesToGet);

    T getUser(String asStringValue);

    List<T> getAllUsers(String s, Set<String> attributesToGet);

    T createUser(T user);

    void deleteUser(String userId);

    T updateUser(T user);

    boolean testService();
}
