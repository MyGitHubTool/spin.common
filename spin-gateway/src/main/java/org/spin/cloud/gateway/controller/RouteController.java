package org.spin.cloud.gateway.controller;

import org.spin.cloud.gateway.filter.TokenResolveFilter;
import org.spin.cloud.gateway.service.DynamicRouteService;
import org.spin.cloud.gateway.vo.RestfulResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * 动态路由控制器
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/13</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@RestController
@RequestMapping("v1/route")
public class RouteController {

    private final DynamicRouteService routeService;

    @Autowired
    public RouteController(DynamicRouteService routeService) {
        this.routeService = routeService;
    }

    /**
     * 查询所有动态路由信息
     *
     * @return 动态路由信息
     */
    @GetMapping
    public Mono<RestfulResponse> getRoutes() {
        return routeService.getRoutes().collect(Collectors.toList()).map(RestfulResponse::ok);
    }

    /**
     * 查询指定的动态路由信息
     *
     * @param routeId 动态路由ID
     * @return 动态路由信息
     */
    @GetMapping("{routeId}")
    public Mono<RestfulResponse> getRoute(@PathVariable String routeId) {
        return routeService.getRoute(routeId).map(RestfulResponse::ok);
    }

    /**
     * 重新加载动态路由
     *
     * @return 操作结果
     */
    @GetMapping("reload")
    public Mono<RestfulResponse> reloadRoute() {
        routeService.sendRouteReloadEvent();
        return Mono.just(RestfulResponse.ok());
    }

    /**
     * 新增动态路由
     *
     * @param definition 动态路由定义
     * @return 操作结果
     */
    @PostMapping
    public Mono<RestfulResponse> addRoute(@RequestBody RouteDefinition definition) {
        routeService.sendRouteAddEvent(definition);
        return Mono.just(RestfulResponse.ok());
    }

    /**
     * 更新动态路由
     *
     * @param definition 动态路由定义
     * @return 操作结果
     */
    @PutMapping
    public Mono<RestfulResponse> updateRoute(@RequestBody RouteDefinition definition) {
        routeService.sendRouteUpdateEvent(definition);
        return Mono.just(RestfulResponse.ok());
    }

    /**
     * 删除指定动态路由
     *
     * @param routeId 动态路由ID
     * @return 操作结果
     */
    @DeleteMapping("{routeId}")
    public Mono<RestfulResponse> deleteRoute(@PathVariable String routeId) {
        routeService.sendRouteDeleteEvent(routeId);
        return Mono.just(RestfulResponse.ok());
    }

    /**
     * 重新加载黑名单
     *
     * @return 操作结果
     */
    @PutMapping("blackList/init")
    public Mono<RestfulResponse> reloadBlackList() {
        TokenResolveFilter.initBlackList();
        return Mono.just(RestfulResponse.ok());
    }


//    public static void smain(String[] args) throws Exception {
//        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
//            "<configuration xmlns=\"http://www.jooq.org/xsd/jooq-codegen-3.9.2.xsd\">\n" +
//            "  <!-- Configure the database connection here -->\n" +
//            "  <jdbc>\n" +
//            "    <driver>com.mysql.cj.jdbc.Driver</driver>\n" +
//            "    <!-- 数据库url -->\n" +
//            "    <url>jdbc:mysql://192.168.13.178:3306/xqc_admin?useUnicode=true&amp;characterEncoding=UTF-8</url>\n" +
//            "    <!-- 数据库账号 -->\n" +
//            "    <user>root</user>\n" +
//            "    <!-- 数据库账号密码 -->\n" +
//            "    <password>admin</password>\n" +
//            "  </jdbc>\n" +
//            "\n" +
//            "  <generator>\n" +
//            "    <!-- The default code generator. You can override this one, to generate your own code style.\n" +
//            "         Supported generators:\n" +
//            "         - org.jooq.util.JavaGenerator\n" +
//            "         - org.jooq.util.ScalaGenerator\n" +
//            "         Defaults to org.jooq.util.JavaGenerator -->\n" +
//            "    <name>org.jooq.codegen.JavaGenerator</name>\n" +
//            "\n" +
//            "    <database>\n" +
//            "      <!-- The database type. The format here is:\n" +
//            "           org.util.[database].[database]Database -->\n" +
//            "      <name>org.jooq.meta.mysql.MySQLDatabase</name>\n" +
//            "\n" +
//            "      <!-- The database schema (or in the absence of schema support, in your RDBMS this\n" +
//            "           can be the owner, user, database name) to be generated -->\n" +
//            "      <inputSchema>xqc_admin</inputSchema>\n" +
//            "\n" +
//            "      <!-- All elements that are generated from your schema\n" +
//            "           (A Java regular expression. Use the pipe to separate several expressions)\n" +
//            "           Watch out for case-sensitivity. Depending on your database, this might be important! -->\n" +
//            "      <includes>.*</includes>\n" +
//            "\n" +
//            "      <!-- All elements that are excluded from your schema\n" +
//            "           (A Java regular expression. Use the pipe to separate several expressions).\n" +
//            "           Excludes match before includes, i.e. excludes have a higher priority -->\n" +
//            "      <excludes></excludes>\n" +
//            "    </database>\n" +
//            "\n" +
//            "    <target>\n" +
//            "      <!-- The destination package of your generated classes (within the destination directory) -->\n" +
//            "      <!-- 生成的包名，生成的类在此包下 -->\n" +
//            "      <packageName>org.spin.cloud.gateway.repository</packageName>\n" +
//            "\n" +
//            "      <!-- The destination directory of your generated classes. Using Maven directory layout here -->\n" +
//            "      <!-- 输出的目录 -->\n" +
//            "      <directory>D:\\aaa</directory>\n" +
//            "    </target>\n" +
//            "  </generator>\n" +
//            "</configuration>";
//        GenerationTool.generate(xml);
//    }

//    public static void main(String[] args) {
//        // 用户名
//        String userName = "root";
//        // 密码
//        String password = "admin";
//        // mysql连接url
//        String url = "jdbc:mysql://192.168.13.184:3306/xqc_admin?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true&serverTimezone=PRC&useSSL=false";
//
//        // Connection is the only JDBC resource that we need
//        // PreparedStatement and ResultSet are handled by jOOQ, internally
//        try (Connection conn = DriverManager.getConnection(url, userName, password)) {
//            DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
//            Result<Record> result = create.select().from(GatewayRouteDefinition.GATEWAY_ROUTE_DEFINITION).fetch();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
}
