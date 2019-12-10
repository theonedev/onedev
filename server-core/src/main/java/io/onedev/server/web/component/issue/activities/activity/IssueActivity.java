package io.onedev.server.web.component.issue.activities.activity;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.model.User;
import io.onedev.server.web.util.DeleteCallback;

public interface IssueActivity extends Serializable {
	
	Date getDate();
	
	Panel render(String panelId, DeleteCallback deleteCallback);
	
	User getUser();
	
	@Nullable
	String getAnchor();
}
