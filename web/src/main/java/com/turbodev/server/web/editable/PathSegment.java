package com.turbodev.server.web.editable;

import java.io.Serializable;

import com.google.common.base.Preconditions;

public interface PathSegment extends Serializable {

	@SuppressWarnings("serial")
	public static class Property implements PathSegment {

		private final String name;
		
		public Property(String name) {
			this.name = Preconditions.checkNotNull(name);
		}
		
		public String getName() {
			return name;
		}

		@Override
		public String toString() {
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
		
		@Override
		public String toString() {
			return String.valueOf(index);
		}

	}
	
}
