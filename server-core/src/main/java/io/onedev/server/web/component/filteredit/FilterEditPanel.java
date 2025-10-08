package io.onedev.server.web.component.filteredit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import com.google.common.collect.Lists;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.Pair;
import io.onedev.server.util.criteria.AndCriteria;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.OrCriteria;

public abstract class FilterEditPanel<T extends AbstractEntity> extends GenericPanel<EntityQuery<T>> {
	
	public FilterEditPanel(String id, IModel<EntityQuery<T>> queryModel) {
		super(id, queryModel);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}

	@SuppressWarnings("unchecked")
	private <C extends Criteria<T>> boolean isCriteriaMatching(Criteria<T> criteria, Class<C> criteriaClass, @Nullable Predicate<C> predicate) {
		return criteriaClass.isInstance(criteria) && (predicate == null || predicate.test((C) criteria));
	}

	private <C extends Criteria<T>> boolean isMemberAllMatching(OrCriteria<T> rootCriteria, Class<C> criteriaClass, @Nullable Predicate<C> predicate) {
		for (var childCriteria: rootCriteria.getCriterias()) {
			if (childCriteria instanceof OrCriteria) {
				if (!isMemberAllMatching((OrCriteria<T>) childCriteria, criteriaClass, predicate))
					return false;
			} else if (!isCriteriaMatching(childCriteria, criteriaClass, predicate)) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private <C extends Criteria<T>> List<C> getFlattenedMembers(OrCriteria<T> rootCriteria) {
		var flattenedCriterias = new ArrayList<C>();
		for (var childCriteria: rootCriteria.getCriterias()) {
			if (childCriteria instanceof OrCriteria) {
				flattenedCriterias.addAll(getFlattenedMembers((OrCriteria<T>) childCriteria));
			} else {
				flattenedCriterias.add((C) childCriteria);
			}
		}
		return flattenedCriterias;
	}

	@Nullable
	private <C extends Criteria<T>> Pair<AndCriteria<T>, Integer> getFirstMatchingMember(AndCriteria<T> rootCriteria, Class<C> criteriaClass, @Nullable Predicate<C> predicate) {		
		for (int i=0; i<rootCriteria.getCriterias().size(); i++) {
			var childCriteria = rootCriteria.getCriterias().get(i);
			if (isCriteriaMatching(childCriteria, criteriaClass, predicate) 
					|| (childCriteria instanceof OrCriteria && isMemberAllMatching((OrCriteria<T>)childCriteria, criteriaClass, predicate))) {
				return new Pair<>(rootCriteria, i);
			} else if (childCriteria instanceof AndCriteria) {
				var firstMatchingMember = getFirstMatchingMember((AndCriteria<T>) childCriteria, criteriaClass, predicate);
				if (firstMatchingMember != null) 
					return firstMatchingMember;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected <C extends Criteria<T>> List<C> getMatchingCriterias(@Nullable Criteria<T> rootCriteria, Class<C> criteriaClass, @Nullable Predicate<C> predicate) {	
		if (rootCriteria == null) {
			return new ArrayList<>();
		} else if (isCriteriaMatching(rootCriteria, criteriaClass, predicate)) {	
			return (List<C>) Lists.newArrayList(rootCriteria);
		} else if (rootCriteria instanceof AndCriteria) {
			var firstMatchingMember = getFirstMatchingMember((AndCriteria<T>) rootCriteria, criteriaClass, predicate);
			if (firstMatchingMember != null) {
				var matchingCriteria = firstMatchingMember.getLeft().getCriterias().get(firstMatchingMember.getRight());
				if (matchingCriteria instanceof OrCriteria) 
					return getFlattenedMembers((OrCriteria<T>) matchingCriteria);
				else
					return (List<C>) Lists.newArrayList(matchingCriteria);
			} else {
				return new ArrayList<>();
			}
		} else if (rootCriteria instanceof OrCriteria && isMemberAllMatching((OrCriteria<T>)rootCriteria, criteriaClass, predicate)) {
			return getFlattenedMembers((OrCriteria<T>)rootCriteria);
		} else {
			return new ArrayList<>();
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <C extends Criteria<T>> Criteria<T> setMatchingCriteria(@Nullable Criteria<T> rootCriteria, 
				Class<C> criteriaClass, @Nullable Criteria<T> criteria, @Nullable Predicate<C> predicate) {
		if (rootCriteria == null 
				|| isCriteriaMatching(rootCriteria, criteriaClass, predicate) 
				|| (rootCriteria instanceof OrCriteria && isMemberAllMatching((OrCriteria<T>)rootCriteria, criteriaClass, predicate))) {
			rootCriteria = criteria;
		} else if (rootCriteria instanceof AndCriteria) {
			var firstMatchingMember = getFirstMatchingMember((AndCriteria<T>) rootCriteria, criteriaClass, predicate);
			if (firstMatchingMember != null) {
				if (criteria != null) 
					firstMatchingMember.getLeft().getCriterias().set(firstMatchingMember.getRight(), criteria);
				else
					firstMatchingMember.getLeft().getCriterias().remove(firstMatchingMember.getRight().intValue());
			} else if (criteria != null) {
				((AndCriteria<T>) rootCriteria).getCriterias().add(criteria);
			}
		} else {
			rootCriteria = new AndCriteria<T>(rootCriteria, criteria);
		}
		return rootCriteria;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new FilterEditCssResourceReference()));
	}
}
