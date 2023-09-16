package io.onedev.server.model.support.issue.changedata;

public class IssueLinkAddData extends IssueLinkChangeData {

	private static final long serialVersionUID = 1L;
	
	public IssueLinkAddData(String linkName, boolean opposite, String linkedIssueNumber) {
		super(linkName, opposite, linkedIssueNumber);
	}

	@Override
	public String getActivity() {
		return "added \"" + getLinkName() + "\" " + getLinkedIssueNumber();
	}

}
