package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview;

import java.io.Serializable;
import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

public interface PullRequestActivity extends Serializable {
	
	Date getDate();
	
	Panel render(String panelId);
}
