package io.onedev.server.model.support.issue.transitionspec;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;

import io.onedev.server.annotation.Editable;

@Editable(order=200, name="Pull request is opened or updated")
public class PullRequestOpenedOrUpdatedSpec extends PullRequestSpec {

	private static final long serialVersionUID = 1L;

	@Override
	public String getTriggerDescription() {
		if (getBranches() != null)
			return MessageFormat.format(_T("pull request to branches \"{0}\" is opened or updated"), getBranches());
		else
			return _T("pull request to any branch is opened or updated");
	}

}
