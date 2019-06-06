package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.pullrequest.CloseInfo;
import io.onedev.server.util.PullRequestConstants;

public class StatusCriteria extends PullRequestCriteria {

	private static final long serialVersionUID = 1L;

	private String value;
	
	public StatusCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(Project project, Root<PullRequest> root, CriteriaBuilder builder, User user) {
		Path<?> attribute = PullRequestQuery.getPath(root, PullRequestConstants.ATTR_CLOSE_STATUS);
		if (value.equalsIgnoreCase(PullRequestConstants.STATE_OPEN)) 
			return builder.isNull(attribute);
		else
			return builder.equal(attribute, CloseInfo.Status.valueOf(value.toUpperCase()));
	}

	@Override
	public boolean matches(PullRequest request, User user) {
		if (value.equalsIgnoreCase(PullRequestConstants.STATE_OPEN)) 
			return request.getCloseInfo() == null;
		else
			return request.getCloseInfo() != null && request.getCloseInfo().getStatus().name().equalsIgnoreCase(value);
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return PullRequestQuery.quote(PullRequestConstants.FIELD_STATUS) + " " + PullRequestQuery.getRuleName(PullRequestQueryLexer.Is) + " " + PullRequestQuery.quote(value);
	}

}
