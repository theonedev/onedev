package io.onedev.server.search.commit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.command.RevListOptions;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;

public class CommitterCriteria extends CommitCriteria {

	private static final long serialVersionUID = 1L;

	private final List<String> values;
	
	public CommitterCriteria(List<String> values) {
		Preconditions.checkArgument(!values.isEmpty());
		this.values = values;
	}
	
	@Override
	public void fill(Project project, RevListOptions options) {
		for (String value: values) {
			if (value == null) { // committed by me
				User user = SecurityUtils.getUser();
				if (user != null) {
					user.getEmailAddresses().stream().filter(it->it.isVerified()).forEach(it-> {
						options.committers().add("<" + it.getValue() + ">");
					});
				} else {
					throw new ExplicitException("Please login to perform this query");
				}
			} else {
				options.committers().add(StringUtils.replace(value, "*", ".*"));
			}
		}
	}

	@Override
	public boolean matches(RefUpdated event) {
		RevCommit commit = event.getProject().getRevCommit(event.getNewCommitId(), true);
		String committerEmail = commit.getCommitterIdent().getEmailAddress();
		for (String value: values) {
			if (value == null) { // committed by me
				User user = User.get();
				if (user == null) {
					throw new ExplicitException("Please login to perform this query");
				} else if (user.getEmailAddresses().stream()
						.anyMatch(it-> it.isVerified() && it.getValue().equalsIgnoreCase(committerEmail))) { 
					return true;
				}
			} else {
				if (matches("*" + value + "*", commit.getCommitterIdent())) 
					return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		List<String> parts = new ArrayList<>();
		for (String value: values) {
			if (value != null)
				parts.add(getRuleName(CommitQueryLexer.COMMITTER) + parens(value));
			else
				parts.add(getRuleName(CommitQueryLexer.CommittedByMe));
		}
		return StringUtils.join(parts, " ");
	}
	
}
