package io.onedev.server.web.page.project.pullrequests.detail.activities;

import java.io.Serializable;
import java.util.Date;

import org.apache.wicket.Component;

public interface PullRequestActivity extends Serializable {
	
	Date getDate();
	
	Component render(String componentId);
		
}
