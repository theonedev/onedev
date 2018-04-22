package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public enum TextOperator {
	
	CONTAINS {
		
		@Override
		public Predicate getPredicate(CriteriaBuilder builder, Expression<String> left, String right) {
			return builder.like(left, "%" + right + "%");
		}
		
	}, 
	NOT_CONTAINS {
		
		@Override
		public Predicate getPredicate(CriteriaBuilder builder, Expression<String> left, String right) {
			return builder.notLike(left, "%" + right + "%");
		}
		
	};

	public abstract Predicate getPredicate(CriteriaBuilder builder, Expression<String> left, String right);
	
}
