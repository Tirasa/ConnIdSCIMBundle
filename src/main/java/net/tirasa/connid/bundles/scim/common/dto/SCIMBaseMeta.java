/**
 * Copyright © 2018 ConnId (connid-dev@googlegroups.com)
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.v11.dto.SCIMv11User;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Attribute;

public class SCIMBaseMeta implements Serializable {

    @JsonIgnore
    private static final Log LOG = Log.getLog(SCIMBaseMeta.class);

    @JsonProperty
    protected String created;

    @JsonProperty
    protected String lastModified;

    @JsonProperty
    protected String location;

    @JsonProperty
    protected String version;

    public String getCreated() {
        return created;
    }

    public void setCreated(final String created) {
        this.created = created;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(final String lastModified) {
        this.lastModified = lastModified;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public Set<Attribute> toAttributes() throws IllegalArgumentException, IllegalAccessException {
        Set<Attribute> attrs = new HashSet<>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(JsonIgnore.class)) {
                field.setAccessible(true);
                attrs.add(SCIMAttributeUtils.doBuildAttributeFromClassField(
                        field.get(this),
                        SCIMAttributeUtils.SCIM_USER_META.concat(".")
                                .concat(field.getName()),
                        field.getType()).build());
            }
        }
        return attrs;
    }

}
