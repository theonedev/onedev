package io.onedev.server.search.commit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.command.RevListCommand;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;

public class AuthorCriteria extends CommitCriteria {

	private static final long serialVersionUID = 1L;

	private final List<String> values;
	
	public AuthorCriteria(List<String> values) {
		Preconditions.checkArgument(!values.isEmpty());
		this.values = values;
	}
	
	@Override
	public void fill(Project project, RevListCommand command) {
		for (String value: values) {
			if (value == null) { // authored by me
				User user = SecurityUtils.getUser();
				if (user != null) {
					command.authors().add("<" + user.getEmail() + ">");
					if (user.getGitEmail() != null)
						command.authors().add("<" + user.getGitEmail() + ">");
					for (String email: user.getAlternateEmails()) 
						command.authors().add("<" + email + ">");
				} else {
					throw new ExplicitException("Please login to perform this query");
				}
			} else {
				command.authors().add(StringUtils.replace(value, "*", ".*"));
			}
		}
	}

	@Override
	public boolean matches(RefUpdated event) {
		RevCommit commit = event.getProject().getRevCommit(event.getNewCommitId(), true);
		String authorEmail = commit.getAuthorIdent().getEmailAddress();
		for (String value: values) {
			if (value == null) { // authored by me
				User user = User.get();
				if (user == null) {
					throw new ExplicitException("Please login to perform this query");
				} else if (user.getEmail().equals(authorEmail)
						|| user.getGitEmail()!=null && user.getGitEmail().equals(authorEmail)
						|| user.getAlternateEmails().contains(authorEmail)) { 
					return true;
				}
			} else {
				if (matches("*" + value + "*", commit.getAuthorIdent())) 
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
				parts.add(getRuleName(CommitQueryLexer.AUTHOR) + parens(value));
			else
				parts.add(getRuleName(CommitQueryLexer.AuthoredByMe));
		}
		return StringUtils.join(parts, " ");
	}
	
}
