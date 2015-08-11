package com.pmease.gitplex.web.page.repository.pullrequest.requestdetail.overview;

import java.io.Serializable;
import java.util.Date;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.panel.Panel;

import com.pmease.gitplex.core.model.User;

interface RenderableActivity extends Serializable {
	
	Date getDate();
	
	@Nullable
	User getUser();
	
	Panel render(String panelId);
}
