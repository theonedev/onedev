package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public enum EnumOperator {
	
	EQUALS {
		
		@Override
		public Predicate getPredicate(CriteriaBuilder builder, Expression<String> left, String right) {
			return builder.equal(left, right);
		}
		
	}, 
	NOT_EQUALS {
		
		@Override
		public Predicate getPredicate(CriteriaBuilder builder, Expression<String> left, String right) {
			return builder.notEqual(left, right);
		}
		
	};

	public abstract Predicate getPredicate(CriteriaBuilder builder, Expression<String> left, String right);
	
}
