package io.onedev.server.model.support.issue.transitionspec;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;

import io.onedev.server.annotation.Editable;

@Editable(order=300, name="Pull request is discarded")
public class PullRequestDiscardedSpec extends PullRequestSpec {

	private static final long serialVersionUID = 1L;

	@Override
	public String getTriggerDescription() {
		if (getBranches() != null)
			return MessageFormat.format(_T("pull request to branches \"{0}\" is discarded"), getBranches());
		else
			return _T("pull request to any branch is discarded");
	}

}
