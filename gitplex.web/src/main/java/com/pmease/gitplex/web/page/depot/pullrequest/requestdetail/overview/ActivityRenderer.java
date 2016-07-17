package com.pmease.gitplex.web.page.depot.pullrequest.requestdetail.overview;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.PullRequest;

public interface ActivityRenderer extends Serializable {
	
	Date getDate();
	
	@Nullable
	Account getUser();
	
	PullRequest getRequest();
	
	Panel render(String panelId);
}
