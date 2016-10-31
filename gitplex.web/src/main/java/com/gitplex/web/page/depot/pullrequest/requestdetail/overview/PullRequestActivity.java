package com.gitplex.web.page.depot.pullrequest.requestdetail.overview;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.panel.Panel;

public interface PullRequestActivity extends Serializable {
	
	Date getDate();
	
	Panel render(String panelId);
	
	@Nullable
	String getAnchor();
}
