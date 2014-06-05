package com.pmease.commons.wicket.editor;

import java.io.Serializable;

public interface PathSegment extends Serializable {

	@SuppressWarnings("serial")
	public static class Property implements PathSegment {

		private final String name;
		
		public Property(String name) {
			this.name = name;
		}
		
		public String getname() {
			return name;
		}
		
	}
	
	@SuppressWarnings("serial")
	public static class Element implements PathSegment {

		private final int index;
		
		public Element(int index) {
			this.index = index;
		}
		
		public int getIndex() {
			return index;
		}
		
	}
	
}
