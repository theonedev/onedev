package com.pmease.gitplex.web.page.repository.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.model.User;

public interface PullRequestActivity {
	
	Date getDate();
	
	User getUser();
	
	String getAction();

	Panel render(String panelId);
}
