package io.onedev.server.web.page.project.issues.fieldvalues;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.unbescape.html.HtmlEscape;

import io.onedev.server.OneDev;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.MultiValueIssueField;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.editable.EditableUtils;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.util.inputspec.choiceprovider.ChoiceProvider;
import io.onedev.server.util.inputspec.multichoiceinput.MultiChoiceInput;
import io.onedev.server.web.component.link.UserLink;
import io.onedev.server.web.util.ComponentContext;
import io.onedev.utils.ColorUtils;

@SuppressWarnings("serial")
public class FieldValuesPanel extends GenericPanel<MultiValueIssueField> implements EditContext {

	public FieldValuesPanel(String id, IModel<MultiValueIssueField> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getField() != null && !getField().getValues().isEmpty()) {
			UserManager userManager = OneDev.getInstance(UserManager.class);
			Fragment fragment = new Fragment("values", "nonEmptyValuesFrag", this);
			RepeatingView valuesView = new RepeatingView("values");
			for (String value: getField().getValues()) {
				WebMarkupContainer item = new WebMarkupContainer(valuesView.newChildId());
				if (getField().getType().equals(InputSpec.USER_CHOICE) 
						|| getField().getType().equals(InputSpec.USER_MULTI_CHOICE)) {
					User user = User.getForDisplay(userManager.findByName(value), value);
					Fragment userFrag = new Fragment("value", "userFrag", this);
					userFrag.add(new UserLink("name", user));
					item.add(userFrag);
				} else {
					Label label = new Label("value", value);
					InputSpec fieldSpec = getField().getIssue().getProject().getIssueWorkflow().getField(getField().getName());
					ChoiceProvider choiceProvider = null;
					if (fieldSpec != null && fieldSpec instanceof ChoiceInput) {
						choiceProvider = ((ChoiceInput)fieldSpec).getChoiceProvider();
					} else if (fieldSpec != null && fieldSpec instanceof MultiChoiceInput) {
						choiceProvider = ((MultiChoiceInput)fieldSpec).getChoiceProvider();
					} 
					if (choiceProvider != null) {
						OneContext.push(new ComponentContext(this));
						try {
							String backgroundColor = choiceProvider.getChoices(false).get(value);
							if (backgroundColor != null) {
								String fontColor = ColorUtils.isLight(backgroundColor)?"black":"white"; 
								String style = String.format(
										"background-color: %s; color: %s;", 
										backgroundColor, fontColor);
								label.add(AttributeAppender.append("style", style));
								if (fontColor.equals("white"))
									item.add(AttributeAppender.append("class", "white"));
							}
						} finally {
							OneContext.pop();
						}
					}
					item.add(label);
				}
				valuesView.add(item);
			}
			fragment.add(valuesView);
			add(fragment);
		} else {
			InputSpec fieldSpec = null;
			if (getField() != null)
				fieldSpec = getField().getIssue().getProject().getIssueWorkflow().getField(getField().getName());
			String displayValue;
			if (fieldSpec != null && fieldSpec.getNameOfEmptyValue() != null) 
				displayValue = fieldSpec.getNameOfEmptyValue();
			else
				displayValue = "Undefined";
			displayValue = HtmlEscape.escapeHtml5(displayValue);
			add(new Label("values", "<i>" + displayValue + "</i>").setEscapeModelStrings(false));
		}		
	}

	@Override
	public Object getInputValue(String name) {
		MultiValueIssueField field = getField().getIssue().getMultiValueFields().get(name);
		InputSpec fieldSpec = getField().getIssue().getProject().getIssueWorkflow().getField(name);
		if (field != null && fieldSpec != null && field.getType().equals(EditableUtils.getDisplayName(fieldSpec.getClass()))) {
			return fieldSpec.convertToObject(field.getValues());
		} else {
			return null;
		}
	}

	private MultiValueIssueField getField() {
		return getModelObject();
	}
	
}
