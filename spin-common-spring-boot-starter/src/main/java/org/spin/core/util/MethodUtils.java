package org.spin.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Method工具类
 */
public abstract class MethodUtils {
    private static final Logger logger = LoggerFactory.getLogger(MethodUtils.class);
    // --------------------------------------------------------- Private Methods

    private static boolean loggedAccessibleWarning = false;

    private static boolean cacheMethods = true;

    private static final Class<?>[] EMPTY_CLASS_PARAMETERS = new Class[0];
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private MethodUtils() {
    }

    private static final Map<MethodDescriptor, Reference<Method>> cache = Collections
        .synchronizedMap(new WeakHashMap<>());

    // --------------------------------------------------------- Public Methods

    public static synchronized void setCacheMethods(final boolean cacheMethods) {
        MethodUtils.cacheMethods = cacheMethods;
        if (!MethodUtils.cacheMethods) {
            clearCache();
        }
    }

    public static synchronized int clearCache() {
        final int size = cache.size();
        cache.clear();
        return size;
    }

    public static Object invokeMethod(
        final Object object,
        final String methodName,
        final Object arg)
        throws
        NoSuchMethodException,
        IllegalAccessException,
        InvocationTargetException {

        final Object[] args = toArray(arg);
        return invokeMethod(object, methodName, args);
    }


