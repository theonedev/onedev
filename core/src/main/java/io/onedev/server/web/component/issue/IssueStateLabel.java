package io.onedev.server.web.component.issue;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import io.onedev.server.model.Issue;
import io.onedev.server.model.support.issue.workflow.StateSpec;
import io.onedev.utils.ColorUtils;

@SuppressWarnings("serial")
public class IssueStateLabel extends Label {

	private IModel<Issue> issueModel;
	
	public IssueStateLabel(String id, IModel<Issue> issueModel) {
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
		StateSpec stateSpec = issue.getProject().getIssueWorkflow().getStateSpec(issue.getState());
		if (stateSpec != null) {
			String fontColor = ColorUtils.isLight(stateSpec.getColor())?"black":"white";
			String style = String.format("background-color: %s; color: %s;", stateSpec.getColor(), fontColor);
			add(AttributeAppender.append("style", style));
			add(AttributeAppender.append("title", "State"));
		}
		
		add(AttributeAppender.append("class", "issue-state label"));
	}

}
