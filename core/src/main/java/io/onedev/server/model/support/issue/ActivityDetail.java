package io.onedev.server.model.support.issue;

import java.io.Serializable;

import org.apache.wicket.Component;

import io.onedev.server.model.IssueActivity;

public interface ActivityDetail extends Serializable {
	
	Component render(String componentId, IssueActivity activity);
	
}
