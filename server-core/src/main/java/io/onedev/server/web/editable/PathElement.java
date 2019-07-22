package io.onedev.server.web.editable;

import java.io.Serializable;

import com.google.common.base.Preconditions;

public interface PathElement extends Serializable {

	@SuppressWarnings("serial")
	public static class Named implements PathElement {

		private final String name;
		
		public Named(String name) {
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
	public static class Indexed implements PathElement {

		private final int index;
		
		public Indexed(int index) {
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
