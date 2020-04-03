package io.onedev.server.util;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Preconditions;

public interface PathNode extends Serializable {

	@SuppressWarnings("serial")
	public static class Named implements PathNode {

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
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Named)) 
				return false;
			if (this == other)
				return true;
			Named otherElement = (Named) other;
			return new EqualsBuilder()
				.append(name, otherElement.getName())
				.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37)
				.append(name)
				.toHashCode();
		}		
		
	}
	
	@SuppressWarnings("serial")
	public static class Indexed implements PathNode {

		private final int index;
		
		public Indexed(int index) {
			this.index = index;
		}
		
		public int getIndex() {
			return index;
		}
		
		@Override
		public String toString() {
			return "item #" + (index+1);
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Indexed)) 
				return false;
			if (this == other)
				return true;
			Indexed otherElement = (Indexed) other;
			return new EqualsBuilder()
				.append(index, otherElement.getIndex())
				.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37)
				.append(index)
				.toHashCode();
		}		
		
	}
	
}
