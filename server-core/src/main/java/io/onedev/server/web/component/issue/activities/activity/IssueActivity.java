package io.onedev.server.web.component.issue.activities.activity;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.web.util.DeleteCallback;
import io.onedev.server.util.userident.UserIdent;

public interface IssueActivity extends Serializable {
	
	Date getDate();
	
	Panel render(String panelId, DeleteCallback deleteCallback);
	
	UserIdent getUser();
	
	@Nullable
	String getAnchor();
}
