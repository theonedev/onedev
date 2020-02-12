package io.onedev.server.util;

import org.eclipse.jgit.util.StringUtils;
import org.hibernate.criterion.Order;

import io.onedev.server.model.Milestone;

public enum MilestoneSort {
	CLOSEST_DUE_DATE {

		@Override
		public Order getOrder(boolean closed) {
			return closed?Order.desc(Milestone.PROP_DUE_DATE):Order.asc(Milestone.PROP_DUE_DATE);
		}
		
	},
	FURTHEST_DUE_DATE {

		@Override
		public Order getOrder(boolean closed) {
			return closed?Order.asc(Milestone.PROP_DUE_DATE):Order.desc(Milestone.PROP_DUE_DATE);
		}
		
	},
	MOST_ISSUES_TODO {

		@Override
		public Order getOrder(boolean closed) {
			return Order.desc("numOfIssuesTodo");
		}
		
	}, 
	LEAST_ISSUES_TODO {

		@Override
		public Order getOrder(boolean closed) {
			return Order.asc("numOfIssuesTodo");
		}
		
	},
	MOST_ISSUES_DONE {

		@Override
		public Order getOrder(boolean closed) {
			return Order.desc("numOfIssuesDone");
		}
		
	},
	LEAST_ISSUES_DONE {

		@Override
		public Order getOrder(boolean closed) {
			return Order.asc("numOfIssuesDone");
		}
		
	},
	NAME {

		@Override
		public Order getOrder(boolean closed) {
			return Order.asc("name");
		}
		
	},
	NAME_REVERSELY {

		@Override
		public Order getOrder(boolean closed) {
			return Order.desc("name");
		}
		
	};
	
	public abstract Order getOrder(boolean closed);

	@Override
	public String toString() {
		return StringUtils.capitalize(name().toLowerCase().replace("_", " "));
	}
	
}
