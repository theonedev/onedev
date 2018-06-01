package io.onedev.server.util;

import org.eclipse.jgit.util.StringUtils;
import org.hibernate.criterion.Order;

public enum MilestoneSort {
	RECENTLY_UPDATED {

		@Override
		public Order getOrder() {
			return Order.desc("updateDate");
		}
		
	},
	FURTHEST_DUE_DATE {

		@Override
		public Order getOrder() {
			return Order.desc("dueDate");
		}
		
	},
	CLOSEST_DUE_DATE {

		@Override
		public Order getOrder() {
			return Order.asc("dueDate");
		}
		
	},
	MOST_ISSUES {

		@Override
		public Order getOrder() {
			return Order.desc("numOfOpenIssues");
		}
		
	}, 
	LEAST_ISSUES {

		@Override
		public Order getOrder() {
			return Order.asc("numOfOpenIssues");
		}
		
	},
	MOST_COMPLETED {

		@Override
		public Order getOrder() {
			return Order.desc("numOfClosedIssues");
		}
		
	},
	LEAST_COMPLETED {

		@Override
		public Order getOrder() {
			return Order.asc("numOfClosedIssues");
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
