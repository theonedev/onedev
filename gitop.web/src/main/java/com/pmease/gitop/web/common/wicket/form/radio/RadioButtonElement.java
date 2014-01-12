package com.pmease.gitop.web.common.wicket.form.radio;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidator;

import com.pmease.gitop.web.common.wicket.form.AbstractInputElement;

public class RadioButtonElement<T> extends AbstractInputElement<T, RadioButtonElement<T>> {

  private static final long serialVersionUID = 1L;

  protected final IModel<T> radioModel;
  protected Radio<T> radio;
  protected Panel radioContainer;

  public RadioButtonElement(String id, IModel<T> model, String label) {
    super(id, label);
    this.radioModel = model;
  }

  @Override
  protected Component createInputComponent(String id) {
    radioContainer = new RadioButtonElementPanel(id);
    radioContainer.add(radio = new Radio<T>("radio", radioModel));
    radioContainer.add(new Label("label", getLabel()));
    
    return radioContainer;
  }

  @Override
  protected void addValidator(IValidator<T> validator) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected IFeedbackMessageFilter getFeedbackMessageFilter() {
    return new ComponentFeedbackMessageFilter(radio);
  }

  @Override
  public FormComponent<T> getFormComponent() {
    throw new UnsupportedOperationException();
  }

  public Panel getContainer() {
    return radioContainer;
  }

  @Override
  protected RadioButtonElement<T> self() {
	return this;
  }
}
