package com.gitplex.commons.util;

import java.io.InputStream;

import javax.annotation.Nullable;

import javassist.util.proxy.ProxyFactory;

public class ClassUtils extends org.apache.commons.lang3.ClassUtils {
	
	public static @Nullable InputStream getResourceAsStream(@Nullable Class<?> locator, String path) {
		path = StringUtils.stripStart(path, "/");
		if (locator == null)
			return ClassUtils.class.getClassLoader().getResourceAsStream(path);
		else
			return locator.getClassLoader().getResourceAsStream(locator.getPackage().getName().replace(".", "/") + "/" + path);
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> unproxy(Class<T> clazz) {
		Class<T> superClass = clazz;
		while (ProxyFactory.isProxyClass(superClass))
			superClass = (Class<T>) clazz.getSuperclass();
		return superClass;
	}
	
	public static boolean isSystemType(Class<?> type) {
		return type.getName().startsWith("java") 
			|| type.getName().startsWith("javax") 
			|| type.isArray()
			|| type.isPrimitive()
			|| type.isEnum();
	}
	
}
