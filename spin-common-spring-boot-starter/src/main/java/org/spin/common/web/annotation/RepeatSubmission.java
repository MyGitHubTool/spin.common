package org.spin.common.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表单防重复提交注解
 *
 * @author aricXu
 * <p>Created by aricXu on 2019/7/29</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatSubmission {

    /**
     * 限制时间
     */
    int limitTime() default 1000;
}
