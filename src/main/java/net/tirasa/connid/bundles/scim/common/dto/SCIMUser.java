package net.tirasa.connid.bundles.scim.common.dto;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.identityconnectors.framework.common.objects.Attribute;

public interface SCIMUser<AT, MT extends SCIMBaseMeta> extends SCIMBaseResource<AT, MT> {
    String getUserName();

    void setUserName(String username);

    void setPassword(String password);

    void setActive(Boolean active);

    void fillSCIMCustomAttributes(Set<Attribute> createAttributes, String customAttributesJSON);

    Map<String, List<Object>> getReturnedCustomAttributes();
}
