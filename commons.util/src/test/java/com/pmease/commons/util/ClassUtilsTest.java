package com.pmease.commons.util;

import static org.junit.Assert.assertSame;

import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.pmease.commons.util.ClassUtils;
import com.pmease.commons.util.findsubclassesexploded.DummyAbstractClass;
import com.pmease.commons.util.findsubclassesexploded.DummyInterface;
import com.pmease.commons.util.findsubclassesexploded.dummy.DummyClass1;

import com.pmease.commons.test.findsubclassesinjar.DummyAbstractClassInJar;
import com.pmease.commons.test.findsubclassesinjar.DummyInterfaceInJar;
import com.pmease.commons.test.findsubclassesinjar.dummy.DummyClass1InJar;

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

	@Test
	public void shouldFindSubClassesInJar() {
		Collection<?> classes = ClassUtils.findSubClasses(
				DummyInterfaceInJar.class, DummyInterfaceInJar.class);
		Assert.assertEquals(1, classes.size());

		classes = ClassUtils.findSubClasses(
				DummyAbstractClassInJar.class, DummyClass1InJar.class);
		Assert.assertEquals(2, classes.size());

		classes = ClassUtils.findSubClasses(
				DummyClass1InJar.class, DummyClass1InJar.class);
		Assert.assertEquals(0, classes.size());

		classes = ClassUtils.findSubClasses(
				DummyInterfaceInJar.class, DummyClass1InJar.class);
		Assert.assertEquals(3, classes.size());
	}
	
	private static class SuperClass<T1, T2> {}
	
	private static class SubClass1 extends SuperClass<String, Integer> {}

	private static class SubClass2<T> extends SuperClass<String, T> {}

	private static class SubClass3 extends SubClass2<Integer> {}

	@Test
	public void shouldResolveTypeArg() {
		List<?> typeArguments = ClassUtils.getTypeArguments(SuperClass.class, SubClass1.class);
		assertSame(String.class, typeArguments.get(0));
		assertSame(Integer.class, typeArguments.get(1));

		typeArguments = ClassUtils.getTypeArguments(SuperClass.class, SubClass2.class);
		assertSame(String.class, typeArguments.get(0));

		typeArguments = ClassUtils.getTypeArguments(SuperClass.class, SubClass3.class);
		assertSame(String.class, typeArguments.get(0));
		assertSame(Integer.class, typeArguments.get(1));

		typeArguments = ClassUtils.getTypeArguments(SubClass2.class, SubClass3.class);
		assertSame(Integer.class, typeArguments.get(0));
	}
	
}
