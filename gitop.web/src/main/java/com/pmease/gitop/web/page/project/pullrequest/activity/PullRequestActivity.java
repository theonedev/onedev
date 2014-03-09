package com.pmease.gitop.web.page.project.pullrequest.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

public interface PullRequestActivity {
	
	Date getDate();

	Panel render(String panelId);
}
