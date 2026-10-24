package io.onedev.server.codequality;

import java.io.Serial;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;

public class GeneralTarget extends ProblemTarget {

	@Serial
	private static final long serialVersionUID = 1L;
	
	public GeneralTarget(String name) {
		super(new GroupKey(name));
	}
	
	public static class GroupKey extends ProblemTarget.GroupKey {

		@Serial
		private static final long serialVersionUID = 1L;

		public GroupKey(String name) {
			super(name);
		}

		@Override
		public Component render(String componentId) {
			return new Label(componentId, getName()).add(AttributeAppender.append("class", "text-break"));
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof GroupKey))
				return false;
			if (this == other)
				return true;
			var otherKey = (GroupKey) other;
			return new EqualsBuilder()
					.append(getName(), otherKey.getName())
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37)
					.append(getName())
					.toHashCode();
		}

	}	
}
