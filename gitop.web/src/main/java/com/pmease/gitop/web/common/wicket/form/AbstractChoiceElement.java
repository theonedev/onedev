package com.pmease.gitop.web.common.wicket.form;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.WildcardListModel;

public abstract class AbstractChoiceElement<T, B extends AbstractChoiceElement<T, B>> 
	extends AbstractInputElement<T, B> {

  private static final long serialVersionUID = 1L;

  protected IModel<T> inputModel;
  protected IModel<List<? extends T>> choicesModel;
  protected IChoiceRenderer<? super T> choiceRenderer;

  public AbstractChoiceElement(String id, String label, IModel<T> inputModel, List<? extends T> choices) {
    this(id, label, inputModel, choices, null, false);
  }

  public AbstractChoiceElement(String id, String label, IModel<T> inputModel, List<? extends T> choices,
      IChoiceRenderer<? super T> renderer) {
    this(id, label, inputModel, choices, renderer, false);
  }

  public AbstractChoiceElement(String id, String label, IModel<T> inputModel, List<? extends T> choices,
      IChoiceRenderer<? super T> choiceRenderer, boolean required) {
    this(id, label, inputModel, new WildcardListModel<T>(choices), choiceRenderer, required);
  }

  public AbstractChoiceElement(String id, String label, IModel<T> inputModel) {
    this(id, label, inputModel, new WildcardListModel<T>(new ArrayList<T>()));
  }

  public AbstractChoiceElement(String id, String label, IModel<T> inputModel,
      IModel<List<? extends T>> choicesModel) {
    this(id, label, inputModel, choicesModel, null);
  }

  public AbstractChoiceElement(String id, String label, IModel<T> inputModel,
      IModel<List<? extends T>> choicesModel, IChoiceRenderer<? super T> choiceRenderer) {
    this(id, label, inputModel, choicesModel, choiceRenderer, false);
  }

  public AbstractChoiceElement(String id, String label, IModel<T> inputModel,
      IModel<List<? extends T>> choicesModel, IChoiceRenderer<? super T> choiceRenderer,
      boolean required) {
    super(id, label, required);

    this.inputModel = inputModel;
    this.choicesModel = choicesModel;
    this.choiceRenderer = choiceRenderer;
  }
}
