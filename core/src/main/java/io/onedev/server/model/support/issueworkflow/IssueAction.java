package io.onedev.server.model.support.issueworkflow;

import io.onedev.server.model.support.submitter.Submitter;

public interface IssueAction {

	String getName();
	
	Submitter getSubmitter();
	
}
