package org.spin.cloud.gateway.repository;


import org.jooq.UniqueKey;
import org.jooq.impl.Internal;
import org.spin.cloud.gateway.repository.tables.GatewayRouteDefinition;
import org.spin.cloud.gateway.repository.tables.records.GatewayRouteDefinitionRecord;

import javax.annotation.Generated;


/**
 * A class modelling foreign key relationships and constraints of tables of
 * the <code>xqc_admin</code> schema.
 */
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<GatewayRouteDefinitionRecord> KEY_GATEWAY_ROUTE_DEFINITION_PRIMARY = UniqueKeys0.KEY_GATEWAY_ROUTE_DEFINITION_PRIMARY;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Identities0 {
    }

    private static class UniqueKeys0 {
        public static final UniqueKey<GatewayRouteDefinitionRecord> KEY_GATEWAY_ROUTE_DEFINITION_PRIMARY = Internal.createUniqueKey(GatewayRouteDefinition.GATEWAY_ROUTE_DEFINITION, "KEY_gateway_route_definition_PRIMARY", GatewayRouteDefinition.GATEWAY_ROUTE_DEFINITION.ID);
    }
}
