package net.tirasa.connid.bundles.scim.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseMeta;
import net.tirasa.connid.bundles.scim.common.dto.SCIMBaseResource;
import net.tirasa.connid.bundles.scim.common.service.SCIMService;
import net.tirasa.connid.bundles.scim.common.utils.SCIMAttributeUtils;
import net.tirasa.connid.bundles.scim.common.utils.SCIMUtils;
import net.tirasa.connid.bundles.scim.v11.dto.PagedResults;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.identityconnectors.framework.spi.operations.*;

public abstract class AbstractSCIMConnector<T extends SCIMBaseResource<Attribute, ? extends SCIMBaseMeta>, ST extends SCIMService>
        implements Connector, CreateOp, DeleteOp, SchemaOp, SearchOp<Filter>, TestOp, UpdateOp {

    private static final Log LOG = Log.getLog(AbstractSCIMConnector.class);

    @Override
    public void executeQuery(ObjectClass objectClass, Filter query, ResultsHandler handler, OperationOptions options) {
        LOG.ok("Connector READ");

        Attribute key = null;
        if (query instanceof EqualsFilter) {
            Attribute filterAttr = ((EqualsFilter) query).getAttribute();
            if (filterAttr instanceof Uid
                    || ObjectClass.ACCOUNT.equals(objectClass)
                    || ObjectClass.GROUP.equals(objectClass)) {
                key = filterAttr;
            }
        }

        Set<String> attributesToGet = new HashSet<>();
        if (options.getAttributesToGet() != null) {
            attributesToGet.addAll(Arrays.asList(options.getAttributesToGet()));
        }

        if (ObjectClass.ACCOUNT.equals(objectClass)) {
            if (key == null) {
                List<T> users = null;
                int remainingResults = -1;
                int pagesSize = options.getPageSize() == null ? -1 : options.getPageSize();
                String cookie = options.getPagedResultsCookie();

                try {
                    if (pagesSize != -1) {
                        if (StringUtil.isNotBlank(cookie)) {
                            PagedResults<T> pagedResult =
                                    getClient().getAllUsers(Integer.valueOf(cookie), pagesSize, attributesToGet);
                            users = pagedResult.getResources();

                            cookie = users.size() >= pagesSize
                                    ? String.valueOf(pagedResult.getStartIndex() + users.size())
                                    : null;
                        } else {
                            PagedResults<T> pagedResult = getClient().getAllUsers(1, pagesSize, attributesToGet);
                            users = pagedResult.getResources();

                            cookie = users.size() >= pagesSize
                                    ? String.valueOf(pagedResult.getStartIndex() + users.size())
                                    : null;
                        }
                    } else {
                        users = getClient().getAllUsers(attributesToGet);
                    }
                } catch (Exception e) {
                    SCIMUtils.wrapGeneralError("While getting Users!", e);
                }

                for (T user : users) {
                    handler.handle(fromUser(user, attributesToGet));
                }

                if (handler instanceof SearchResultsHandler) {
                    ((SearchResultsHandler) handler).handleResult(new SearchResult(cookie, remainingResults));
                }
            } else {
                T result = null;
                if (Uid.NAME.equals(key.getName()) || SCIMAttributeUtils.USER_ATTRIBUTE_ID.equals(key.getName())) {
                    result = null;
                    try {
                        result = (T) getClient().getUser(AttributeUtil.getAsStringValue(key));
                    } catch (Exception e) {
                        SCIMUtils.wrapGeneralError("While getting User : "
                                + key.getName() + " - " + AttributeUtil.getAsStringValue(key), e);
                    }
                } else if (Name.NAME.equals(key.getName())) {
                    try {
                        List<T> users =
                                getClient().getAllUsers("username eq \"" + AttributeUtil.getAsStringValue(key) + "\"",
                                        attributesToGet);
                        if (!users.isEmpty()) {
                            result = users.get(0);
                        }
                    } catch (Exception e) {
                        SCIMUtils.wrapGeneralError("While getting User : "
                                + key.getName() + " - " + AttributeUtil.getAsStringValue(key), e);
                    }
                }
                if (result != null) {
                    handler.handle(fromUser(result, attributesToGet));
                }
            }
        } else {
            LOG.warn("Search of type {0} is not supported", objectClass.getObjectClassValue());
            throw new UnsupportedOperationException("Search of type" + objectClass.getObjectClassValue()
                    + " is not supported");
        }
    }

    protected abstract ConnectorObject fromUser(final T user, final Set<String> attributesToGet);

    protected abstract ST getClient();
}
