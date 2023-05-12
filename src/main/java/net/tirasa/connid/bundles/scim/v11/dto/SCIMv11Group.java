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

package net.tirasa.connid.bundles.scim.v11.dto;

import net.tirasa.connid.bundles.scim.common.dto.AbstractSCIMGroup;

public class SCIMv11Group extends AbstractSCIMGroup<SCIMv11Meta> {

    public static final String SCHEMA_URI = "urn:scim:schemas:core:1.0";

    public SCIMv11Group() {
        super(SCHEMA_URI, new SCIMv11Meta());
    }
}
