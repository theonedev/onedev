package io.onedev.server.search.commit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.command.RevListOptions;
import io.onedev.server.git.service.RefFacade;
import io.onedev.server.model.Project;
import io.onedev.server.search.commit.Revision.Scope;

public class RevisionCriteria extends CommitCriteria {

	private static final long serialVersionUID = 1L;

	private final List<Revision> revisions;
	
	public RevisionCriteria(List<Revision> revisions) {
		Preconditions.checkArgument(!revisions.isEmpty());
		this.revisions = revisions;
	}
	
	@Override
	public void fill(Project project, RevListOptions options) {
		boolean ranged = false;
		for (Revision revision: revisions) {
			if (revision.getScope() == Scope.SINCE) {
				options.revisions().add("^" + revision.getValue());
				ranged = true;
			} else if (revision.getScope() == Scope.UNTIL) {
				options.revisions().add(revision.getValue());
				ranged = true;
			} else if (project.getBranchRef(revision.getValue()) != null) {
				ranged = true;
				options.revisions().add(revision.getValue());
			} else {
				options.revisions().add(revision.getValue());
			}
		}
		if (options.revisions().size() == 1 && !ranged)
			options.count(1);
	}

	@Override
	public boolean matches(RefUpdated event) {
		List<Revision> untilRevisions = revisions.stream().filter(it->it.getScope() != Scope.SINCE).collect(Collectors.toList());
		if (!untilRevisions.isEmpty()) {
			for (Revision revision: untilRevisions) {
				RefFacade ref = event.getProject().getRef(revision.getValue());
				if (ref != null && ref.getName().equals(event.getRefName())) 
					return true;
			}
			return false;
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
