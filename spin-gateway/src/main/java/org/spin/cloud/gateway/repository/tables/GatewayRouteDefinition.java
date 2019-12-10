package org.spin.cloud.gateway.repository.tables;


import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.spin.cloud.gateway.repository.Indexes;
import org.spin.cloud.gateway.repository.Keys;
import org.spin.cloud.gateway.repository.XqcAdmin;
import org.spin.cloud.gateway.repository.tables.records.GatewayRouteDefinitionRecord;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


/**
 * This class is generated by jOOQ.
 */
public class GatewayRouteDefinition extends TableImpl<GatewayRouteDefinitionRecord> {

    private static final long serialVersionUID = 303260241;

    /**
     * The reference instance of <code>xqc_admin.gateway_route_definition</code>
     */
    public static final GatewayRouteDefinition GATEWAY_ROUTE_DEFINITION = new GatewayRouteDefinition();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<GatewayRouteDefinitionRecord> getRecordType() {
        return GatewayRouteDefinitionRecord.class;
    }

    /**
     * The column <code>xqc_admin.gateway_route_definition.id</code>.
     */
    public final TableField<GatewayRouteDefinitionRecord, String> ID = createField("id", org.jooq.impl.SQLDataType.VARCHAR(64).nullable(false), this, "");

    /**
     * The column <code>xqc_admin.gateway_route_definition.uri</code>.
     */
    public final TableField<GatewayRouteDefinitionRecord, String> URI = createField("uri", org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>xqc_admin.gateway_route_definition.predicates</code>.
     */
    public final TableField<GatewayRouteDefinitionRecord, String> PREDICATES = createField("predicates", org.jooq.impl.SQLDataType.VARCHAR(2000), this, "");

    /**
     * The column <code>xqc_admin.gateway_route_definition.filters</code>.
     */
    public final TableField<GatewayRouteDefinitionRecord, String> FILTERS = createField("filters", org.jooq.impl.SQLDataType.VARCHAR(2000), this, "");

    /**
     * The column <code>xqc_admin.gateway_route_definition.create_time</code>.
     */
    public final TableField<GatewayRouteDefinitionRecord, LocalDateTime> CREATE_TIME = createField("create_time", SQLDataType.LOCALDATETIME, this, "");

    /**
     * The column <code>xqc_admin.gateway_route_definition.update_time</code>.
     */
    public final TableField<GatewayRouteDefinitionRecord, LocalDateTime> UPDATE_TIME = createField("update_time", org.jooq.impl.SQLDataType.LOCALDATETIME, this, "");

    /**
     * The column <code>xqc_admin.gateway_route_definition.version</code>.
     */
    public final TableField<GatewayRouteDefinitionRecord, Integer> VERSION = createField("version", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>xqc_admin.gateway_route_definition.valid</code>.
     */
    public final TableField<GatewayRouteDefinitionRecord, Integer> VALID = createField("valid", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>xqc_admin.gateway_route_definition.order_no</code>.
     */
    public final TableField<GatewayRouteDefinitionRecord, Integer> ORDER_NO = createField("order_no", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * Create a <code>xqc_admin.gateway_route_definition</code> table reference
     */
    public GatewayRouteDefinition() {
        this(DSL.name("gateway_route_definition"), null);
    }

    /**
     * Create an aliased <code>xqc_admin.gateway_route_definition</code> table reference
     */
    public GatewayRouteDefinition(String alias) {
        this(DSL.name(alias), GATEWAY_ROUTE_DEFINITION);
    }

    /**
     * Create an aliased <code>xqc_admin.gateway_route_definition</code> table reference
     */
    public GatewayRouteDefinition(Name alias) {
        this(alias, GATEWAY_ROUTE_DEFINITION);
    }

    private GatewayRouteDefinition(Name alias, Table<GatewayRouteDefinitionRecord> aliased) {
        this(alias, aliased, null);
    }

    private GatewayRouteDefinition(Name alias, Table<GatewayRouteDefinitionRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> GatewayRouteDefinition(Table<O> child, ForeignKey<O, GatewayRouteDefinitionRecord> key) {
        super(child, key, GATEWAY_ROUTE_DEFINITION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return XqcAdmin.XQC_ADMIN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.GATEWAY_ROUTE_DEFINITION_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<GatewayRouteDefinitionRecord> getPrimaryKey() {
        return Keys.KEY_GATEWAY_ROUTE_DEFINITION_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<GatewayRouteDefinitionRecord>> getKeys() {
        return Arrays.asList(Keys.KEY_GATEWAY_ROUTE_DEFINITION_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GatewayRouteDefinition as(String alias) {
        return new GatewayRouteDefinition(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GatewayRouteDefinition as(Name alias) {
        return new GatewayRouteDefinition(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public GatewayRouteDefinition rename(String name) {
        return new GatewayRouteDefinition(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public GatewayRouteDefinition rename(Name name) {
        return new GatewayRouteDefinition(name, null);
    }
}