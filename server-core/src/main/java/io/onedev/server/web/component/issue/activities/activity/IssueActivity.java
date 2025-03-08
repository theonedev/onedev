package io.onedev.server.web.component.issue.activities.activity;

import java.io.Serializable;
import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

public interface IssueActivity extends Serializable {
	
	Date getDate();
	
	Panel render(String panelId);

}
