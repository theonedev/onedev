package io.onedev.server.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

public class BeanUtilsTest {

	@Test
	public void shouldFindGetter() {
		Method getter = BeanUtils.findGetter(SubClass.class, "value");
		assertEquals(getter.getReturnType(), String.class);

		List<Method> getters = BeanUtils.findGetters(SubClass.class);
		assertEquals(getters.size(), 2);
		assertEquals(getters.get(0).getReturnType(), String.class);
		assertEquals(getters.get(1).getReturnType(), Class.class);
	}

	@Test
	public void shouldFindSetter() {
		Method getter = BeanUtils.findGetter(SubClass.class, "value");
		assertTrue(BeanUtils.findSetter(getter) == null);

		getter = BeanUtils.findGetter(SuperClass.class, "value");
		assertTrue(BeanUtils.findSetter(getter) != null);
	}
	
	private static class SuperClass {
		
		@SuppressWarnings("unused")
		public Object getValue() {
			return null;
		}

		@SuppressWarnings("unused")
		public void setValue(Object value) {
			
		}
	}
	
	private static class SubClass extends SuperClass {

		public String getValue() {
			return null;
		}
		
	}
	
}
