package io.onedev.server.web.page.project.issues.issueboards;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.IssueBoard;
import io.onedev.server.model.support.issue.workflow.IssueWorkflow;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.util.inputspec.userchoiceinput.UserChoiceInput;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.util.ComponentContext;
import io.onedev.utils.ColorUtils;

@SuppressWarnings("serial")
abstract class BoardColumnPanel extends Panel implements EditContext {

	public BoardColumnPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		WebMarkupContainer content = new WebMarkupContainer("content");
		add(content);
		
		String title;
		String color = null;
		User user = null;
		if (getColumnIndex() != -1) {
			IssueWorkflow workflow = getProject().getIssueWorkflow();
			String columnName = getBoard().getColumns().get(getColumnIndex());
			String identifyField = getBoard().getIdentifyField();
			if (columnName != null) {
				title = columnName;
				if (identifyField.equals(Issue.STATE)) {
					color = workflow.getStateSpec(columnName).getColor();
				} else {
					InputSpec field = workflow.getFieldSpec(identifyField);
					if (field instanceof ChoiceInput) {
						ChoiceProvider choiceProvider = ((ChoiceInput)field).getChoiceProvider();
						OneContext.push(new ComponentContext(this));
						try {
							color = choiceProvider.getChoices(true).get(columnName);
						} finally {
							OneContext.pop();
						}
					} else if (field instanceof UserChoiceInput) {
						user = OneDev.getInstance(UserManager.class).findByName(columnName);
					}
				}
			} else {
				title = workflow.getFieldSpec(identifyField).getNameOfEmptyValue();
			}
		} else {
			title = "Backlog";
		}

		WebMarkupContainer head = new WebMarkupContainer("head");
		if (user != null) {
			head.add(new WebMarkupContainer("title").setVisible(false));
			head.add(new AvatarLink("avatarLink", user));
			head.add(new UserLink("userLink", user));
		} else {
			head.add(new Label("title", title));
			head.add(new WebMarkupContainer("avatarLink").setVisible(false));
			head.add(new WebMarkupContainer("userLink").setVisible(false));
		}
		head.add(AttributeAppender.append("title", getBoard().getIdentifyField()));
		content.add(head);
		if (color != null) {
			head.add(AttributeAppender.append("style", 
					"background:" + color + ";border-bottom-color:" + color + ";"));
			if (!ColorUtils.isLight(color))
				head.add(AttributeAppender.append("style", "color:white;"));
			content.add(AttributeAppender.append("style", "border-color:" + color + ";"));
		}
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		String script = String.format("onedev.server.issueBoards.onColumnDomReady('%s');", 
				getMarkupId());
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	@Override
	public Object getInputValue(String name) {
		return null;
	}

	protected abstract Project getProject();
	
	protected abstract IssueBoard getBoard();
	
	protected abstract Milestone getMilestone();
	
	protected abstract int getColumnIndex();
	
}
