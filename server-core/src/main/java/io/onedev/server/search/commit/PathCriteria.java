package io.onedev.server.search.commit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.base.Preconditions;

import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.command.RevListCommand;
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
	public void fill(Project project, RevListCommand command) {
		for (String value: values)
			command.paths().add(value);
	}

	@Override
	public boolean matches(RefUpdated event) {
		Project project = event.getProject();
		RevCommit commit = project.getRevCommit(event.getNewCommitId(), true);
		
		Collection<String> changedFiles;
		if (!event.getOldCommitId().equals(ObjectId.zeroId()))
			changedFiles = GitUtils.getChangedFiles(project.getRepository(), event.getOldCommitId(), event.getNewCommitId());
		else if (commit.getParentCount() != 0)
			changedFiles = GitUtils.getChangedFiles(project.getRepository(), commit.getParent(0), event.getNewCommitId());
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
