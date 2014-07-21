package com.pmease.gitplex.web.common.wicket.form;

import org.apache.wicket.markup.html.form.FormComponent;

public abstract class ValidatableElement<T> extends FormElement {

  private static final long serialVersionUID = 1L;

  public ValidatableElement(String id) {
    super(id);
  }

  abstract public FormComponent<T> getFormComponent();
}
