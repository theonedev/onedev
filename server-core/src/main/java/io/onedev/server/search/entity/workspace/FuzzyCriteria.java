package io.onedev.server.search.entity.workspace;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.Workspace;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.OrCriteria;

public class FuzzyCriteria extends Criteria<Workspace> {

	private static final long serialVersionUID = 1L;

	private final String value;

	public FuzzyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query,
                                  From<Workspace, Workspace> from, CriteriaBuilder builder) {
		return parse(value).getPredicate(projectScope, query, from, builder);
	}

	@Override
	public boolean matches(Workspace workspace) {
		return parse(value).matches(workspace);
	}

	@SuppressWarnings("unchecked")
	private Criteria<Workspace> parse(String value) {
		return new OrCriteria<>(
				new BranchCriteria("*" + value + "*", WorkspaceQueryLexer.Is),
				new SpecCriteria("*" + value + "*", WorkspaceQueryLexer.Is));
	}

	@Override
	public String toStringWithoutParens() {
		return "~" + StringUtils.escape(value, "~") + "~";
	}

}
