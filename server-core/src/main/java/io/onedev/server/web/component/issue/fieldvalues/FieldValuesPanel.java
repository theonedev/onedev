package io.onedev.server.web.component.issue.fieldvalues;

import javax.annotation.Nullable;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.ObjectId;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.SecretInput;
import io.onedev.server.model.support.inputspec.choiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.model.support.issue.fieldspec.ChoiceField;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ColorUtils;
import io.onedev.server.util.ComponentContext;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.Input;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;
import io.onedev.server.web.page.project.commits.CommitDetailPage;
import io.onedev.server.web.page.project.issues.detail.IssueActivitiesPage;
import io.onedev.server.web.page.project.pullrequests.detail.activities.PullRequestActivitiesPage;

@SuppressWarnings("serial")
public abstract class FieldValuesPanel extends Panel implements EditContext {

	private final Mode userFieldDisplayMode;
	
	public FieldValuesPanel(String id, Mode userFieldDisplayMode) {
		super(id);
		this.userFieldDisplayMode = userFieldDisplayMode;
	}

	private GlobalIssueSetting getIssueSetting() {
		return OneDev.getInstance(SettingManager.class).getIssueSetting();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getField() != null && !getField().getValues().isEmpty()) {
			Fragment fragment = new Fragment("content", "nonEmptyValuesFrag", this);
			fragment.add(new ListView<String>("values", getField().getValues()) {

				@Override
				protected void populateItem(ListItem<String> item) {
					String value = item.getModelObject();
					if (getField().getType().equals(FieldSpec.USER)) {
						User user = User.from(OneDev.getInstance(UserManager.class).findByName(value), value);
						item.add(new UserIdentPanel("value", user, userFieldDisplayMode));
					} else if (getField().getType().equals(FieldSpec.ISSUE)) {
						Issue issue = OneDev.getInstance(IssueManager.class)
								.find(getIssue().getProject(), Long.valueOf(value));
						if (issue != null) {
							Fragment linkFrag = new Fragment("value", "linkFrag", FieldValuesPanel.this);
							Link<Void> link = new BookmarkablePageLink<Void>("link", IssueActivitiesPage.class, 
									IssueActivitiesPage.paramsOf(issue));
							link.add(new Label("label", "#" + issue.getNumber()));
							linkFrag.add(link);
							item.add(linkFrag);
						} else {
							item.add(new Label("value", "#" + value));
						}
					} else if (getField().getType().equals(FieldSpec.BUILD)) {
						Build build = OneDev.getInstance(BuildManager.class)
								.find(getIssue().getProject(), Long.valueOf(value));
						if (build != null) {
							Fragment linkFrag = new Fragment("value", "linkFrag", FieldValuesPanel.this);
							Link<Void> link = new BookmarkablePageLink<Void>("link", 
									BuildDashboardPage.class, BuildDashboardPage.paramsOf(build));
							String buildInfo = "#" + build.getNumber();
							if (build.getVersion() != null)
								buildInfo += " (" + build.getVersion() + ")";
							link.add(new Label("label", buildInfo));
							linkFrag.add(link);
							item.add(linkFrag);
						} else {
							item.add(new Label("value", "#" + value));
						}
					} else if (getField().getType().equals(FieldSpec.PULLREQUEST)) {
						PullRequest request = OneDev.getInstance(PullRequestManager.class)
								.find(getIssue().getProject(), Long.valueOf(value));
						if (request != null && SecurityUtils.canReadCode(request.getTargetProject())) {
							Fragment linkFrag = new Fragment("value", "linkFrag", FieldValuesPanel.this);
							Link<Void> link = new BookmarkablePageLink<Void>("link", PullRequestActivitiesPage.class, 
									PullRequestActivitiesPage.paramsOf(request));
							link.add(new Label("label", "#" + request.getNumber()));
							linkFrag.add(link);
							item.add(linkFrag);
						} else {
							item.add(new Label("value", "#" + value));
						}
					} else if (getField().getType().equals(FieldSpec.COMMIT)) {
						if (ObjectId.isId(value)) {
							if (SecurityUtils.canReadCode(getIssue().getProject())) {
								Fragment fragment = new Fragment("value", "commitFrag", FieldValuesPanel.this);
								Project project = getIssue().getProject();
								CommitDetailPage.State commitState = new CommitDetailPage.State();
								commitState.revision = value;
								PageParameters params = CommitDetailPage.paramsOf(project, commitState);
								Link<Void> hashLink = new ViewStateAwarePageLink<Void>("hashLink", CommitDetailPage.class, params);
								fragment.add(hashLink);
								hashLink.add(new Label("hash", GitUtils.abbreviateSHA(value)));
								fragment.add(new CopyToClipboardLink("copyHash", Model.of(value)));
								item.add(fragment);
							} else {
								Fragment fragment = new Fragment("value", "notAccessibleCommitFrag", FieldValuesPanel.this);
								fragment.add(new Label("hash", GitUtils.abbreviateSHA(value)));
								fragment.add(new CopyToClipboardLink("copyHash", Model.of(value)));
								item.add(fragment);
							}
						} else {
							item.add(new Label("value", value));
						}
					} else {
						Label label;
						if (getField().getType().equals(ParamSpec.SECRET))
							label = new Label("value", SecretInput.MASK);
						else 
							label = new Label("value", value);
						
						FieldSpec fieldSpec = getIssueSetting().getFieldSpec(getField().getName());
						if (fieldSpec != null && fieldSpec instanceof ChoiceField) {
							ChoiceProvider choiceProvider = ((ChoiceField)fieldSpec).getChoiceProvider();
							ComponentContext.push(new ComponentContext(this));
							try {
								String backgroundColor = choiceProvider.getChoices(false).get(value);
								if (backgroundColor == null)
									backgroundColor = "#E4E6EF";
								String fontColor = ColorUtils.isLight(backgroundColor)?"#3F4254":"white"; 
								String style = String.format(
										"background-color: %s; color: %s;", 
										backgroundColor, fontColor);
								label.add(AttributeAppender.append("style", style));
								label.add(AttributeAppender.append("class", "badge"));
							} finally {
								ComponentContext.pop();
							}
						} 
						item.add(label);
					}
					item.add(AttributeAppender.append("title", getField().getName()));
				}
				
			});
			add(fragment);
		} else if (getField() != null) {
			FieldSpec fieldSpec = null;
			if (getField() != null)
				fieldSpec = getIssueSetting().getFieldSpec(getField().getName());
			String displayValue;
			if (fieldSpec != null && fieldSpec.getNameOfEmptyValue() != null) 
				displayValue = fieldSpec.getNameOfEmptyValue();
			else
				displayValue = "Undefined";
			displayValue = HtmlEscape.escapeHtml5(displayValue);
			Label label = new Label("content", "<i>" + displayValue + "</i>");
			label.setEscapeModelStrings(false);
			label.add(AttributeAppender.append("title", getField().getName()));
			add(label);
			add(AttributeAppender.append("class", "undefined"));
		} else {
			add(new WebMarkupContainer("content"));
			setVisible(false);
		}
	}

	@Override
	public Object getInputValue(String name) {
		Input field = getIssue().getFieldInputs().get(name);
		FieldSpec fieldSpec = getIssueSetting().getFieldSpec(name);
		if (field != null && fieldSpec != null && field.getType().equals(EditableUtils.getDisplayName(fieldSpec.getClass()))) {
			return fieldSpec.convertToObject(field.getValues());
		} else {
			return null;
		}
	}

	protected abstract Issue getIssue();
	
	@Nullable
	protected abstract Input getField();

}
