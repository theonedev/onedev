package io.onedev.server.search.commit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.base.Preconditions;

import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.command.RevListCommand;
import io.onedev.server.model.Project;
import io.onedev.server.util.match.WildcardUtils;


public class MessageCriteria extends CommitCriteria {

	private static final long serialVersionUID = 1L;

	private final List<String> values;
	
	public MessageCriteria(List<String> values) {
		Preconditions.checkArgument(!values.isEmpty());
		this.values = values;
	}
	
	public List<String> getValues() {
		return values;
	}

	@Override
	public void fill(Project project, RevListCommand command) {
		for (String value: values)
			command.messages().add(value);
	}

	@Override
	public boolean matches(RefUpdated event) {
		RevCommit commit = event.getProject().getRevCommit(event.getNewCommitId(), true);
		for (String value: values) {
			if (WildcardUtils.matchString("*" + value + "*", commit.getFullMessage()))
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		List<String> parts = new ArrayList<>();
		for (String value: values) 
			parts.add(getRuleName(CommitQueryLexer.MESSAGE) + parens(value));
		return StringUtils.join(parts, " ");
	}
	
}
