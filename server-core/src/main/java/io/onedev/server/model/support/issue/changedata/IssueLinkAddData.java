package io.onedev.server.model.support.issue.changedata;

import java.io.Serial;

public class IssueLinkAddData extends IssueLinkChangeData {

	@Serial
	private static final long serialVersionUID = 1L;
	
	public IssueLinkAddData(String linkName, boolean opposite, String linkedIssueNumber) {
		super(linkName, opposite, linkedIssueNumber);
	}

	@Override
	public String getActivity() {
		return "added \"" + getLinkName() + "\" " + getLinkedIssueNumber();
	}

}
