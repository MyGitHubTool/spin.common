package org.spin.cloud.gateway.repository;


import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.Internal;
import org.spin.cloud.gateway.repository.tables.GatewayRouteDefinition;


/**
 * A class modelling indexes of tables of the <code>xqc_admin</code> schema.
 */
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index GATEWAY_ROUTE_DEFINITION_PRIMARY = Indexes0.GATEWAY_ROUTE_DEFINITION_PRIMARY;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Indexes0 {
        public static Index GATEWAY_ROUTE_DEFINITION_PRIMARY = Internal.createIndex("PRIMARY", GatewayRouteDefinition.GATEWAY_ROUTE_DEFINITION, new OrderField[]{GatewayRouteDefinition.GATEWAY_ROUTE_DEFINITION.ID}, true);
    }
}