    public static Object invokeMethod(
        final Object object,
        final String methodName,
        Object[] args)
        throws
        NoSuchMethodException,
        IllegalAccessException,
        InvocationTargetException {

        if (args == null) {
            args = EMPTY_OBJECT_ARRAY;
        }
        final int arguments = args.length;
        final Class<?>[] parameterTypes = new Class[arguments];
        for (int i = 0; i < arguments; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        return invokeMethod(object, methodName, args, parameterTypes);
    }


    public static Object invokeMethod(
        final Object object,
        final String methodName,
        Object[] args,
        Class<?>[] parameterTypes)
        throws
        NoSuchMethodException,
        IllegalAccessException,
        InvocationTargetException {

        if (parameterTypes == null) {
            parameterTypes = EMPTY_CLASS_PARAMETERS;
        }
        if (args == null) {
            args = EMPTY_OBJECT_ARRAY;
        }

        final Method method = getMatchingAccessibleMethod(
            object.getClass(),
            methodName,
            parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException("No such accessible method: " +
                methodName + "() on object: " + object.getClass().getName());
        }
        return method.invoke(object, args);
    }


    public static Object invokeExactMethod(
        final Object object,
        final String methodName,
        final Object arg)
        throws
        NoSuchMethodException,
        IllegalAccessException,
        InvocationTargetException {

        final Object[] args = toArray(arg);
        return invokeExactMethod(object, methodName, args);
    }


    public static Object invokeExactMethod(
        final Object object,
        final String methodName,
        Object[] args)
        throws
        NoSuchMethodException,
        IllegalAccessException,
        InvocationTargetException {

        if (args == null) {
            args = EMPTY_OBJECT_ARRAY;
        }
        final int arguments = args.length;
        final Class<?>[] parameterTypes = new Class[arguments];
        for (int i = 0; i < arguments; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        return invokeExactMethod(object, methodName, args, parameterTypes);
    }


    public static Object invokeExactMethod(
        final Object object,
        final String methodName,
        Object[] args,
        Class<?>[] parameterTypes)
        throws
        NoSuchMethodException,
        IllegalAccessException,
        InvocationTargetException {

        if (args == null) {
            args = EMPTY_OBJECT_ARRAY;
        }

        if (parameterTypes == null) {
            parameterTypes = EMPTY_CLASS_PARAMETERS;
        }

        final Method method = getAccessibleMethod(
            object.getClass(),
            methodName,
            parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException("No such accessible method: " +
                methodName + "() on object: " + object.getClass().getName());
        }
        return method.invoke(object, args);
    }

    public static Object invokeExactStaticMethod(
        final Class<?> objectClass,
        final String methodName,
        Object[] args,
        Class<?>[] parameterTypes)
        throws
        NoSuchMethodException,
        IllegalAccessException,
        InvocationTargetException {

        if (args == null) {
            args = EMPTY_OBJECT_ARRAY;
        }

        if (parameterTypes == null) {
            parameterTypes = EMPTY_CLASS_PARAMETERS;
        }

        final Method method = getAccessibleMethod(
            objectClass,
            methodName,
            parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException("No such accessible method: " +
                methodName + "() on class: " + objectClass.getName());
        }
        return method.invoke(null, args);
    }

    public static Object invokeStaticMethod(
        final Class<?> objectClass,
        final String methodName,
        final Object arg)
        throws
        NoSuchMethodException,
        IllegalAccessException,
        InvocationTargetException {

        final Object[] args = toArray(arg);
        return invokeStaticMethod(objectClass, methodName, args);
    }


    public static Object invokeStaticMethod(
        final Class<?> objectClass,
        final String methodName,
        Object[] args)
        throws
        NoSuchMethodException,
        IllegalAccessException,
        InvocationTargetException {

        if (args == null) {
            args = EMPTY_OBJECT_ARRAY;
        }
        final int arguments = args.length;
        final Class<?>[] parameterTypes = new Class[arguments];
        for (int i = 0; i < arguments; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        return invokeStaticMethod(objectClass, methodName, args, parameterTypes);
    }


    public static Object invokeStaticMethod(
        final Class<?> objectClass,
        final String methodName,
        Object[] args,
        Class<?>[] parameterTypes)
        throws
        NoSuchMethodException,
        IllegalAccessException,
        InvocationTargetException {

        if (parameterTypes == null) {
            parameterTypes = EMPTY_CLASS_PARAMETERS;
        }
        if (args == null) {
            args = EMPTY_OBJECT_ARRAY;
        }

        final Method method = getMatchingAccessibleMethod(
            objectClass,
            methodName,
            parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException("No such accessible method: " +
                methodName + "() on class: " + objectClass.getName());
        }
        return method.invoke(null, args);
    }


    public static Object invokeExactStaticMethod(
        final Class<?> objectClass,
        final String methodName,
        final Object arg)
        throws
        NoSuchMethodException,
        IllegalAccessException,
        InvocationTargetException {

        final Object[] args = toArray(arg);
        return invokeExactStaticMethod(objectClass, methodName, args);
    }

    public static Object invokeExactStaticMethod(
        final Class<?> objectClass,
        final String methodName,
        Object[] args)
        throws
        NoSuchMethodException,
        IllegalAccessException,
        InvocationTargetException {

        if (args == null) {
            args = EMPTY_OBJECT_ARRAY;
        }
        final int arguments = args.length;
        final Class<?>[] parameterTypes = new Class[arguments];
        for (int i = 0; i < arguments; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        return invokeExactStaticMethod(objectClass, methodName, args, parameterTypes);
    }


    private static Object[] toArray(final Object arg) {
        Object[] args = null;
        if (arg != null) {
            args = new Object[]{arg};
        }
        return args;
    }

    public static Method getAccessibleMethod(
        final Class<?> clazz,
        final String methodName,
        final Class<?> parameterType) {

        final Class<?>[] parameterTypes = {parameterType};
        return getAccessibleMethod(clazz, methodName, parameterTypes);
    }


    public static Method getAccessibleMethod(
        final Class<?> clazz,
        final String methodName,
        final Class<?>[] parameterTypes) {

        try {
            final MethodDescriptor md = new MethodDescriptor(clazz, methodName, parameterTypes, true);
            Method method = getCachedMethod(md);
            if (method != null) {
                return method;
            }

            method = getAccessibleMethod(clazz, clazz.getMethod(methodName, parameterTypes));
            cacheMethod(md, method);
            return method;
        } catch (final NoSuchMethodException e) {
            return (null);
        }
    }

    public static Method getAccessibleMethod(final Method method) {

        if (method == null) {
            return (null);
        }

        return getAccessibleMethod(method.getDeclaringClass(), method);
    }


    public static Method getAccessibleMethod(Class<?> clazz, Method method) {

        if (method == null) {
            return (null);
        }

        if (!Modifier.isPublic(method.getModifiers())) {
            return (null);
        }

        boolean sameClass = true;
        if (clazz == null) {
            clazz = method.getDeclaringClass();
        } else {
            sameClass = clazz.equals(method.getDeclaringClass());
            if (!method.getDeclaringClass().isAssignableFrom(clazz)) {
                throw new IllegalArgumentException(clazz.getName() +
                    " is not assignable from " + method.getDeclaringClass().getName());
            }
        }

        if (Modifier.isPublic(clazz.getModifiers())) {
            if (!sameClass && !Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
                setMethodAccessible(method); // Default access superclass workaround
            }
            return (method);
        }


        return (method);
    }


    // -------------------------------------------------------- Private Methods

    /**
     * <p>Return an accessible method (that is, one that can be invoked via
     * reflection) by scanning through the superclasses. If no such method
     * can be found, return <code>null</code>.</p>
     *
     * @param clazz          Class to be checked
     * @param methodName     Method name of the method we wish to call
     * @param parameterTypes The parameter type signatures
     */
    private static Method getAccessibleMethodFromSuperclass
    (final Class<?> clazz, final String methodName, final Class<?>[] parameterTypes) {

        Class<?> parentClazz = clazz.getSuperclass();
        while (parentClazz != null) {
            if (Modifier.isPublic(parentClazz.getModifiers())) {
                try {
                    return parentClazz.getMethod(methodName, parameterTypes);
                } catch (final NoSuchMethodException e) {
                    return null;
                }
            }
            parentClazz = parentClazz.getSuperclass();
        }
        return null;
    }

    /**
     * <p>Return an accessible method (that is, one that can be invoked via
     * reflection) that implements the specified method, by scanning through
     * all implemented interfaces and subinterfaces.  If no such method
     * can be found, return <code>null</code>.</p>
     * <p>
     * <p> There isn't any good reason why this method must be private.
     * It is because there doesn't seem any reason why other classes should
     * call this rather than the higher level methods.</p>
     *
     * @param clazz          Parent class for the interfaces to be checked
     * @param methodName     Method name of the method we wish to call
     * @param parameterTypes The parameter type signatures
     */
    private static Method getAccessibleMethodFromInterfaceNest
    (Class<?> clazz, final String methodName, final Class<?>[] parameterTypes) {

        Method method = null;

        // Search up the superclass chain
        for (; clazz != null; clazz = clazz.getSuperclass()) {

            // Check the implemented interfaces of the parent class
            final Class<?>[] interfaces = clazz.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {

                // Is this interface public?
                if (!Modifier.isPublic(interfaces[i].getModifiers())) {
                    continue;
                }

                // Does the method exist on this interface?
                try {
                    method = interfaces[i].getDeclaredMethod(methodName,
                        parameterTypes);
                } catch (final NoSuchMethodException e) {
                    /* Swallow, if no method is found after the loop then this
                     * method returns null.
                     */
                }
                if (method != null) {
                    return method;
                }

                // Recursively check our parent interfaces
                method =
                    getAccessibleMethodFromInterfaceNest(interfaces[i],
                        methodName,
                        parameterTypes);
                if (method != null) {
                    return method;
                }

            }

        }

        // We did not find anything
        return (null);
    }

    /**
     * Find an accessible method that matches the given name and has compatible parameters.
     * Compatible parameters mean that every method parameter is assignable from
     * the given parameters.
     * In other words, it finds a method with the given name
     * that will take the parameters given.
     * <p>This method is slightly undeterministic since it loops
     * through methods names and return the first matching method.</p>
     * <p>This method is used by</p>
     * This method can match primitive parameter by passing in wrapper classes.
     * For example, a <code>Boolean</code> will match a primitive <code>boolean</code>
     * parameter.
     *
     * @param clazz          find method in this class
     * @param methodName     find method with this name
     * @param parameterTypes find method with compatible parameters
     * @return The accessible method
     */
    public static Method getMatchingAccessibleMethod(
        final Class<?> clazz,
        final String methodName,
        final Class<?>[] parameterTypes) {
        // trace logging
        if (logger.isTraceEnabled()) {
            logger.trace("Matching name=" + methodName + " on " + clazz);
        }
        final MethodDescriptor md = new MethodDescriptor(clazz, methodName, parameterTypes, false);

        // see if we can find the method directly
        // most of the time this works and it's much faster
        try {
            // Check the cache first
            Method method = getCachedMethod(md);
            if (method != null) {
                return method;
            }

            method = clazz.getMethod(methodName, parameterTypes);
            if (logger.isTraceEnabled()) {
                logger.trace("Found straight match: " + method);
                logger.trace("isPublic:" + Modifier.isPublic(method.getModifiers()));
            }

            setMethodAccessible(method); // Default access superclass workaround

            cacheMethod(md, method);
            return method;

        } catch (final NoSuchMethodException e) { /* SWALLOW */ }

        final int paramSize = parameterTypes.length;
        Method bestMatch = null;
        final Method[] methods = clazz.getMethods();
        float bestMatchCost = Float.MAX_VALUE;
        float myCost;
        for (Method method2 : methods) {
            if (method2.getName().equals(methodName)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Found matching name:");
                    logger.trace(method2.toGenericString());
                }

                final Class<?>[] methodsParams = method2.getParameterTypes();
                final int methodParamSize = methodsParams.length;
                if (methodParamSize == paramSize) {
                    boolean match = true;
                    for (int n = 0; n < methodParamSize; n++) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Param=" + parameterTypes[n].getName());
                            logger.trace("Method=" + methodsParams[n].getName());
                        }
                        if (!isAssignmentCompatible(methodsParams[n], parameterTypes[n])) {
                            if (logger.isTraceEnabled()) {
                                logger.trace(methodsParams[n] + " is not assignable from "
                                    + parameterTypes[n]);
                            }
                            match = false;
                            break;
                        }
                    }

                    if (match) {
                        final Method method = getAccessibleMethod(clazz, method2);
                        if (method != null) {
                            if (logger.isTraceEnabled()) {
                                logger.trace(method + " accessible version of "
                                    + method2);
                            }
                            setMethodAccessible(method);
                            myCost = getTotalTransformationCost(parameterTypes, method.getParameterTypes());
                            if (myCost < bestMatchCost) {
                                bestMatch = method;
                                bestMatchCost = myCost;
                            }
                        }

                        logger.trace("Couldn't find accessible method.");
                    }
                }
            }
        }
        if (bestMatch != null) {
            cacheMethod(md, bestMatch);
        } else {
            logger.trace("No match found.");
        }

        return bestMatch;
    }

    private static void setMethodAccessible(final Method method) {
        try {
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }

        } catch (final SecurityException se) {
            // log but continue just in case the method.invoke works anyway
            if (!loggedAccessibleWarning) {
                boolean vulnerableJVM = false;
                try {
                    final String specVersion = System.getProperty("java.specification.version");
                    if (specVersion.charAt(0) == '1' &&
                        (specVersion.charAt(2) == '0' ||
                            specVersion.charAt(2) == '1' ||
                            specVersion.charAt(2) == '2' ||
                            specVersion.charAt(2) == '3')) {

                        vulnerableJVM = true;
                    }
                } catch (final SecurityException e) {
                    // don't know - so display warning
                    vulnerableJVM = true;
                }
                if (vulnerableJVM) {
                    logger.warn(
                        "Current Security Manager restricts use of workarounds for reflection bugs "
                            + " in pre-1.4 JVMs.");
                }
                loggedAccessibleWarning = true;
            }
            logger.debug("Cannot setAccessible on method. Therefore cannot use jvm access bug workaround.", se);
        }
    }

