package io.onedev.server.model.support.issue.query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.User;

public enum UserOperator {
	
	EQUALS {
		
		@Override
		public Predicate getPredicate(CriteriaBuilder builder, Expression<User> left, User right) {
			return builder.equal(left, right);
		}
		
	}, 
	NOT_EQUALS {
		
		@Override
		public Predicate getPredicate(CriteriaBuilder builder, Expression<User> left, User right) {
			return builder.notEqual(left, right);
		}
		
	};

	public abstract Predicate getPredicate(CriteriaBuilder builder, Expression<User> left, User right);
	
}
