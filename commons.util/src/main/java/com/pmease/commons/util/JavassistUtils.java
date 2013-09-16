package com.pmease.commons.util;

import javassist.util.proxy.ProxyFactory;

public class JavassistUtils {

	public static Class<?> unproxy(Class<?> clazz) {
		Class<?> superClass = clazz;
		while (ProxyFactory.isProxyClass(superClass))
			superClass = clazz.getSuperclass();
		return superClass;
	}

}
