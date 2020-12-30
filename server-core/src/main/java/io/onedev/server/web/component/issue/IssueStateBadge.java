package io.onedev.server.web.component.issue;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.util.ColorUtils;

@SuppressWarnings("serial")
public class IssueStateBadge extends Label {

	private IModel<Issue> issueModel;
	
	public IssueStateBadge(String id, IModel<Issue> issueModel) {
		super(id, issueModel.getObject().getState());
		this.issueModel = issueModel;
	}

	@Override
	protected void onDetach() {
		issueModel.detach();
		super.onDetach();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Issue issue = issueModel.getObject();
		StateSpec stateSpec = OneDev.getInstance(SettingManager.class).getIssueSetting().getStateSpec(issue.getState());
		if (stateSpec != null) {
			String fontColor = ColorUtils.isLight(stateSpec.getColor())?"#333":"#f9f9f9";
			String style = String.format("background-color: %s; color: %s;", stateSpec.getColor(), fontColor);
			add(AttributeAppender.append("style", style));
			add(AttributeAppender.append("title", "State"));
		}
		
		add(AttributeAppender.append("class", "issue-state badge"));
	}

}
