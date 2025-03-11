package io.onedev.server.util;

import java.util.List;

import io.onedev.server.model.Issue;

public class LinkGroup {
	
	private final LinkDescriptor descriptor;
	
	private final List<Issue> issues;
	
	public LinkGroup(LinkDescriptor descriptor, List<Issue> issues) {
		this.descriptor = descriptor;
		this.issues = issues;
	}
	
	public LinkDescriptor getDescriptor() {
		return descriptor;
	}
	
	public List<Issue> getIssues() {
		return issues;
	}
	
}