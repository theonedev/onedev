package io.onedev.server.util.criteria;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.apache.lucene.document.LongPoint;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.RangeBuilder;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedStateResolution;

public abstract class Criteria<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int IN_CLAUSE_LIMIT = 1000;
	
	private static final int RANGE_THRESHOLD = 6;
	
	private boolean withParens;
	
	public static Predicate forManyValues(CriteriaBuilder builder, Path<Long> path, Collection<Long> matchValues, 
			Collection<Long> allValues) {
		List<Predicate> predicates = new ArrayList<>();
		forManyValues(matchValues, allValues, new NumberCriteriaBuilder() {

			@Override
			public void forRange(long min, long max) {
				predicates.add(builder.and(
						builder.greaterThanOrEqualTo(path, min), 
						builder.lessThanOrEqualTo(path, max)));
			}

			@Override
			public void forDiscretes(Collection<Long> numbers) {
				predicates.add(path.in(numbers));
			}
			
		});
		
		return builder.or(predicates.toArray(new Predicate[0]));
	}
	
	public static Query forManyValues(String fieldName, Collection<Long> matchValues, Collection<Long> allValues) {
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		forManyValues(matchValues, allValues, new NumberCriteriaBuilder() {

			@Override
			public void forRange(long min, long max) {
				queryBuilder.add(LongPoint.newRangeQuery(fieldName, min, max), Occur.SHOULD);
			}

			@Override
			public void forDiscretes(Collection<Long> numbers) {
				for (Long number: numbers)
					queryBuilder.add(LongPoint.newExactQuery(fieldName, number), Occur.SHOULD);
			}
			
		});
		
		queryBuilder.setMinimumNumberShouldMatch(1);
		return queryBuilder.build();
	}
	
	public static void forManyValues(Collection<Long> matchValues, Collection<Long> allValues, 
			NumberCriteriaBuilder builder) {
		List<Long> listOfInValues = new ArrayList<>(matchValues);
		Collections.sort(listOfInValues);
		List<Long> listOfAllValues = new ArrayList<>(allValues);
		Collections.sort(listOfAllValues);
		
		List<Long> discreteValues = new ArrayList<>();
		for (List<Long> range: new RangeBuilder(listOfInValues, listOfAllValues).getRanges()) {
			if (range.size() <= RANGE_THRESHOLD) 
				discreteValues.addAll(range);
			else 
				builder.forRange(range.get(0), range.get(range.size()-1));
		}

		Collection<Long> inClause = new ArrayList<>();
		for (Long value: discreteValues) {
			inClause.add(value);
			if (inClause.size() == IN_CLAUSE_LIMIT) {
				builder.forDiscretes(inClause);
				inClause = new ArrayList<>();
			}
		}
		if (!inClause.isEmpty()) 
			builder.forDiscretes(inClause);
	}
	
	public abstract Predicate getPredicate(CriteriaQuery<?> query, From<T, T> from, CriteriaBuilder builder);

	public abstract boolean matches(T t);
	
	public Criteria<T> withParens(boolean withParens) {
		this.withParens = withParens;
		return this;
	}
	
	public boolean withParens() {
		return withParens;
	}

	public void onRenameUser(String oldName, String newName) {
	}

	public void onMoveProject(String oldPath, String newPath) {
	}

	public void onRenameGroup(String oldName, String newName) {
	}

	public void onRenameLink(String oldName, String newName) {
	}
	
	public boolean isUsingUser(String userName) {
		return false;
	}

	public boolean isUsingProject(String projectPath) {
		return false;
	}
	
	public boolean isUsingGroup(String groupName) {
		return false;
	}
	
	public boolean isUsingLink(String linkName) {
		return false;
	}
	
	public Collection<String> getUndefinedStates() {
		return new HashSet<>();
	}

	public Collection<String> getUndefinedFields() {
		return new HashSet<>();
	}
	
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		return new HashSet<>();
	}

	public boolean fixUndefinedStates(Map<String, UndefinedStateResolution> resolutions) {
		return true;
	}
	
	public boolean fixUndefinedFields(Map<String, UndefinedFieldResolution> resolutions) {
		return true;
	}
	
	public boolean fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		return true;
	}
	
	public static String quote(String value) {
		return "\"" + StringUtils.escape(value, "\"") + "\"";
	}

	@Override
	public String toString() {
		if (withParens)
			return "(" + toStringWithoutParens() + ")";
		else
			return toStringWithoutParens();
	}
	
	public String toString(boolean addParensIfNecessary) {
		return toString();
	}
	
	public void fill(T object) {
	}
	
	public abstract String toStringWithoutParens();
	
	@Nullable
	public static <T> Criteria<T> andCriterias(List<Criteria<T>> criterias) {
		if (criterias.size() > 1)
			return new AndCriteria<T>(criterias);
		else if (criterias.size() == 1)
			return criterias.iterator().next();
		else
			return null;
	}

	@Nullable
	public static <T> Criteria<T> orCriterias(List<Criteria<T>> criterias) {
		if (criterias.size() > 1)
			return new OrCriteria<T>(criterias);
		else if (criterias.size() == 1)
			return criterias.iterator().next();
		else
			return null;
	}
	
}
