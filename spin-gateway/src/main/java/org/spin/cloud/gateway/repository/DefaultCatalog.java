package org.spin.cloud.gateway.repository;


import org.jooq.Schema;
import org.jooq.impl.CatalogImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
public class DefaultCatalog extends CatalogImpl {

    private static final long serialVersionUID = -1925598806;

    /**
     * The reference instance of <code></code>
     */
    public static final DefaultCatalog DEFAULT_CATALOG = new DefaultCatalog();

    /**
     * The schema <code>xqc_admin</code>.
     */
    public final XqcAdmin XQC_ADMIN = XqcAdmin.XQC_ADMIN;

    /**
     * No further instances allowed
     */
    private DefaultCatalog() {
        super("");
    }

    @Override
    public final List<Schema> getSchemas() {
        return new ArrayList<>(getSchemas0());
    }

    private List<Schema> getSchemas0() {
        return Arrays.asList(
            XqcAdmin.XQC_ADMIN);
    }
}
