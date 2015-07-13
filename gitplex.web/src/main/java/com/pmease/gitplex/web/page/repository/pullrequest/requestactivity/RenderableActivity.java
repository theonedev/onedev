package com.pmease.gitplex.web.page.repository.pullrequest.requestactivity;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.model.User;

public interface RenderableActivity extends Serializable {
	
	Date getDate();
	
	@Nullable
	User getUser();
	
	Panel render(String panelId);
}
