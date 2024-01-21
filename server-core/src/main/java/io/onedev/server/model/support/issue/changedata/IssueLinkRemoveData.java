package io.onedev.server.model.support.issue.changedata;

public class IssueLinkRemoveData extends IssueLinkChangeData {

	private static final long serialVersionUID = 1L;
	
	public IssueLinkRemoveData(String linkName, boolean opposite, String linkedIssueNumber) {
		super(linkName, opposite, linkedIssueNumber);
	}

	@Override
	public String getActivity() {
		return "removed \"" + getLinkName() + "\" " + getLinkedIssueNumber();
	}

}
