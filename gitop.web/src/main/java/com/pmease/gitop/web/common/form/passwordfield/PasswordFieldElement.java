package com.pmease.gitop.web.common.form.passwordfield;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidator;

import com.pmease.gitop.web.common.form.AbstractInputElement;

public class PasswordFieldElement extends AbstractInputElement<String> {

  private static final long serialVersionUID = 1L;

  private IModel<String> model;
  private int size;
  private PasswordTextField passwordField;

  public PasswordFieldElement(String id, String label, IModel<String> model) {
    this(id, label, model, true);
  }

  public PasswordFieldElement(String id, String label, IModel<String> model, boolean required) {
    super(id, label, required);
    this.model = model;
  }

  @Override
  public FormComponent<String> getFormComponent() {
    return passwordField;
  }

  @Override
  protected Component createInputComponent(String id) {
    PasswordFieldElementPanel panel = new PasswordFieldElementPanel(id);
    panel.setRenderBodyOnly(true);
    passwordField = newTextInput("inputField");
    panel.add(passwordField);

    return panel;
  }

  protected PasswordTextField newTextInput(String id) {
    PasswordTextField passwordField = new PasswordTextField(id, model) {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isEnabled() {
        return !isReadOnly();
      }
    };

    passwordField.setRequired(isRequired());
    passwordField.setResetPassword(false);
    passwordField.setLabel(Model.of(getLabel()));

    if (size > 0) {
      passwordField.add(AttributeModifier.replace("size", String.valueOf(size)));
    }

    return passwordField;
  }

  @Override
  protected void addValidator(IValidator<String> validator) {
    passwordField.add(validator);
  }

  @Override
  protected IFeedbackMessageFilter getFeedbackMessageFilter() {
    return new ComponentFeedbackMessageFilter(passwordField);
  }

  public int getSize() {
    return size;
  }

  public PasswordFieldElement setSize(int size) {
    this.size = size;
    return this;
  }


}
