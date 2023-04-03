/**
 * Copyright (C) 2018 ConnId (connid-dev@googlegroups.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tirasa.connid.bundles.scim.common.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.identityconnectors.framework.common.objects.Attribute;

public interface SCIMUser<AT, MT extends SCIMBaseMeta, EUT extends SCIMEnterpriseUser>
        extends SCIMBaseResource<AT, MT> {

    String getUserName();

    void setUserName(String username);

    void setPassword(String password);

    void setActive(Boolean active);

    void fillSCIMCustomAttributes(Set<Attribute> createAttributes, String customAttributesJSON);

    Map<String, List<Object>> getReturnedCustomAttributes();

    Map<? extends SCIMBaseAttribute<?>, List<Object>> getSCIMCustomAttributes();

    EUT getEnterpriseUser();

    void fillEnterpriseUser(Set<Attribute> attributes);

    void setEnterpriseUser(EUT enterpriseUser);
}
