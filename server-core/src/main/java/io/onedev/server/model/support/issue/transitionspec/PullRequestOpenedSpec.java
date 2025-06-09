package io.onedev.server.model.support.issue.transitionspec;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;

import io.onedev.server.annotation.Editable;

@Editable(order=200, name="Pull request is opened")
public class PullRequestOpenedSpec extends PullRequestSpec {

	private static final long serialVersionUID = 1L;

	@Override
	public String getTriggerDescription() {
		if (getBranches() != null)
			return MessageFormat.format(_T("pull request to branches \"{0}\" is opened"), getBranches());
		else
			return _T("pull request to any branch is opened");
	}

}
