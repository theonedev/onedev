package io.onedev.server.util;

import static org.junit.Assert.assertSame;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

import com.google.common.base.Optional;

public class ReflectionUtilsTest {

	private static class SuperClass<T1, T2> {}
	
	private static class SubClass1 extends SuperClass<String, Integer> {}

	private static class SubClass2<T> extends SuperClass<String, T> {}

	private static class SubClass3 extends SubClass2<Integer> {}

	@Test
	public void shouldResolveTypeArg() {
		List<?> typeArguments = ReflectionUtils.getTypeArguments(SuperClass.class, SubClass1.class);
		assertSame(String.class, typeArguments.get(0));
		assertSame(Integer.class, typeArguments.get(1));

		typeArguments = ReflectionUtils.getTypeArguments(SuperClass.class, SubClass2.class);
		assertSame(String.class, typeArguments.get(0));

		typeArguments = ReflectionUtils.getTypeArguments(SuperClass.class, SubClass3.class);
		assertSame(String.class, typeArguments.get(0));
		assertSame(Integer.class, typeArguments.get(1));

		typeArguments = ReflectionUtils.getTypeArguments(SubClass2.class, SubClass3.class);
		assertSame(Integer.class, typeArguments.get(0));
	}
	
	@Test
	public void shouldFindMethodCorrectly() {
		Method method = ReflectionUtils.findMethod(SubClass4.class, "method", String.class, int.class);
		assertSame(method.getReturnType(), String.class);

		method = ReflectionUtils.findMethod(SuperClass1.class, "method", String.class, int.class);
		assertSame(method.getReturnType(), Object.class);
		
		method = ReflectionUtils.findMethod(SubClass4.class, "method2", String.class);
		assertSame(method.getParameterTypes()[0], String.class);

		method = ReflectionUtils.findMethod(SubClass4.class, "method2", Object.class);
		assertSame(method.getParameterTypes()[0], Object.class);
	}
	
	private static class SuperClass1 {

		@SuppressWarnings("unused")
		public Object method(String param1, int param2) {
			return param1.toString() + param2;
		}
	}

	private static class SubClass4 extends SuperClass1 {
		
		public String method(String param1, int param2) {
			return param1 + param2;
		}
		
		@SuppressWarnings("unused")
		public List<String> method2(Object param1) {
			return null;
		}
		
		@SuppressWarnings("unused")
		public List<String> method2(String param1) {
			return null;
		}
		
		@SuppressWarnings("unused")
		public List<Optional<String>> method3(String param1) {
			return null;
		}

		@SuppressWarnings("unused")
		public List<String> method4(String param1) {
			return null;
		}
	}

}
