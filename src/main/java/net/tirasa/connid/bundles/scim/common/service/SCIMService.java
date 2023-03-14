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

    T createUser(final T user);

    void deleteUser(String userId);

    T updateUser(T user);

    boolean testService();
}
