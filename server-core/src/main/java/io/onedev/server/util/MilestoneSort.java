package io.onedev.server.util;

import org.eclipse.jgit.util.StringUtils;
import org.hibernate.criterion.Order;

public enum MilestoneSort {
	CLOSEST_DUE_DATE {

		@Override
		public Order getOrder() {
			return Order.asc("dueDate");
		}
		
	},
	FURTHEST_DUE_DATE {

		@Override
		public Order getOrder() {
			return Order.desc("dueDate");
		}
		
	},
	MOST_ISSUES_TODO {

		@Override
		public Order getOrder() {
			return Order.desc("numOfIssuesTodo");
		}
		
	}, 
	LEAST_ISSUES_TODO {

		@Override
		public Order getOrder() {
			return Order.asc("numOfIssuesTodo");
		}
		
	},
	MOST_ISSUES_DONE {

		@Override
		public Order getOrder() {
			return Order.desc("numOfIssuesDone");
		}
		
	},
	LEAST_ISSUES_DONE {

		@Override
		public Order getOrder() {
			return Order.asc("numOfIssuesDone");
		}
		
	},
	NAME {

		@Override
		public Order getOrder() {
			return Order.asc("name");
		}
		
	},
	NAME_REVERSELY {

		@Override
		public Order getOrder() {
			return Order.desc("name");
		}
		
	};
	
	public abstract Order getOrder();

	@Override
	public String toString() {
		return StringUtils.capitalize(name().toLowerCase().replace("_", " "));
	}
	
}
