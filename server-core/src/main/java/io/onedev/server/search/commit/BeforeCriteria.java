package io.onedev.server.search.commit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.base.Preconditions;

import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.command.RevListOptions;
import io.onedev.server.model.Project;
import io.onedev.server.util.DateUtils;

public class BeforeCriteria extends CommitCriteria {

	private static final long serialVersionUID = 1L;

	private final List<String> values;
	
	public BeforeCriteria(List<String> values) {
		Preconditions.checkArgument(!values.isEmpty());
		this.values = values;
	}
	
	public Date getDate() {
		return DateUtils.parseRelaxed(values.get(0));
	}
	
	@Override
	public void fill(Project project, RevListOptions options) {
		for (String value: values)
			options.before(DateUtils.formatISO8601Date(DateUtils.parseRelaxed(value)));
	}

	@Override
	public boolean matches(RefUpdated event) {
		RevCommit commit = event.getProject().getRevCommit(event.getNewCommitId(), true);
		for (String value: values) {
			if (!commit.getCommitterIdent().getWhen().before(DateUtils.parseRelaxed(value)))
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		List<String> parts = new ArrayList<>();
		for (String value: values) 
			parts.add(getRuleName(CommitQueryLexer.BEFORE) + parens(value));
		return StringUtils.join(parts, " ");
	}
	
}
