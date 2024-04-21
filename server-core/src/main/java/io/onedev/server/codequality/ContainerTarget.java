package io.onedev.server.codequality;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.wicket.Component;

public class ContainerTarget extends ProblemTarget {

	private static final long serialVersionUID = 1L;
	
	public ContainerTarget(String name, String platform) {
		super(new GroupKey(name, platform));
	}
	
	public static class GroupKey extends ProblemTarget.GroupKey {

		private static final long serialVersionUID = 1L;

		private final String platform;

		public GroupKey(String name, String platform) {
			super(name);
			this.platform = platform;
		}

		public String getPlatform() {
			return platform;
		}

		@Override
		public Component render(String componentId) {
			return new ContainerTargetKeyPanel(componentId, getName(), getPlatform());
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
					.append(platform, otherKey.platform)
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37)
					.append(getName())
					.append(platform)
					.toHashCode();
		}

	}
}
