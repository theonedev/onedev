package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public enum ArithmeticOperator {
	
	EQUALS {
		
		@Override
		public Predicate getPredicate(CriteriaBuilder builder, Expression<Integer> left, int right) {
			return builder.equal(left, right);
		}
		
	}, 
	NOT_EQUALS {
		
		@Override
		public Predicate getPredicate(CriteriaBuilder builder, Expression<Integer> left, int right) {
			return builder.notEqual(left, right);
		}
		
	},
	GREATER_THAN {
		
		@Override
		public Predicate getPredicate(CriteriaBuilder builder, Expression<Integer> left, int right) {
			return builder.greaterThan(left, right);
		}
		
	}, 
	LESS_THAN {
		
		@Override
		public Predicate getPredicate(CriteriaBuilder builder, Expression<Integer> left, int right) {
			return builder.lessThan(left, right);
		}
		
	};

	public abstract Predicate getPredicate(CriteriaBuilder builder, Expression<Integer> left, int right);
	
}
