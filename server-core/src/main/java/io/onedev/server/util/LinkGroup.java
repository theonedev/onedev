package io.onedev.server.util;

import java.util.List;

import io.onedev.server.model.IssueLink;

public class LinkGroup {
	
	private final LinkDescriptor descriptor;
	
	private final List<IssueLink> issueLinks;
	
	public LinkGroup(LinkDescriptor descriptor, List<IssueLink> issueLinks) {
		this.descriptor = descriptor;
		this.issueLinks = issueLinks;
	}
	
	public LinkDescriptor getDescriptor() {
		return descriptor;
	}
	
	public List<IssueLink> getIssueLinks() {
		return issueLinks;
	}
	
}