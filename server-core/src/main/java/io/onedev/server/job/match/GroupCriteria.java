package io.onedev.server.job.match;

import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import static io.onedev.server.job.match.JobMatchLexer.SubmittedByGroup;

public class GroupCriteria extends Criteria<JobMatchContext> {

	private static final long serialVersionUID = 1L;
	
	private Group group;
	
	public GroupCriteria(Group group) {
		this.group = group;
	}

	@Override
	public boolean matches(JobMatchContext context) {
		User user = context.getUser();
		return user != null && user.getGroups().contains(group);
	}

	@Override
	public void onRenameGroup(String oldName, String newName) {
		if (group.getName().equals(oldName))
			group.setName(newName);
	}

	@Override
	public boolean isUsingGroup(String groupName) {
		return group.getName().equals(groupName);
	}

	@Override
	public String toStringWithoutParens() {
		return JobMatch.getRuleName(SubmittedByGroup) + " " + quote(group.getName());
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<JobMatchContext, JobMatchContext> from,
			CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
}
