package org.spin.common.vo;

/**
 * 获取企业对象
 * @author Darrick
 */
public class CurrentEnterprise {

    private static final ThreadLocal<CurrentEnterprise> threadLocal = new ThreadLocal<>();

    private final Long orginId;

    private CurrentEnterprise(Long orginId) {
        this.orginId = orginId;
    }

    public static void setValue(Long orginId){
        CurrentEnterprise current = new CurrentEnterprise(orginId);
        threadLocal.set(current);
    }

    public static CurrentEnterprise getCurrent() {
        return threadLocal.get();
    }

    public static void clearCurrent() {
        threadLocal.remove();
    }

    public Long getOrginId(){ return orginId; }

}
