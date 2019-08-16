package io.onedev.server.search.commit;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.base.Preconditions;

import io.onedev.server.OneException;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.command.RevListCommand;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.SecurityUtils;

public class CommitterCriteria extends CommitCriteria {

	private static final long serialVersionUID = 1L;

	private final List<String> values;
	
	public CommitterCriteria(List<String> values) {
		Preconditions.checkArgument(!values.isEmpty());
		this.values = values;
	}
	
	@Override
	public boolean needsLogin() {
		for (String value: values) {
			if (value == null) // committed by me
				return true;
		}
		return false;
	}

	@Override
	public void fill(Project project, RevListCommand command) {
		for (String value: values) {
			if (value == null) { // committed by me
				if (SecurityUtils.getUser() != null)
					command.committers().add("<" + SecurityUtils.getUser().getEmail() + ">");
				else
					throw new OneException("Please login to perform this query");
			} else {
				command.committers().add(StringUtils.replace(value, "*", ".*"));
			}
		}
	}

	@Override
	public boolean matches(RefUpdated event, User user) {
		RevCommit commit = event.getProject().getRevCommit(event.getNewCommitId(), true);
		for (String value: values) {
			if (value == null) { // committed by me
				if (user.getEmail().equals(commit.getCommitterIdent().getEmailAddress())) 
					return true;
			} else {
				if (matches("*" + value + "*", commit.getCommitterIdent())) 
					return true;
			}
		}
		return false;
	}

}
