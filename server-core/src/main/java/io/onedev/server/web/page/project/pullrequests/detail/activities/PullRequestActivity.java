package io.onedev.server.web.page.project.pullrequests.detail.activities;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.web.util.DeleteCallback;
import io.onedev.server.util.userident.UserIdent;

public interface PullRequestActivity extends Serializable {
	
	Date getDate();
	
	Component render(String componentId, DeleteCallback deleteCallback);
	
	UserIdent getUser();
	
	@Nullable
	String getAnchor();
}
