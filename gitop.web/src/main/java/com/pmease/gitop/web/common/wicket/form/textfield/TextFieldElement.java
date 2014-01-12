package com.pmease.gitop.web.common.wicket.form.textfield;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.validation.IValidator;

import com.pmease.gitop.web.common.wicket.form.AbstractInputElement;

public class TextFieldElement<T> extends AbstractInputElement<T, TextFieldElement<T>> {

  private static final long serialVersionUID = 1L;

  private IModel<T> model;
  private int size;
  private TextField<T> textField;

  public TextFieldElement(String id, String label, IModel<T> inputModel) {
    this(id, label, inputModel, true);
  }

  public TextFieldElement(String id, String label, IModel<T> inputModel, int size) {
    this(id, label, inputModel, true, size);
  }

  public TextFieldElement(String id, String label, IModel<T> inputModel, boolean required) {
    this(id, label, inputModel, required, -1);
  }

  public TextFieldElement(String id, String label, IModel<T> inputModel, boolean required, int size) {
    super(id, label, required);
    this.model = inputModel;
    this.size = size;
  }

  @Override
  public FormComponent<T> getFormComponent() {
    return textField;
  }

  @Override
  protected Component createInputComponent(String id) {
    TextFieldElementPanel panel = new TextFieldElementPanel(id);
    panel.setRenderBodyOnly(true);
    textField = newTextInput("inputField");
    textField.setRequired(isRequired());
    textField.setEnabled(!isReadOnly());
    textField.setLabel(Model.of(getLabel()));
    panel.add(textField);

    return panel;
  }

  protected TextField<T> newTextInput(String id) {
    TextField<T> textField = new TextField<T>(id, model) {

      private static final long serialVersionUID = 1L;

      @Override
      public <C> IConverter<C> getConverter(Class<C> type) {
        return getComponentConverter(type);
      }
    };

    if (size > 0) {
      textField.add(AttributeModifier.replace("size", String.valueOf(size)));
    }

    return textField;
  }

  protected <C> IConverter<C> getComponentConverter(Class<C> type) {
    return getApplication().getConverterLocator().getConverter(type);
  }

  @Override
   protected void addValidator(IValidator<T> validator) {
    textField.add(validator);
  }

  @Override
  protected IFeedbackMessageFilter getFeedbackMessageFilter() {
    return new ComponentFeedbackMessageFilter(getFormComponent());
  }

  @Override
  protected TextFieldElement<T> self() {
	return this;
  }
}
