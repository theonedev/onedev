package com.pmease.gitop.web.common.wicket.form.checkbox;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidator;

import com.google.common.base.Strings;
import com.pmease.gitop.web.common.wicket.form.AbstractInputElement;

public class CheckBoxElement extends AbstractInputElement<Boolean> {

  private static final long serialVersionUID = 1L;

  protected IModel<String> description;
  protected IModel<Boolean> model;
  protected CheckBox checkbox;

  public CheckBoxElement(String id, IModel<Boolean> inputModel, IModel<String> description) {
    this(id, "", inputModel, description);
  }

  public CheckBoxElement(String id, String label, IModel<Boolean> inputModel, IModel<String> description) {
    super(id, label);
    this.model = inputModel;
    this.description = description;
    setRequired(false);
  }

  @Override
  protected void onInitialize() {
    super.onInitialize();
  }

  @Override
  public FormComponent<Boolean> getFormComponent() {
    return checkbox;
  }

  @Override
  protected Component createInputComponent(String id) {
    CheckBoxElementPanel panel = new CheckBoxElementPanel(id);
    panel.setRenderBodyOnly(true);

    checkbox = new CheckBox("check", model);
    panel.add(checkbox);
    Label label = (Label) createDescriptionLabel("description");

    panel.add(label);
    return panel;
  }

  protected Component createDescriptionLabel(String id) {
	  return new Label(id, description).setVisibilityAllowed(
	            description != null &&
	            !Strings.isNullOrEmpty(description.getObject()))
	            .setEscapeModelStrings(isEscapeDescriptionString());
  }
  
  protected boolean isEscapeDescriptionString() {
    return false;
  }

  @Override
  protected void addValidator(IValidator<Boolean> validator) {
    checkbox.add(validator);
  }

  @Override
  protected IFeedbackMessageFilter getFeedbackMessageFilter() {
    return new ComponentFeedbackMessageFilter(checkbox);
  }
}
