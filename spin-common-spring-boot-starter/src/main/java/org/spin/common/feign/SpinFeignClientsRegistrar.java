/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spin.common.feign;

import org.spin.core.util.StringUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * @author Spencer Gibb
 * @author Jakub Narloch
 * @author Venil Noronha
 * @author Gang Li
 */
class SpinFeignClientsRegistrar
    implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

    // patterned after Spring Integration IntegrationComponentScanRegistrar
    // and RibbonClientsConfigurationRegistgrar

    private ResourceLoader resourceLoader;

    private Environment environment;

    private static Properties resolver;

    SpinFeignClientsRegistrar() {
    }

    private static void validateFallback(final Class clazz) {
        Assert.isTrue(!clazz.isInterface(),
            "Fallback class must implement the interface annotated by @FeignClient");
    }

    private static void validateFallbackFactory(final Class clazz) {
        Assert.isTrue(!clazz.isInterface(), "Fallback factory must produce instances "
            + "of fallback classes that implement the interface annotated by @FeignClient");
    }

    private static String getName(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }

        String host = null;
        try {
            String url;
            if (!name.startsWith("http://") && !name.startsWith("https://")) {
                url = "http://" + name;
            } else {
                url = name;
            }
            host = new URI(url).getHost();

        } catch (URISyntaxException ignore) {
            // do nothing
        }
        Assert.state(host != null, "Service id not legal hostname (" + name + ")");
        return name;
    }

    private static String getUrl(String url) {
        if (StringUtils.isNotBlank(url) && !(url.startsWith("#{") && url.contains("}"))) {
            if (!url.contains("://")) {
                url = "http://" + url;
            }
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(url + " is malformed", e);
            }
        }
        return url;
    }

    private static String getPath(String path) {
        if (StringUtils.isNotBlank(path)) {
            path = path.trim();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
        }
        return path;
    }

    @Override
    public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata metadata,
                                        @NonNull BeanDefinitionRegistry registry) {
        registerDefaultConfiguration(metadata, registry);
        registerFeignClients(metadata, registry);
    }

    private void registerDefaultConfiguration(AnnotationMetadata metadata,
                                              BeanDefinitionRegistry registry) {
        Map<String, Object> defaultAttrs = metadata
            .getAnnotationAttributes(EnableSpinFeignClients.class.getName(), true);

        if (defaultAttrs != null && defaultAttrs.containsKey("defaultConfiguration")) {
            String name;
            if (metadata.hasEnclosingClass()) {
                name = "default." + metadata.getEnclosingClassName();
            } else {
                name = "default." + metadata.getClassName();
            }
            registerClientConfiguration(registry, name,
                defaultAttrs.get("defaultConfiguration"));
        }
    }

    private void registerFeignClients(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);

        Set<String> basePackages;

        Map<String, Object> attrs = metadata.getAnnotationAttributes(EnableSpinFeignClients.class.getName());
        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(FeignClient.class);
        final Class<?>[] clients = attrs == null ? null : (Class<?>[]) attrs.get("clients");

        if (clients == null || clients.length == 0) {
            scanner.addIncludeFilter(annotationTypeFilter);
            basePackages = getBasePackages(metadata);
        } else {
            final Set<String> clientClasses = new HashSet<>();
            basePackages = new HashSet<>();
            for (Class<?> clazz : clients) {
                basePackages.add(ClassUtils.getPackageName(clazz));
                clientClasses.add(clazz.getCanonicalName());
            }
            AbstractClassTestingTypeFilter filter = new AbstractClassTestingTypeFilter() {
                @Override
                protected boolean match(ClassMetadata metadata) {
                    String cleaned = metadata.getClassName().replaceAll("\\$", ".");
                    return clientClasses.contains(cleaned);
                }
            };
            scanner.addIncludeFilter(new AllTypeFilter(Arrays.asList(filter, annotationTypeFilter)));
        }

        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {
                    // verify annotated class is an interface
                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                    Assert.isTrue(annotationMetadata.isInterface(), "@FeignClient can only be specified on an interface");

                    Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(FeignClient.class.getCanonicalName());

                    if (null == attributes) {
                        continue;
                    }
                    String name = getClientName(attributes);
                    registerClientConfiguration(registry, name, attributes.get("configuration"));

                    registerFeignClient(registry, annotationMetadata, attributes);
                }
            }
        }
    }

    private void registerFeignClient(BeanDefinitionRegistry registry, AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {
        String className = annotationMetadata.getClassName();
        BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(FeignClientFactoryBean.class);
        validate(attributes);
        String name = getName(attributes);
        String defUrl = StringUtils.trimToEmpty(resolver.getProperty(StringUtils.toUpperCase(name)));
        String url = getUrl(attributes);
        definition.addPropertyValue("url", StringUtils.isEmpty(url) ? defUrl : url);
        definition.addPropertyValue("path", getPath(attributes));
        definition.addPropertyValue("name", name);
        String contextId = getContextId(attributes);
        definition.addPropertyValue("contextId", contextId);
        definition.addPropertyValue("type", className);
        definition.addPropertyValue("decode404", attributes.get("decode404"));
        definition.addPropertyValue("fallback", attributes.get("fallback"));
        definition.addPropertyValue("fallbackFactory", attributes.get("fallbackFactory"));
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

        String alias = contextId + "FeignClient";
        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();

        boolean primary = (Boolean) attributes.get("primary"); // has a default, won't be
        // null

        beanDefinition.setPrimary(primary);

        String qualifier = getQualifier(attributes);
        if (StringUtils.isNotBlank(qualifier)) {
            alias = qualifier;
        }

        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className,
            new String[]{alias});
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }

    private void validate(Map<String, Object> attributes) {
        AnnotationAttributes annotation = AnnotationAttributes.fromMap(attributes);
        // This blows up if an aliased property is overspecified
        // FIXME annotation.getAliasedString("name", FeignClient.class, null);
        validateFallback(annotation.getClass("fallback"));
        validateFallbackFactory(annotation.getClass("fallbackFactory"));
    }

    /* for testing */
    private String getName(Map<String, Object> attributes) {
        String name = (String) attributes.get("serviceId");
        if (StringUtils.isBlank(name)) {
            name = (String) attributes.get("name");
        }
        if (StringUtils.isBlank(name)) {
            name = (String) attributes.get("value");
        }
        name = resolve(name);
        return getName(name);
    }

    private String getContextId(Map<String, Object> attributes) {
        String contextId = (String) attributes.get("contextId");
        if (StringUtils.isBlank(contextId)) {
            return getName(attributes);
        }

        contextId = resolve(contextId);
        return getName(contextId);
    }

    private String resolve(String value) {
        if (StringUtils.isNotBlank(value)) {
            return this.environment.resolvePlaceholders(value);
        }
        return value;
    }

    private String getUrl(Map<String, Object> attributes) {
        String url = resolve((String) attributes.get("url"));
        return getUrl(url);
    }

    private String getPath(Map<String, Object> attributes) {
        String path = resolve((String) attributes.get("path"));
        return getPath(path);
    }

    private ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(
                AnnotatedBeanDefinition beanDefinition) {
                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent()) {
                    if (!beanDefinition.getMetadata().isAnnotation()) {
                        isCandidate = true;
                    }
                }
                return isCandidate;
            }
        };
    }

    private Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableSpinFeignClients.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();
        if (null == attributes) {
            return basePackages;
        }
        for (String pkg : (String[]) attributes.get("value")) {
            if (StringUtils.isNotBlank(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (String pkg : (String[]) attributes.get("basePackages")) {
            if (StringUtils.isNotBlank(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (Class<?> clazz : (Class[]) attributes.get("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }

        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }

    private String getQualifier(Map<String, Object> client) {
        if (client == null) {
            return null;
        }
        String qualifier = (String) client.get("qualifier");
        if (StringUtils.isNotBlank(qualifier)) {
            return qualifier;
        }
        return null;
    }

    private String getClientName(Map<String, Object> client) {
        if (client == null) {
            return null;
        }
        String value = (String) client.get("contextId");
        if (StringUtils.isBlank(value)) {
            value = (String) client.get("value");
        }
        if (StringUtils.isBlank(value)) {
            value = (String) client.get("name");
        }
        if (StringUtils.isBlank(value)) {
            value = (String) client.get("serviceId");
        }
        if (StringUtils.isNotBlank(value)) {
            return value;
        }

        throw new IllegalStateException("Either 'name' or 'value' must be provided in @" + FeignClient.class.getSimpleName());
    }

    private void registerClientConfiguration(BeanDefinitionRegistry registry, Object name, Object configuration) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
            .genericBeanDefinition(FeignClientSpecification.class);
        builder.addConstructorArgValue(name);
        builder.addConstructorArgValue(configuration);
        registry.registerBeanDefinition(
            name + "." + FeignClientSpecification.class.getSimpleName(),
            builder.getBeanDefinition());
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
        String resolve = environment.getProperty("feign.resolve.path");
        if (null != resolver) {
            return;
        }
        resolver = new Properties();
        Properties properties = new Properties();
        if (StringUtils.isEmpty(resolve)) {
            resolve = System.getProperty("user.home") + File.separator + "feign-resolve.properties";
            try (InputStream is = new FileInputStream(new File(resolve))) {
                properties.load(is);
                properties.forEach((k, v) -> resolver.put(k.toString().toUpperCase(), v));
            } catch (Exception e) {
                // do nothing
            }
        } else {
            try (InputStream is = SpinFeignClientsRegistrar.class.getClassLoader().getResourceAsStream(resolve)) {
                if (null != is) {
                    properties.load(is);
                    properties.forEach((k, v) -> resolver.put(k.toString().toUpperCase(), v));
                }
            } catch (Exception e) {
                throw new RuntimeException("Feign-Resolve配置加载失败", e);
            }
        }


    }

    /**
     * Helper class to create a {@link TypeFilter} that matches if all the delegates
     * match.
     *
     * @author Oliver Gierke
     */
    private static class AllTypeFilter implements TypeFilter {

        private final List<TypeFilter> delegates;

        /**
         * Creates a new {@link AllTypeFilter} to match if all the given delegates match.
         *
         * @param delegates must not be {@literal null}.
         */
        AllTypeFilter(List<TypeFilter> delegates) {
            Assert.notNull(delegates, "This argument is required, it must not be null");
            this.delegates = delegates;
        }

        @Override
        public boolean match(@NonNull MetadataReader metadataReader,
                             @NonNull MetadataReaderFactory metadataReaderFactory) throws IOException {

            for (TypeFilter filter : this.delegates) {
                if (!filter.match(metadataReader, metadataReaderFactory)) {
                    return false;
                }
            }

            return true;
        }

    }

}
