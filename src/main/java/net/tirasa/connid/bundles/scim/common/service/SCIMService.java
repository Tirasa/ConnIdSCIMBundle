package net.tirasa.connid.bundles.scim.common.service;

import java.util.List;
import java.util.Set;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseMeta;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseResource;
import net.tirasa.connid.bundles.scim.v11.dto.PagedResults;
import net.tirasa.connid.bundles.scim.v2.dto.SCIMv2User;
import org.identityconnectors.framework.common.objects.Attribute;

public interface SCIMService<T extends SCIMBaseResource<Attribute, ? extends SCIMBaseMeta>> {

    PagedResults<T> getAllUsers(Integer valueOf, Integer pagesSize, Set<String> attributesToGet);

    List<T> getAllUsers(Set<String> attributesToGet);

    T getUser(String asStringValue);

    List<T> getAllUsers(String s, Set<String> attributesToGet);
}
