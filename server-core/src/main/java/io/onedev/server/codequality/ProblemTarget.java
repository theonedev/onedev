package io.onedev.server.codequality;

import io.onedev.server.model.Group;
import org.apache.wicket.Component;

import java.io.Serializable;

public abstract class ProblemTarget implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final GroupKey groupKey;
	
	public ProblemTarget(GroupKey groupKey) {
		this.groupKey = groupKey;
	}
	
	public GroupKey getGroupKey() {
		return groupKey;
	}

	public static abstract class GroupKey implements Serializable {

		private static final long serialVersionUID = 1L;

		private final String name;

		public GroupKey(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
		
		public abstract Component render(String componentId);
		
	}
	
}