    private static float getTotalTransformationCost(final Class<?>[] srcArgs, final Class<?>[] destArgs) {

        float totalCost = 0.0f;
        for (int i = 0; i < srcArgs.length; i++) {
            Class<?> srcClass, destClass;
            srcClass = srcArgs[i];
            destClass = destArgs[i];
            totalCost += getObjectTransformationCost(srcClass, destClass);
        }

        return totalCost;
    }

    private static float getObjectTransformationCost(Class<?> srcClass, final Class<?> destClass) {
        float cost = 0.0f;
        while (srcClass != null && !destClass.equals(srcClass)) {
            if (destClass.isPrimitive()) {
                final Class<?> destClassWrapperClazz = getPrimitiveWrapper(destClass);
                if (destClassWrapperClazz != null && destClassWrapperClazz.equals(srcClass)) {
                    cost += 0.25f;
                    break;
                }
            }
            if (destClass.isInterface() && isAssignmentCompatible(destClass, srcClass)) {
                cost += 0.25f;
                break;
            }
            cost++;
            srcClass = srcClass.getSuperclass();
        }

        if (srcClass == null) {
            cost += 1.5f;
        }

        return cost;
    }


    public static boolean isAssignmentCompatible(final Class<?> parameterType, final Class<?> parameterization) {
        if (parameterType.isAssignableFrom(parameterization)) {
            return true;
        }

        if (parameterType.isPrimitive()) {
            final Class<?> parameterWrapperClazz = getPrimitiveWrapper(parameterType);
            if (parameterWrapperClazz != null) {
                return parameterWrapperClazz.equals(parameterization);
            }
        }

        return false;
    }

