package io.onedev.server.search.commit;

import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.command.RevListOptions;
import io.onedev.server.model.Project;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;

public class CommitterCriteria extends PersonCriteria {

	private static final long serialVersionUID = 1L;
	
	public CommitterCriteria(List<String> values) {
		super(values);
	}
	
	@Override
	public void fill(Project project, RevListOptions options) {
		fill(project, options.committers());
	}

	@Override
	public boolean matches(RefUpdated event) {
		RevCommit commit = event.getProject().getRevCommit(event.getNewCommitId(), true);
		return matches(commit.getCommitterIdent());
	}

	@Override
	public String toString() {
		return toString(CommitQueryLexer.COMMITTER, CommitQueryLexer.CommittedByMe);
	}
	
}
