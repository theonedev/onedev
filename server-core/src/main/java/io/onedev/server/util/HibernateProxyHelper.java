package io.onedev.server.util;

import org.hibernate.proxy.HibernateProxy;

public final class HibernateProxyHelper {
    private HibernateProxyHelper() {}

    @SuppressWarnings("rawtypes")
    public static Class getClassWithoutInitializingProxy(Object entity) {
        var initializer = HibernateProxy.extractLazyInitializer(entity);
        return initializer != null ? initializer.getPersistentClass() : entity.getClass();
    }
}
