package io.onedev.server.web.page.project.issues.fieldvalues;

import javax.annotation.Nullable;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.web.component.IssueStateLabel;
import io.onedev.server.web.component.avatar.AvatarLink;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.issuedetail.activities.IssueActivitiesPage;
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
			UserManager userManager = OneDev.getInstance(UserManager.class);
			IssueManager issueManager = OneDev.getInstance(IssueManager.class);
			Fragment fragment = new Fragment("content", "nonEmptyValuesFrag", this);
			RepeatingView valuesView = new RepeatingView("values");
			for (String value: getField().getValues()) {
				if (getField().getType().equals(InputSpec.USER_CHOICE)) {
					User user = User.getForDisplay(userManager.findByName(value), value);
					Fragment userFrag = new Fragment(valuesView.newChildId(), "userFrag", this);
					userFrag.add(new UserLink("name", user));
					userFrag.add(new AvatarLink("avatar", user));
					valuesView.add(userFrag);
				} else if (getField().getType().equals(InputSpec.ISSUE_CHOICE)) {
					Issue issue = issueManager.find(project, Long.valueOf(value));
					if (issue != null) {
						Fragment issueFrag = new Fragment(valuesView.newChildId(), "issueFrag", this);
						Link<Void> link = new BookmarkablePageLink<Void>("link", IssueActivitiesPage.class, IssueActivitiesPage.paramsOf(issue, null));
						link.add(new Label("label", "#" + issue.getNumber()));
						issueFrag.add(link);
						issueFrag.add(new IssueStateLabel("state", Model.of(issue)));
						valuesView.add(issueFrag);
					} else {
						valuesView.add(new Label(valuesView.newChildId(), "#" + value));
					}
				} else {
					Label label = new Label(valuesView.newChildId(), value);
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
								label.add(AttributeAppender.append("class", "has-color"));
							}
						} finally {
							OneContext.pop();
						}
					} 
					valuesView.add(label);
				}
			}
			fragment.add(valuesView);
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
		IssueField field = getIssue().getEffectiveFields().get(name);
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
