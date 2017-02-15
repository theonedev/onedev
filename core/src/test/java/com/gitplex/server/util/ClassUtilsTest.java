package com.gitplex.server.util;

import org.junit.Assert;
import org.junit.Test;

import com.gitplex.server.util.ClassUtils;

public class ClassUtilsTest {
	
	@Test
	public void testGetResourceAsStream() {
		String packageName = ClassUtils.class.getPackage().getName();
		Assert.assertNotNull(ClassUtils.getResourceAsStream(
				null, packageName.replace('.', '/') + "/ClassUtils.class"));
		Assert.assertNotNull(ClassUtils.getResourceAsStream(
				ClassUtils.class, "ClassUtils.class"));
	}
	
}
