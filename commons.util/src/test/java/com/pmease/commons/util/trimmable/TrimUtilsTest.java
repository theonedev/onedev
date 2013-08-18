package com.pmease.commons.util.trimmable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TrimUtilsTest {

	@Test
	public void shouldTrimList() {
		List<Object> list = new ArrayList<Object>();
		list.add(new SimpleTrimmable(null));
		list.add(new SimpleTrimmable("1"));
		list.add(new SimpleTrimmable(null));
		list.add(new Trimmable() {

			@Override
			public Trimmable trim(Object context) {
				return new SimpleTrimmable("2");
			}
			
		});
		list.add(new SimpleTrimmable("3"));
		
		TrimUtils.trim(list, null);
		
		assertEquals(list.size(), 3);
		assertTrue(list.get(0) instanceof SimpleTrimmable);
		assertEquals(((SimpleTrimmable)list.get(0)).getValue(), "1");
		assertTrue(list.get(1) instanceof SimpleTrimmable);
		assertEquals(((SimpleTrimmable)list.get(1)).getValue(), "2");
		assertTrue(list.get(2) instanceof SimpleTrimmable);
		assertEquals(((SimpleTrimmable)list.get(2)).getValue(), "3");
	}

	@Test
	public void shouldTrimAndOrConstruct() {
		AndOrConstruct construct = new SimpleAndOrConstruct(
				new SimpleTrimmable("1"),
				new SimpleAndOrConstruct(
						new SimpleTrimmable(null), 
						new SimpleTrimmable("2")),
				new SimpleAndOrConstruct());
		
		Object result = TrimUtils.trim(construct, null);
		
		assertTrue(result instanceof AndOrConstruct);
		assertEquals(((AndOrConstruct)result).getMembers().size(), 2);
		assertTrue(((AndOrConstruct)result).getMembers().get(1) instanceof SimpleTrimmable);

		construct = new SimpleAndOrConstruct(
				new SimpleTrimmable(null),
				new SimpleAndOrConstruct(
						new SimpleTrimmable(null), 
						new SimpleTrimmable("2")),
				new SimpleAndOrConstruct());
		
		result = TrimUtils.trim(construct, null);
		
		assertTrue(result instanceof SimpleTrimmable);
		assertEquals(((SimpleTrimmable)result).getValue(), "2");
	}
	
	private static class SimpleAndOrConstruct implements AndOrConstruct, Trimmable {

		private List<Object> members = new ArrayList<Object>();
		
		public SimpleAndOrConstruct(Trimmable...members) {
			for (Trimmable each: members)
				this.members.add(each);
		}
		
		@Override
		public Object trim(Object context) {
			return TrimUtils.trim(this, null);
		}
		
		@Override
		public Object getSelf() {
			return this;
		}

		@Override
		public List<Object> getMembers() {
			return members;
		}

	}
	
	private static class SimpleTrimmable implements Trimmable {

		private String value;
		
		public SimpleTrimmable(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
		
		@Override
		public Trimmable trim(Object context) {
			if (value == null)
				return null;
			else
				return this;
		}
		
	};

}