    public static Class<?> getPrimitiveWrapper(final Class<?> primitiveType) {
        // does anyone know a better strategy than comparing names?
        if (boolean.class.equals(primitiveType)) {
            return Boolean.class;
        } else if (float.class.equals(primitiveType)) {
            return Float.class;
        } else if (long.class.equals(primitiveType)) {
            return Long.class;
        } else if (int.class.equals(primitiveType)) {
            return Integer.class;
        } else if (short.class.equals(primitiveType)) {
            return Short.class;
        } else if (byte.class.equals(primitiveType)) {
            return Byte.class;
        } else if (double.class.equals(primitiveType)) {
            return Double.class;
        } else if (char.class.equals(primitiveType)) {
            return Character.class;
        } else {

            return null;
        }
    }

    /**
     * Gets the class for the primitive type corresponding to the primitive wrapper class given.
     * For example, an instance of <code>Boolean.class</code> returns a <code>boolean.class</code>.
     *
     * @param wrapperType the
     * @return the primitive type class corresponding to the given wrapper class,
     * null if no match is found
     */
    public static Class<?> getPrimitiveType(final Class<?> wrapperType) {
        // does anyone know a better strategy than comparing names?
        if (Boolean.class.equals(wrapperType)) {
            return boolean.class;
        } else if (Float.class.equals(wrapperType)) {
            return float.class;
        } else if (Long.class.equals(wrapperType)) {
            return long.class;
        } else if (Integer.class.equals(wrapperType)) {
            return int.class;
        } else if (Short.class.equals(wrapperType)) {
            return short.class;
        } else if (Byte.class.equals(wrapperType)) {
            return byte.class;
        } else if (Double.class.equals(wrapperType)) {
            return double.class;
        } else if (Character.class.equals(wrapperType)) {
            return char.class;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Not a known primitive wrapper class: " + wrapperType);
            }
            return null;
        }
    }

    /**
     * Find a non primitive representation for given primitive class.
     *
     * @param clazz the class to find a representation for, not null
     * @return the original class if it not a primitive. Otherwise the wrapper class. Not null
     */
    public static Class<?> toNonPrimitiveClass(final Class<?> clazz) {
        if (clazz.isPrimitive()) {
            final Class<?> primitiveClazz = MethodUtils.getPrimitiveWrapper(clazz);
            // the above method returns
            if (primitiveClazz != null) {
                return primitiveClazz;
            } else {
                return clazz;
            }
        } else {
            return clazz;
        }
    }

    /**
     * 判断方法的参数列表中，是否存在含有泛型的参数
     *
     * @param method 待判断的方法
     * @return 方法是否含有泛型参数
     */
    public static boolean containsGenericArg(Method method) {
        return containsGenericArg(Arrays.stream(method.getParameters()).map(Parameter::getParameterizedType).toArray(java.lang.reflect.Type[]::new));
    }

    /**
     * 判断类型中是否含有泛型参数
     *
     * @param argTypes 待判断的类型
     * @return 类型中是否存在含有泛型参数的情况
     */
    public static boolean containsGenericArg(java.lang.reflect.Type... argTypes) {
        boolean contains;
        for (java.lang.reflect.Type type : argTypes) {
            if (type instanceof TypeVariable) {
                // 泛型参数
                return true;
            } else if (type instanceof GenericArrayType) {
                // 含泛型数组参数
                contains = containsGenericArg(((GenericArrayType) type).getGenericComponentType());
                if (contains) return true;
            } else if (type instanceof ParameterizedType) {
                // 含泛型参数的参数
                contains = containsGenericArg(((ParameterizedType) type).getActualTypeArguments());
                if (contains) return true;
            }
        }
        return false;
    }

    /**
     * Return the method from the cache, if present.
     *
     * @param md The method descriptor
     * @return The cached method
     */
    private static Method getCachedMethod(final MethodDescriptor md) {
        if (cacheMethods) {
            final Reference<Method> methodRef = cache.get(md);
            if (methodRef != null) {
                return methodRef.get();
            }
        }
        return null;
    }

    /**
     * Add a method to the cache.
     *
     * @param md     The method descriptor
     * @param method The method to cache
     */
    private static void cacheMethod(final MethodDescriptor md, final Method method) {
        if (cacheMethods) {
            if (method != null) {
                cache.put(md, new WeakReference<>(method));
            }
        }
    }

    /**
     * 描述反射方法的关键信息
     */
    private static class MethodDescriptor {
        private final Class<?> cls;
        private final String methodName;
        private final Class<?>[] paramTypes;
        private final boolean exact;
        private final int hashCode;

        /**
         * @param cls        反射的类，不能为空
         * @param methodName 需要获取的方法
         * @param paramTypes 需获取方法的参数
         * @param exact      whether the match has to be exact.
         */
        public MethodDescriptor(final Class<?> cls, final String methodName, Class<?>[] paramTypes, final boolean exact) {
            if (cls == null) {
                throw new IllegalArgumentException("Class cannot be null");
            }
            if (methodName == null) {
                throw new IllegalArgumentException("Method Name cannot be null");
            }
            if (paramTypes == null) {
                paramTypes = EMPTY_CLASS_PARAMETERS;
            }

            this.cls = cls;
            this.methodName = methodName;
            this.paramTypes = paramTypes;
            this.exact = exact;

            this.hashCode = methodName.length();
        }

        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof MethodDescriptor)) {
                return false;
            }
            final MethodDescriptor md = (MethodDescriptor) obj;

            return (
                exact == md.exact &&
                    methodName.equals(md.methodName) &&
                    cls.equals(md.cls) &&
                    java.util.Arrays.equals(paramTypes, md.paramTypes)
            );
        }

        /**
         * 返回方法名称的字符串长度。如果名称长度不同，则肯定不是同一方法，如果长度相同，
         * 进一步通过equals方法判断等价性
         *
         * @return 方法名称的字符串长度
         */
        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}

