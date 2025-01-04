package io.onedev.server.search.commit;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.command.RevListOptions;
import io.onedev.server.git.service.RefFacade;
import io.onedev.server.model.Project;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.onedev.commons.utils.match.WildcardUtils.matchString;

public class FuzzyCriteria extends CommitCriteria {

	private static final long serialVersionUID = 1L;

	private final List<String> values;
	
	public FuzzyCriteria(List<String> values) {
		Preconditions.checkArgument(!values.isEmpty());
		this.values = values;
	}
	
	public List<String> getValues() {
		return values;
	}
	
	@Override
	public void fill(Project project, RevListOptions options) {
		boolean ranged = false;
		for (String value: values) {
			if (project.getBranchRef(value) != null) {
				options.revisions().add(value);
				ranged = true;
			} else if (project.getTagRef(value) != null || value.length() >= 6 && project.getObjectId(value, false) != null) {
				options.revisions().add(value);
			} else {
				options.messages().add(value);
			}
		}
		if (options.revisions().size() == 1 && !ranged)
			options.count(1);
	}

	@Override
	public boolean matches(RefUpdated event) {
		var project = event.getProject();
		RevCommit commit = project.getRevCommit(event.getNewCommitId(), true);
		for (String value: values) {
			if (commit.equals(project.getRevCommit(value, false))
					|| matchString("*" + value + "*", commit.getFullMessage())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		List<String> parts = new ArrayList<>();
		for (String value: values) 
			parts.add("~" + StringUtils.escape(value, "~") + "~");
		return StringUtils.join(parts, " ");
	}
	
}
