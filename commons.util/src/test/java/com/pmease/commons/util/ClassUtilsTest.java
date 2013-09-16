package com.pmease.commons.util;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import com.pmease.commons.util.findsubclassesexploded.DummyAbstractClass;
import com.pmease.commons.util.findsubclassesexploded.DummyInterface;
import com.pmease.commons.util.findsubclassesexploded.dummy.DummyClass1;

public class ClassUtilsTest {

	@Test
	public void shouldFindExplodedSubClasses() {
		Collection<?> classes = ClassUtils.findSubClasses(
				DummyInterface.class, DummyInterface.class);
		Assert.assertEquals(1, classes.size());

		classes = ClassUtils.findSubClasses(
				DummyAbstractClass.class, DummyClass1.class);
		Assert.assertEquals(2, classes.size());

		classes = ClassUtils.findSubClasses(
				DummyClass1.class, DummyClass1.class);
		Assert.assertEquals(0, classes.size());

		classes = ClassUtils.findSubClasses(
				DummyInterface.class, DummyClass1.class);
		Assert.assertEquals(3, classes.size());
	}

}
