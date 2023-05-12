package net.tirasa.connid.bundles.scim.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.tirasa.connid.bundles.scim.common.SCIMConnectorConfiguration;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.framework.common.objects.Attribute;

public class AbstractSCIMGroup<MT extends SCIMBaseMeta> extends AbstractSCIMBaseResource<MT> implements SCIMGroup<MT> {

    protected String displayName;

    protected List<BaseResourceReference> members = new ArrayList<>();

    protected final Set<String> schemas = new TreeSet<>();

    @JsonIgnore protected String baseSchema;

    protected AbstractSCIMGroup() {
    }

    protected AbstractSCIMGroup(final String schemaUri, final MT meta) {
        super(meta);
        this.baseSchema = schemaUri;
        schemas.add(baseSchema);
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    @Override public List<BaseResourceReference> getMembers() {
        return members;
    }

    @JsonIgnore @Override @SuppressWarnings("unchecked")
    public Set<Attribute> toAttributes(final Class<?> type, final SCIMConnectorConfiguration configuration)
            throws IllegalArgumentException, IllegalAccessException {
        Set<Attribute> attrs = new HashSet<>();

        FieldUtils.getAllFieldsList(type).stream()
                .filter(f -> !"LOG".equals(f.getName()) && !"serialVersionUID".equals(f.getName())
                        && !"RESOURCE_NAME".equals(f.getName()) && !"SCHEMA_URI".equals(f.getName())).forEach(field -> {
                    try {
                        field.setAccessible(true);
                        // SCIM-3 manage enterprise user
                        if (!field.isAnnotationPresent(JsonIgnore.class) && !SCIMUtils.isEmptyObject(field.get(this))) {
                            Object objInstance = field.get(this);

                            if (field.getGenericType().toString().contains(SCIMBaseMeta.class.getName())) {
                                if (field.getType().equals(List.class)) {
                                    List<MT> list = (List<MT>) objInstance;
                                    for (MT scimMeta : list) {
                                        SCIMAttributeUtils.addAttribute(scimMeta.toAttributes(), attrs,
                                                field.getType());
                                    }
                                } else {
                                    SCIMAttributeUtils.addAttribute(SCIMBaseMeta.class.cast(objInstance).toAttributes(),
                                            attrs, field.getType());
                                }
                            } else {
                                attrs.add(SCIMAttributeUtils.buildAttributeFromClassField(field, this).build());
                            }
                        }
                    } catch (IllegalAccessException e) {
                        LOG.error(e, "Unable to build user attributes by reflection");
                    }
                });

        return attrs;
    }

    @JsonIgnore @Override public void fromAttributes(final Set<Attribute> attributes) {
        attributes.stream().filter(attribute -> !CollectionUtil.isEmpty(attribute.getValue())).forEach(attribute -> {
            try {
                doSetAttribute(attribute.getName(), attribute.getValue());
            } catch (Exception e) {
                LOG.warn(e, "While populating User field from ConnId attribute: {0}", attribute);
            }
        });
    }

    @JsonIgnore @SuppressWarnings("unchecked")
    private void doSetAttribute(final String name, final List<Object> values) {
        if ("displayName".equals(name)) {
            this.displayName = String.class.cast(values.get(0));
        } else if ("members".equals(name)) {
            values.forEach(value -> members.add(
                    new BaseResourceReference.Builder().value(value.toString()).ref("../Users/" + value).build()));
        }
    }
}
