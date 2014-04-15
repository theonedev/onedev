package com.pmease.gitop.web.page.project.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitop.model.User;

public interface PullRequestActivity {
	
	Date getDate();
	
	User getUser();
	
	String getAction();

	Panel render(String panelId);
}
