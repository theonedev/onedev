package io.onedev.server.util.interpolative;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import io.onedev.server.annotation.Editable;

public class VariableInterpolatorTest {

	@Test
	public void test() {
		assertEquals("@", new VariableInterpolator(it->"!").interpolate("@@"));
		assertEquals("@@", new VariableInterpolator(it->it).interpolate("@@@@"));
		assertEquals("hello", new VariableInterpolator(it->it).interpolate("@hello@"));
		assertEquals("helloworlddo", new VariableInterpolator(it->it).interpolate("hello@world@do"));
		assertEquals("1234", new VariableInterpolator(it->it).interpolate("1@2@@3@4"));
		assertEquals("1!!4", new VariableInterpolator(it->"!").interpolate("1@2@@3@4"));
		assertEquals("1!-!4", new VariableInterpolator(it->"!").interpolate("1@2@-@3@4"));
		try {
			Interpolative.parse("hello@world@@do");
			assertTrue(false);
		} catch (RuntimeException e) {
		}
		try {
			Interpolative.parse("@@@");
			assertTrue(false);
		} catch (RuntimeException e) {
		}
		
		TestBean bean = new TestBean();
		bean.setName("@var@");
		bean.setChildren(Lists.newArrayList("@var@", "@var@"));
		bean.setParent(new TestParentBean());
		bean.getParent().setName("@var@");
		
		TestBean interpolated = new VariableInterpolator(it->"hello").interpolateProperties(bean);
		assertEquals("hello", interpolated.getName());
		assertEquals(Lists.newArrayList("hello", "hello"), interpolated.getChildren());
		assertEquals("hello", interpolated.getParent().getName());
		
		assertEquals("@var@", bean.getName());
		assertEquals(Lists.newArrayList("@var@", "@var@"), bean.getChildren());
		assertEquals("@var@", bean.getParent().getName());
	}

	@Editable
	public static class TestBean {
		
		private String name;
		
		private List<String> children = new ArrayList<>();
		
		private TestParentBean parent = new TestParentBean();

		@Editable
		@io.onedev.server.annotation.Interpolative
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Editable
		@io.onedev.server.annotation.Interpolative
		public List<String> getChildren() {
			return children;
		}

		public void setChildren(List<String> children) {
			this.children = children;
		}

		@Editable
		public TestParentBean getParent() {
			return parent;
		}

		public void setParent(TestParentBean parent) {
			this.parent = parent;
		}
		
	}

	@Editable
	public static class TestParentBean {
		
		private String name;

		@Editable
		@io.onedev.server.annotation.Interpolative
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
	}
}
