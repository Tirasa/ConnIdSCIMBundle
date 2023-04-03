package net.tirasa.connid.bundles.scim.common.dto;

import java.io.Serializable;
import java.util.Set;
import org.identityconnectors.framework.common.objects.Attribute;

public interface SCIMEnterpriseUser<T extends Serializable> extends Serializable {

    String getEmployeeNumber();

    void setEmployeeNumber(String employeeNumber);

    T getManager();

    void setManager(T manager);

    Set<Attribute> toAttributes(String id) throws IllegalArgumentException, IllegalAccessException;

}
