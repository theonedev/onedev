package io.onedev.server.search.commit;

import com.google.common.base.Preconditions;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.command.RevListOptions;
import io.onedev.server.model.Project;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

import static io.onedev.server.util.match.WildcardUtils.matchString;

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
		for (String value: values) {
			if (value.length() >= 4 && project.getObjectId(value, false) != null) 
				options.revisions().add(value);
			else 
				options.messages().add(value);
		}
		if (options.revisions().size() == 1)
			options.count(1);
	}

	@Override
	public boolean matches(RefUpdated event) {
		var project = event.getProject();
		RevCommit commit = project.getRevCommit(event.getNewCommitId(), true);
		for (String value: values) {
			if (value.length() >= 4 && commit.equals(project.getObjectId(value, false)) 
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
