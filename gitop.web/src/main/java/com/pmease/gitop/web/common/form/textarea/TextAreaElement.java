package com.pmease.gitop.web.common.form.textarea;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidator;

import com.pmease.gitop.web.common.form.AbstractInputElement;

public class TextAreaElement<T> extends AbstractInputElement<T> {

  private static final long serialVersionUID = 1L;

  private IModel<T> model;
  private TextArea<T> textarea;
  private int rows;

  public TextAreaElement(String label, IModel<T> model) {
    this(label, true, model);
  }

  public TextAreaElement(String label, boolean required, IModel<T> model) {
    this(label, null, required, model);
  }

  public TextAreaElement(String label, String help, IModel<T> model) {
    this(label, help, true, model);
  }

  public TextAreaElement(String label, String help, boolean required, IModel<T> model) {
    super(label, help, required);
    this.model = model;
    setExtraCssClass("textarea-field");
  }

  @Override
  public FormComponent<T> getFormComponent() {
    return textarea;
  }

  @Override
  protected Component createInputComponent(String id) {
    TextAreaElementPanel panel = new TextAreaElementPanel(id);
    textarea = newTextArea("inputField");
    textarea.setRequired(isRequired());
    textarea.setEnabled(!isReadOnly());
    textarea.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {
      private static final long serialVersionUID = 1L;

      @Override
      public String getObject() {
        if (isReadOnly()) {
          return "uneditable-input";
        } else {
          return "";
        }
      }
    }));
    textarea.add(AttributeModifier.replace("rows", new AbstractReadOnlyModel<String>() {

      private static final long serialVersionUID = 1L;

      @Override
      public String getObject() {
        if (rows > 0) {
          return String.valueOf(rows);
        } else {
          return "";
        }
      }
    }));

    panel.add(textarea);
    panel.setRenderBodyOnly(true);
    return panel;
  }

  protected TextArea<T> newTextArea(String id) {
    return new TextArea<T>(id, model);
  }

  @Override
  protected void addValidator(IValidator<T> validator) {
    textarea.add(validator);
  }

  @Override
  public TextAreaElement<T> add(IValidator<T> validator) {
    return (TextAreaElement<T>) super.add(validator);
  }

  @Override
  protected IFeedbackMessageFilter getFeedbackMessageFilter() {
    return new ComponentFeedbackMessageFilter(getFormComponent());
  }

  public int getRows() {
    return rows;
  }

  public TextAreaElement<T> setRows(int rows) {
    this.rows = rows;
    return this;
  }
}
