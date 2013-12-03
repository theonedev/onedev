package com.pmease.commons.util;

import org.junit.Assert;
import org.junit.Test;

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
