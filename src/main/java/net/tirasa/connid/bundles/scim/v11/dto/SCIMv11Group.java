package net.tirasa.connid.bundles.scim.v11.dto;

import net.tirasa.connid.bundles.scim.common.dto.AbstractSCIMGroup;

public class SCIMv11Group extends AbstractSCIMGroup<SCIMv11Meta> {

    public static final String SCHEMA_URI = "urn:scim:schemas:core:1.0";

    public SCIMv11Group() {
        super(SCHEMA_URI, new SCIMv11Meta());
    }
}
