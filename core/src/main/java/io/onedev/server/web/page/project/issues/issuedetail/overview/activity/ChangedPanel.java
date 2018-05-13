package io.onedev.server.web.page.project.issues.issuedetail.overview.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import com.google.common.base.Splitter;

import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.component.diff.plain.PlainDiffPanel;
import io.onedev.server.web.component.link.UserLink;

@SuppressWarnings("serial")
class ChangedPanel extends GenericPanel<IssueChange> {

	public ChangedPanel(String id, IModel<IssueChange> model) {
		super(id, model);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		IssueChange change = getModelObject();
		User userForDisplay = User.getForDisplay(change.getUser(), change.getUserName());
		add(new UserLink("user", userForDisplay));
		add(new Label("property", change.getProperty()));
		add(new Label("age", DateUtils.formatAge(change.getDate())));
		
		List<String> prevLines;
		if (change.getPrevContent() != null)
			prevLines = Splitter.on("\n").splitToList(change.getPrevContent());
		else
			prevLines = new ArrayList<>();
		
		List<String> lines;
		if (change.getContent() != null)
			lines = Splitter.on("\n").splitToList(change.getContent());
		else
			lines = new ArrayList<>();
		add(new PlainDiffPanel("body", prevLines, lines));
	}

}
