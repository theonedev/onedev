package io.onedev.server.model.support.issue.query;

import java.util.Date;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

public enum DateOperator {
	
	BEFORE {
		
		@Override
		public Predicate getPredicate(CriteriaBuilder builder, Expression<Date> left, Date right) {
			return builder.lessThan(left, right);
		}
		
	}, 
	AFTER {
		
		@Override
		public Predicate getPredicate(CriteriaBuilder builder, Expression<Date> left, Date right) {
			return builder.greaterThan(left, right);
		}
		
	};
	
	public abstract Predicate getPredicate(CriteriaBuilder builder, Expression<Date> left, Date right);
	
}
