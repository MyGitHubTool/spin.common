package org.spin.common.feign;

import feign.Feign;
import feign.Target;
import org.springframework.cloud.openfeign.FeignContext;

/**
 * @author Spencer Gibb
 */
public class DefaultTargeter implements Targeter {

    @Override
    public <T> T target(FeignClientFactoryBean factory, Feign.Builder feign, FeignContext context, Target.HardCodedTarget<T> target) {
        return feign.target(target);
    }
}
