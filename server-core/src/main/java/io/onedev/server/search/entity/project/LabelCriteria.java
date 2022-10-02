package io.onedev.server.search.entity.project;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.Project;
import io.onedev.server.model.ProjectLabel;
import io.onedev.server.util.criteria.Criteria;

public class LabelCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	private final LabelSpec labelSpec;
	
	public LabelCriteria(LabelSpec labelSpec) {
		this.labelSpec = labelSpec;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		var labelQuery = query.subquery(ProjectLabel.class);
		var labelRoot = labelQuery.from(ProjectLabel.class);
		labelQuery.select(labelRoot);

		return builder.exists(labelQuery.where(
				builder.equal(labelRoot.get(ProjectLabel.PROP_PROJECT), from), 
				builder.equal(labelRoot.get(ProjectLabel.PROP_SPEC), labelSpec)));
	}

	@Override
	public boolean matches(Project project) {
		return project.getLabels().stream().anyMatch(it->it.getSpec().equals(labelSpec));
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(Project.NAME_LABEL) + " " 
				+ ProjectQuery.getRuleName(ProjectQueryLexer.Is) + " " 
				+ Criteria.quote(labelSpec.getName());
	}

}
