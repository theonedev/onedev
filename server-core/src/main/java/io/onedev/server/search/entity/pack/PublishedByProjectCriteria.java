package io.onedev.server.search.entity.pack;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Pack;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.commons.utils.match.WildcardUtils;

import javax.persistence.criteria.*;

public class PublishedByProjectCriteria extends Criteria<Pack> {

	private static final long serialVersionUID = 1L;

	private final String projectPath;

	public PublishedByProjectCriteria(String projectPath) {
		this.projectPath = projectPath;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Pack, Pack> from, CriteriaBuilder builder) {
		return OneDev.getInstance(ProjectManager.class).getPathMatchPredicate(
				builder, from.join(Pack.PROP_BUILD, JoinType.INNER).join(Build.PROP_PROJECT, JoinType.INNER), projectPath);
	}

	@Override
	public boolean matches(Pack pack) {
		if (pack.getBuild() != null) 
			return WildcardUtils.matchPath(projectPath, pack.getBuild().getProject().getPath());
		else 
			return false;
	}

	@Override
	public String toStringWithoutParens() {
		return PackQueryLexer.PublishedByProject + " " + quote(projectPath);
	}
}
