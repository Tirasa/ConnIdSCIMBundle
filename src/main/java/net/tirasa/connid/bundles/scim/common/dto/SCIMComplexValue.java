package net.tirasa.connid.bundles.scim.common.dto;

import java.io.Serializable;
import java.util.Set;
import org.identityconnectors.framework.common.objects.Attribute;

public interface SCIMComplexValue extends Serializable {

    void setValue(String value);

    String getValue();

    Set<Attribute> toAttributes(String id) throws IllegalArgumentException, IllegalAccessException;
}
