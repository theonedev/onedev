package io.onedev.server.search.commit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.command.RevListOptions;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Project;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;

public class PathCriteria extends CommitCriteria {

	private static final long serialVersionUID = 1L;

	private final List<String> values;
	
	public PathCriteria(List<String> values) {
		Preconditions.checkArgument(!values.isEmpty());
		this.values = values;
	}
	
	public List<String> getValues() {
		return values;
	}

	@Override
	public void fill(Project project, RevListOptions options) {
		for (String value: values)
			options.paths().add(value);
	}

	@Override
	public boolean matches(RefUpdated event) {
		Project project = event.getProject();
		RevCommit commit = project.getRevCommit(event.getNewCommitId(), true);
		
		GitService gitService = OneDev.getInstance(GitService.class);
		Collection<String> changedFiles;
		if (!event.getOldCommitId().equals(ObjectId.zeroId())) 
			changedFiles = gitService.getChangedFiles(project, event.getOldCommitId(), event.getNewCommitId(), null);
		else if (commit.getParentCount() != 0)
			changedFiles = gitService.getChangedFiles(project, commit.getParent(0), event.getNewCommitId(), null);
		else
			changedFiles = new HashSet<>();
		
		Matcher matcher = new PathMatcher();
		for (String value: values) {
			for (String changedFile: changedFiles) {
				if (matcher.matches(value, changedFile)) 
					return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		List<String> parts = new ArrayList<>();
		for (String value: values) 
			parts.add(getRuleName(CommitQueryLexer.PATH) + parens(value));
		return StringUtils.join(parts, " ");
	}
	
}
