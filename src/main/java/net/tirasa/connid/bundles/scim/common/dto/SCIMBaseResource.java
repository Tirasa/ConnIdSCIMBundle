package net.tirasa.connid.bundles.scim.common.dto;

import java.io.Serializable;
import java.util.Set;

public interface SCIMBaseResource<AT, MT extends SCIMBaseMeta> extends Serializable {

    Set<String> getSchemas();

    void setSchemas(final Set<String> schemas);

    MT getMeta();

    void setMeta(MT meta);

    String getId();

    void setId(String id);

    String getExternalId();

    void setExternalId(String externalId);

    String getBaseSchema();

    Set<AT> toAttributes() throws IllegalArgumentException, IllegalAccessException;

    void fromAttributes(Set<AT> attributes);

}
