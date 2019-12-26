package io.onedev.server.util.jobmatch;

import static io.onedev.server.util.jobmatch.JobMatch.getRuleName;
import static io.onedev.server.util.jobmatch.JobMatchLexer.Is;
import static io.onedev.server.util.query.BuildQueryConstants.FIELD_PROJECT_OWNER;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class ProjectOwnerCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;
	
	private String ownerName;
	
	public ProjectOwnerCriteria(String ownerName) {
		this.ownerName = ownerName;
	}

	@Override
	public boolean matches(Build build) {
		return WildcardUtils.matchString(ownerName, build.getProject().getOwner().getName());
	}

	@Override
	public void onRenameUser(String oldName, String newName) {
		if (oldName.equals(ownerName))
			ownerName = newName;
	}

	@Override
	public boolean isUsingUser(String userName) {
		return userName.equals(ownerName);
	}

	@Override
	public String asString() {
		return quote(FIELD_PROJECT_OWNER) + " " + getRuleName(Is) + " " + quote(ownerName);
	}
	
}
