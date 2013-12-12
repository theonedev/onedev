package com.pmease.gitop.web.common.wicket.form;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.validation.IValidator;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public abstract class AbstractInputElement<T> extends ValidatableElement<T> {

  private static final long serialVersionUID = 1L;

  private final String label;
  private boolean required = true;
  private boolean readOnly = false;
  private String help;
  private List<IValidator<T>> validators = Lists.newArrayList();
  private String extraCssClass = "";

  abstract protected Component createInputComponent(String id);

  abstract protected void addValidator(IValidator<T> validator);

  abstract protected IFeedbackMessageFilter getFeedbackMessageFilter();

  public AbstractInputElement(String id, String label) {
    super(id);
    this.label = label;
  }

  public AbstractInputElement(String id, String label, boolean required) {
    super(id);
    this.label = label;
    this.required = required;
  }

  public AbstractInputElement(String id, String label, String help) {
    this(label, help, true);
  }

  public AbstractInputElement(String id, String label, String help, boolean required) {
    super(id);
    this.label = label;
    this.help = help;
    this.required = required;
  }

  public boolean hasError() {
    return getFormComponent().hasErrorMessage();
  }

  public AbstractInputElement<T> add(IValidator<T> validator) {
    validators.add(validator);
    return this;
  }

  @SuppressWarnings({"serial"})
  @Override
  protected void onInitialize() {
    super.onInitialize();

    MarkupContainer container = new WebMarkupContainer("fieldContainer");
    add(container);
    container.add(createFieldLabel("label"));
    container.add(createRequiredLabel("required"));
    container.add(createHelpLabel("help"));

    final FeedbackPanel feedback = new FeedbackPanel("feedback");

    feedback.setOutputMarkupId(true);
    container.add(feedback);

    Component c = createInputComponent("input");
    
    container.add(c);

    feedback.setFilter(getFeedbackMessageFilter());
    container.add(AttributeModifier.append("class", new AbstractReadOnlyModel<String>() {

      @Override
      public String getObject() {
        return getExtraCssClass() + " " + feedback.getCssClass();
      }
    }));

    for (IValidator<T> each : validators) {
      addValidator(each);
    }
  }

  public String getExtraCssClass() {
    return extraCssClass;
  }

  public AbstractInputElement<T> setExtraCssClass(String extraCssClass) {
    this.extraCssClass = extraCssClass;
    return this;
  }

  protected Component createFieldLabel(String id) {
    return new Label(id, label).setVisibilityAllowed(!Strings.isNullOrEmpty(label))
        .setEscapeModelStrings(false);
  }

  protected Component createRequiredLabel(String id) {
    return new Label(id, new AbstractReadOnlyModel<String>() {

      private static final long serialVersionUID = 1L;

      @Override
      public String getObject() {
        return isRequired() ? "*" : "&nbsp;";
      }
    }).setEscapeModelStrings(false).setVisibilityAllowed(!Strings.isNullOrEmpty(label));
  }

  protected Component createHelpLabel(String id) {
    return new Label(id, help).setVisibilityAllowed(!Strings.isNullOrEmpty(help))
        .setEscapeModelStrings(false);
  }

  public boolean isRequired() {
    return required;
  }

  public AbstractInputElement<T> setRequired(boolean required) {
    this.required = required;
    return this;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public AbstractInputElement<T> setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
    return this;
  }

  public String getHelp() {
    return help;
  }

  public AbstractInputElement<T> setHelp(String help) {
    this.help = help;
    return this;
  }

  public String getLabel() {
    return label;
  }
}
