package io.onedev.server.search.entity.project;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLabel;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class LabelCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	private final LabelSpec labelSpec;
	
	private final int operator;
	
	public LabelCriteria(LabelSpec labelSpec, int operator) {
		this.labelSpec = labelSpec;
		this.operator = operator;
	}

	public LabelSpec getLabelSpec() {
		return labelSpec;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		var labelQuery = query.subquery(ProjectLabel.class);
		var labelRoot = labelQuery.from(ProjectLabel.class);
		labelQuery.select(labelRoot);

		var predicate = builder.exists(labelQuery.where(
				builder.equal(labelRoot.get(ProjectLabel.PROP_PROJECT), from), 
				builder.equal(labelRoot.get(ProjectLabel.PROP_SPEC), labelSpec)));
		
		if (operator == ProjectQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Project project) {
		var matches = project.getLabels().stream().anyMatch(it->it.getSpec().equals(labelSpec));
		if (operator == ProjectQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(Project.NAME_LABEL) + " " 
				+ ProjectQuery.getRuleName(operator) + " " 
				+ Criteria.quote(labelSpec.getName());
	}

}
