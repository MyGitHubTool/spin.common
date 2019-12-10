package org.spin.common.web.interceptor;

import com.alibaba.fastjson.JSON;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.common.internal.NetworkUtils;
import org.spin.common.redis.RedisUtil;
import org.spin.common.util.ApolloKeyUtil;
import org.spin.common.vo.CurrentUser;
import org.spin.common.web.InternalWhiteList;
import org.spin.common.web.RestfulResponse;
import org.spin.common.web.ScopeType;
import org.spin.common.web.annotation.Auth;
import org.spin.core.ErrorCode;
import org.spin.core.util.JsonUtils;
import org.spin.core.util.StringUtils;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户权限拦截器
 *
 * @author wangy QQ 837195190
 * <p>Created by wangy on 2019/3/14.</p>
 */
public class UserAuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(UserAuthInterceptor.class);

    private InternalWhiteList whiteList;

    private DiscoveryClient discoveryClient;

    private EurekaClient eurekaClient;

    private RedisUtil redisUtil;

    /**
     * 用户权限-用户角色集合（1,2,3）
     */
    public static final String USER_PERMISSION_ROLES= "user:permission:roles:";
    /**
     * 用户权限-角色对应菜单url集合(1:{"http://111","http://222"})
     */
    public static final String USER_PERMISSION_ROLE_MENUS= "user:permission:role:menus:";
    /**
     * 用户权限-所有菜单urls
     */
    public static final String USER_PERMISSION_MENUS= "user:permission:menus:";
    /**
     * token对应的authGroup
     */
    public static final String USER_TOKEN_AUTH_GROUP= "user:permission:token:authGroup:";

    /**
     * 判断请求是否来自feign
     */
    public static final String FROM_FEIGN= "fromFeign";

    /**
     * 判断请求是否来自feign
     */
    public static final String NEED_AUTH= "need.auth";


    public UserAuthInterceptor(InternalWhiteList whiteList) {
        this.whiteList = whiteList;
    }

    public UserAuthInterceptor(DiscoveryClient discoveryClient, EurekaClient eurekaClient,RedisUtil redisUtil) {
        this.discoveryClient = discoveryClient;
        this.eurekaClient = eurekaClient;
        this.redisUtil = redisUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 是否调用方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 认证信息判断
        Method method = ((HandlerMethod) handler).getMethod();

        Auth authAnno = AnnotatedElementUtils.getMergedAnnotation(method, Auth.class);
        if (null == authAnno) {
            responseWrite(response, ErrorCode.OTHER, "接口定义不正确");
            return false;
        }

        //boolean internal = internalRequest(request);
        //if (authAnno.scope() == ScopeType.INTERNAL && !internal) {
        //    responseWrite(response, ErrorCode.ACCESS_DENINED, "该接口仅允许内部调用: " + request.getRequestURI());
        //    return false;
        //}

        if (authAnno.scope() == ScopeType.INTERNAL || authAnno.scope() == ScopeType.OPEN_UNAUTH) {
            if (!isInnerApi(request)) {
                responseWrite(response, ErrorCode.ACCESS_DENINED, "请勿进行非法请求!" + request.getRemoteHost());
                return false;
            }
            return true;
        }

        // 用户信息
        Enumeration<String> enumeration = request.getHeaders(HttpHeaders.FROM);
        CurrentUser currentUser = null;
        if (enumeration.hasMoreElements()) {
            String user = enumeration.nextElement();
            if (StringUtils.isNotEmpty(user)) {
                currentUser = CurrentUser.setCurrent(StringUtils.urlDecode(user));
            }
        }

       boolean auth = authAnno.value();
       //if (auth && authAnno.scope() == ScopeType.OPEN_UNAUTH) {
       //    auth = !internal;
       //}

        ErrorCode errorCode = null;
        if (null == currentUser) {
            CurrentUser.clearCurrent();
            errorCode = ErrorCode.ACCESS_DENINED;
        } else if (ErrorCode.TOKEN_EXPIRED.getCode() == -currentUser.getId().intValue()) {
            CurrentUser.clearCurrent();
            errorCode = ErrorCode.TOKEN_EXPIRED;
        } else if (ErrorCode.TOKEN_INVALID.getCode() == -currentUser.getId().intValue()) {
            CurrentUser.clearCurrent();
            errorCode = ErrorCode.TOKEN_INVALID;
        }

        if (null != errorCode && errorCode != ErrorCode.ACCESS_DENINED) {
            logger.warn("非法的Token: {}", errorCode.toString());
        }

        if (!auth) {
            return true;
        } else if (null != errorCode) {
            responseWrite(response, errorCode);
            return false;
        }

        // 权限信息
       //String[] permissions = authAnno.permissions();
       //if (permissions.length == 0) {
       //    return true;
       //}
       //Set<String> allPermissions = PermissionUtils.getUserPermissions(currentUser.getId());
       //if (allPermissions.containsAll(Arrays.asList(permissions))) {
       //    return true;
       //}


        //是否权限校验
        String needAuthStr = ApolloKeyUtil.getString(NEED_AUTH);
        //判断请求是否来自feign
        String fromFeign = request.getHeader(FROM_FEIGN);

        if("true".equals(needAuthStr) && StringUtils.isEmpty(fromFeign)){
            //校验权限(只检验auth=true的url)
            boolean authResult = permissionCheck(request, currentUser);
            if (authResult) {
                return true;
            }else {
                // 无效的授权(用户操作未授权)
                errorCode = ErrorCode.USER_OPERATE_NO_PERMISSION;
                responseWrite(response, ErrorCode.USER_OPERATE_NO_PERMISSION);
            }
        }else {
            return true;
        }

        if(errorCode!= ErrorCode.USER_OPERATE_NO_PERMISSION){
            // 无效的授权
            responseWrite(response, ErrorCode.ACCESS_DENINED);
        }

        return false;

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // do nothing
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // do nothing
    }

    /**
     * 判断请求是否来自内部
     * <pre>
     *     内部的定义:
     *     1. 不允许来源于网关
     *     2. 在白名单中，或者属于同一子网(不允许跨VLAN)，或者来源与注册中心中的其他服务
     * </pre>
     *
     * @param request 请求
     * @return 是否来自内部
     */
    private boolean internalRequest(HttpServletRequest request) {
        return !StringUtils.toStringEmpty(request.getHeader(HttpHeaders.REFERER)).endsWith("GATEWAY")
            && (whiteList.containsOne(request.getRemoteAddr(), request.getRemoteHost())
            || NetworkUtils.inSameVlan(request.getRemoteHost()) || NetworkUtils.inSameVlan(request.getRemoteAddr()));
    }

    private void responseWrite(HttpServletResponse response, ErrorCode errorCode, String... message) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.setHeader("Encoded", "1");
            response.getWriter().write(JsonUtils.toJson(RestfulResponse
                .error(errorCode, ((null == message || message.length == 0 || StringUtils.isEmpty(message[0])) ? errorCode.getDesc() : message[0]))));
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * 是否为内部接口
     * @param request 请求体
     * @return true or false
     */
    private boolean isInnerApi(HttpServletRequest request){
        List<String> servicesList = discoveryClient.getServices();
        // 从eureka客户端中拿到真实ip地址集合，然后转换set去重
        Set<String> collect = servicesList.stream().filter(service -> !"bnd-gateway".equalsIgnoreCase(service))
            .flatMap(serviceName -> eurekaClient.getInstancesByVipAddress(serviceName, false)
                .stream()).map(InstanceInfo::getIPAddr).collect(Collectors.toSet());

        return collect.contains(request.getRemoteHost());
    }

    /**
     *  进行url过滤
     * @param request
     * @param currentUser
     * @return
     */
    private boolean permissionCheck(HttpServletRequest request, CurrentUser currentUser) {
        try {
            String contextPath = request.getServletPath();
            if(StringUtils.isEmpty(contextPath)){
                return true;
            }
            //获取权限组
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            String authGroupAndBusinessId = redisUtil.getValue(USER_TOKEN_AUTH_GROUP + header);
            if(StringUtils.isEmpty(header) || StringUtils.isEmpty(authGroupAndBusinessId)){
                return true;
            }
            //去除多余引号
            authGroupAndBusinessId = authGroupAndBusinessId.replaceAll("\\\"", "");
            String[] authGroupArr = authGroupAndBusinessId.split(",");
            if(authGroupArr!=null && authGroupArr.length>=2){
                List<String> urls = new ArrayList<>();
                //获取用户所有角色
                String roleIds = redisUtil.getValue(USER_PERMISSION_ROLES +currentUser.getId()+":"+ authGroupArr[0]+":"+authGroupArr[1]);
                if(StringUtils.isNotEmpty(roleIds)){
                    //去除多余引号
                    roleIds = roleIds.replaceAll("\\\"", "");
                    String[] roleIdArray = roleIds.split(",");
                    if(roleIdArray!=null && roleIdArray.length>0){
                        for (String roleId : roleIdArray) {
                            //根据roleId获取菜单urls
                            String roleUrls = redisUtil.getValue(USER_PERMISSION_ROLE_MENUS + Integer.parseInt(roleId));
                            if(StringUtils.isNotEmpty(roleUrls)){
                                List<String> urlList = JSON.parseArray(roleUrls, String.class);
                                if(!CollectionUtils.isEmpty(urlList)){
                                    urls.addAll(urlList);
                                }
                            }
                        }
                    }
                }
                //获取权限组下所有菜单
                String menus = redisUtil.getValue(USER_PERMISSION_MENUS+authGroupArr[1]);
                List<String> allAuthUrls = null;
                if(StringUtils.isNotEmpty(menus)){
                    allAuthUrls = JSON.parseArray(menus, String.class);
                }
                //优先判断小的
                //小的没有 大的有
                if((CollectionUtils.isEmpty(urls) || !isMatched(contextPath,urls))
                    && !CollectionUtils.isEmpty(allAuthUrls) && isMatched(contextPath,allAuthUrls)){
                    logger.error("no auth userId : {} , path : {}",currentUser.getId(),contextPath);
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("permissionCheck error - {}", e.toString());
            return false;
        }
        return true;
    }

    /**
     * 匹配请求url
     * @param path
     * @param urls
     * @return
     */
    private boolean isMatched(String path,List<String> urls){
        boolean matched = false;
        if(StringUtils.isNotEmpty(path) && !CollectionUtils.isEmpty(urls)){
            //匹配url
            AntPathMatcher matcher = new AntPathMatcher();
            for (String url : urls) {
                if(StringUtils.isNotEmpty(url) && matcher.match(url,path)){
                   matched = true;
                   break;
                }
            }
        }
        return matched;
    }








}
