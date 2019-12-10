package org.spin.cloud.gateway.cache;

import java.util.List;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/6/11</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class ServiceAuthInfo {
    private String path;
    private String name;
    private boolean auth;
    private List<String> permissions;
    private boolean internal;
}
