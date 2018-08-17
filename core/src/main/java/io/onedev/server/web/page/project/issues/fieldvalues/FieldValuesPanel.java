package io.onedev.server.web.page.project.issues.fieldvalues;

import javax.annotation.Nullable;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.web.component.BuildStatusLabel;
import io.onedev.server.web.component.RequestStatusLabel;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.issuestate.IssueStateLabel;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.issuedetail.activities.IssueActivitiesPage;
import io.onedev.server.web.page.project.pullrequests.requestdetail.activities.RequestActivitiesPage;
import io.onedev.server.web.util.ComponentContext;
import io.onedev.utils.ColorUtils;

@SuppressWarnings("serial")
public abstract class FieldValuesPanel extends Panel implements EditContext {

	public FieldValuesPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Project project = ((ProjectPage) getPage()).getProject();
		
		if (getField() != null && !getField().getValues().isEmpty()) {
			Fragment fragment = new Fragment("content", "nonEmptyValuesFrag", this);
			fragment.add(new ListView<String>("values", getField().getValues()) {

				@Override
				protected void populateItem(ListItem<String> item) {
					String value = item.getModelObject();
					if (getField().getType().equals(InputSpec.USER)) {
						User user = User.getForDisplay(OneDev.getInstance(UserManager.class).findByName(value), value);
						Fragment userFrag = new Fragment("value", "userFrag", FieldValuesPanel.this);
						userFrag.add(new UserLink("name", user));
						userFrag.add(new AvatarLink("avatar", user));
						item.add(userFrag);
					} else if (getField().getType().equals(InputSpec.ISSUE)) {
						Issue issue = OneDev.getInstance(IssueManager.class).find(project, Long.valueOf(value));
						if (issue != null) {
							Fragment issueFrag = new Fragment("value", "issueFrag", FieldValuesPanel.this);
							Link<Void> link = new BookmarkablePageLink<Void>("link", IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(issue, null));
							link.add(new Label("label", "#" + issue.getNumber()));
							issueFrag.add(link);
							issueFrag.add(new IssueStateLabel("state", Model.of(issue)));
							item.add(issueFrag);
						} else {
							item.add(new Label("values", "#" + value));
						}
					} else if (getField().getType().equals(InputSpec.BUILD)) {
						Build build = OneDev.getInstance(BuildManager.class).find(project, Long.valueOf(value));
						if (build != null) {
							Fragment buildFrag = new Fragment("value", "buildFrag", FieldValuesPanel.this);
							ExternalLink link = new ExternalLink("link", build.getUrl());
							link.add(new Label("label", "#" + build.getNumber()));
							buildFrag.add(link);
							buildFrag.add(new BuildStatusLabel("status", Model.of(build)));
							item.add(buildFrag);
						} else {
							item.add(new Label("values", "#" + value));
						}
					} else if (getField().getType().equals(InputSpec.PULLREQUEST)) {
						PullRequest request = OneDev.getInstance(PullRequestManager.class).find(project, Long.valueOf(value));
						if (request != null) {
							Fragment requestFrag = new Fragment("value", "pullRequestFrag", FieldValuesPanel.this);
							Link<Void> link = new BookmarkablePageLink<Void>("link", RequestActivitiesPage.class, RequestActivitiesPage.paramsOf(request, null));
							link.add(new Label("label", "#" + request.getNumber()));
							requestFrag.add(link);
							requestFrag.add(new RequestStatusLabel("status", Model.of(request)));
							item.add(requestFrag);
						} else {
							item.add(new Label("values", "#" + value));
						}
					} else {
						Label label = new Label("value", value);
						InputSpec fieldSpec = getIssue().getProject().getIssueWorkflow().getFieldSpec(getField().getName());
						if (fieldSpec != null && fieldSpec instanceof ChoiceInput) {
							ChoiceProvider choiceProvider = ((ChoiceInput)fieldSpec).getChoiceProvider();
							OneContext.push(new ComponentContext(this));
							try {
								String backgroundColor = choiceProvider.getChoices(false).get(value);
								if (backgroundColor != null) {
									String fontColor = ColorUtils.isLight(backgroundColor)?"black":"white"; 
									String style = String.format(
											"background-color: %s; color: %s;", 
											backgroundColor, fontColor);
									label.add(AttributeAppender.append("style", style));
									item.add(AttributeAppender.append("class", "has-color"));
								}
							} finally {
								OneContext.pop();
							}
						} 
						item.add(label);
					}
				}
				
			});
			add(fragment);
		} else {
			InputSpec fieldSpec = null;
			if (getField() != null)
				fieldSpec = getIssue().getProject().getIssueWorkflow().getFieldSpec(getField().getName());
			String displayValue;
			if (fieldSpec != null && fieldSpec.getNameOfEmptyValue() != null) 
				displayValue = fieldSpec.getNameOfEmptyValue();
			else
				displayValue = "Undefined";
			displayValue = HtmlEscape.escapeHtml5(displayValue);
			add(new Label("content", "<i>" + displayValue + "</i>").setEscapeModelStrings(false));
		}		
	}

	@Override
	public Object getInputValue(String name) {
		IssueField field = getIssue().getFields().get(name);
		InputSpec fieldSpec = getIssue().getProject().getIssueWorkflow().getFieldSpec(name);
		if (field != null && fieldSpec != null && field.getType().equals(EditableUtils.getDisplayName(fieldSpec.getClass()))) {
			return fieldSpec.convertToObject(field.getValues());
		} else {
			return null;
		}
	}

	protected abstract Issue getIssue();
	
	@Nullable
	protected abstract IssueField getField();

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new FieldValuesCssResourceReference()));
	}
	
}
