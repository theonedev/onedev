package io.onedev.server.search.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.util.RangeBuilder;
import io.onedev.server.util.criteria.Criteria;

public abstract class EntityCriteria<T extends AbstractEntity> extends Criteria<T> {
	
	private static final long serialVersionUID = 1L;
	
	public static final int IN_CLAUSE_LIMIT = 1000;
	
	public abstract Predicate getPredicate(Root<T> root, CriteriaBuilder builder);

	protected Predicate inManyValues(CriteriaBuilder builder, Path<Long> attribute, Collection<Long> inValues, 
			Collection<Long> allValues) {
		List<Long> listOfInValues = new ArrayList<>(inValues);
		Collections.sort(listOfInValues);
		List<Long> listOfAllValues = new ArrayList<>(allValues);
		Collections.sort(listOfAllValues);
		
		List<Predicate> predicates = new ArrayList<>();
		List<Long> discreteValues = new ArrayList<>();
		for (List<Long> range: new RangeBuilder(listOfInValues, listOfAllValues).getRanges()) {
			if (range.size() <= 2) {
				discreteValues.addAll(range);
			} else {
				predicates.add(builder.and(
						builder.greaterThanOrEqualTo(attribute, range.get(0)), 
						builder.lessThanOrEqualTo(attribute, range.get(range.size()-1))));
			}
		}

		Collection<Long> inClause = new ArrayList<>();
		for (Long value: discreteValues) {
			inClause.add(value);
			if (inClause.size() == IN_CLAUSE_LIMIT) {
				predicates.add(attribute.in(inClause));
				inClause = new ArrayList<>();
			}
		}
		if (!inClause.isEmpty()) 
			predicates.add(attribute.in(inClause));
		
		return builder.or(predicates.toArray(new Predicate[0]));
	}
	
	@Nullable
	public static <T extends AbstractEntity> EntityCriteria<T> andCriterias(List<EntityCriteria<T>> criterias) {
		if (criterias.size() > 1)
			return new AndEntityCriteria<T>(criterias);
		else if (criterias.size() == 1)
			return criterias.iterator().next();
		else
			return null;
	}

	@Nullable
	public static <T extends AbstractEntity> EntityCriteria<T> orCriterias(List<EntityCriteria<T>> criterias) {
		if (criterias.size() > 1)
			return new OrEntityCriteria<T>(criterias);
		else if (criterias.size() == 1)
			return criterias.iterator().next();
		else
			return null;
	}
	
}
