package io.onedev.server.util;

import org.eclipse.jgit.util.StringUtils;
import org.hibernate.criterion.Order;

import io.onedev.server.model.Iteration;

public enum IterationSort {
	CLOSEST_DUE_DATE {

		@Override
		public Order getOrder(boolean closed) {
			return closed?Order.desc(Iteration.PROP_DUE_DAY):Order.asc(Iteration.PROP_DUE_DAY);
		}
		
	},
	FURTHEST_DUE_DATE {

		@Override
		public Order getOrder(boolean closed) {
			return closed?Order.asc(Iteration.PROP_DUE_DAY):Order.desc(Iteration.PROP_DUE_DAY);
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
