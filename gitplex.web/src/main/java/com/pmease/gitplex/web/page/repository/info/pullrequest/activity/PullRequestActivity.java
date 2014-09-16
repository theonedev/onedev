package com.pmease.gitplex.web.page.repository.info.pullrequest.activity;

import java.io.Serializable;
import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.model.User;

public interface PullRequestActivity extends Serializable {
	
	Date getDate();
	
	User getUser();
	
	Panel render(String panelId);
}
