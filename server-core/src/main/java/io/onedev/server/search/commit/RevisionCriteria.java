package io.onedev.server.search.commit;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.command.RevListOptions;
import io.onedev.server.model.Project;

public class RevisionCriteria extends CommitCriteria {

	private static final long serialVersionUID = 1L;

	private final List<Revision> revisions;
	
	public RevisionCriteria(List<Revision> revisions) {
		Preconditions.checkArgument(!revisions.isEmpty());
		this.revisions = revisions;
	}

	public List<Revision> getRevisions() {
		return revisions;
	}
	
	@Override
	public void fill(Project project, RevListOptions options) {
		for (Revision revision: revisions) {
			var commitHash = revision.getRevCommit(project).name();
			if (revision.isSince()) {
				options.revisions().add("^" + commitHash);
			} else {
				options.revisions().add(commitHash);
			}
		}
	}

	@Override
	public boolean matches(RefUpdated event) {
		List<Revision> untilRevisions = revisions.stream().filter(it->!it.isSince()).collect(toList());
		if (!untilRevisions.isEmpty()) {
			return untilRevisions.stream().anyMatch(it->it.matchesRef(event.getProject(), event.getRefName()));
		} else {
			return true;
		}
	}

	@Override
	public String toString() {
		List<String> parts = new ArrayList<>();
		for (Revision revision: revisions) 
			parts.add(revision.toString());
		return StringUtils.join(parts, " ");
	}
	
}
