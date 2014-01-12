package com.pmease.gitop.web.common.wicket.form.select;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.WildcardListModel;
import org.apache.wicket.validation.IValidator;

import com.pmease.gitop.web.common.wicket.form.AbstractChoiceElement;

public class DropDownChoiceElement<T> extends AbstractChoiceElement<T, DropDownChoiceElement<T>> {

    private static final long serialVersionUID = 1L;

    private DropDownChoice<T> dropdownChoice;
    
    public DropDownChoiceElement(String id, String label, IModel<T> inputModel, List<? extends T> choices) {
        super(id, label, inputModel, choices);
    }
    
    public DropDownChoiceElement(String id, String label, IModel<T> inputModel, 
            List<? extends T> choices, IChoiceRenderer<? super T> renderer) {
        super(id, label, inputModel, choices, renderer);
    }
    
    public DropDownChoiceElement(
            String id,
            String label, 
            IModel<T> inputModel, 
            List<? extends T> choices, 
            IChoiceRenderer<? super T> choiceRenderer, 
            boolean required) {
        super(id, label, inputModel, new WildcardListModel<T>(choices), choiceRenderer, required);
    }
    
    public DropDownChoiceElement(String id, String label, IModel<T> inputModel) {
        super(id, label, inputModel);
    }
    
    public DropDownChoiceElement(String id, String label,
            IModel<T> inputModel, 
            IModel<List<? extends T>> choicesModel) {
        super(id, label, inputModel, choicesModel);
    }
    
    public DropDownChoiceElement(String id, String label,
            IModel<T> inputModel, 
            IModel<List<? extends T>> choicesModel, 
            IChoiceRenderer<? super T> choiceRenderer) {
        super(id, label, inputModel, choicesModel, choiceRenderer);
    }
    
    public DropDownChoiceElement(String id, String label,
            IModel<T> inputModel, 
            IModel<List<? extends T>> choicesModel, 
            IChoiceRenderer<? super T> choiceRenderer, 
            boolean required) {
        super(id, label, inputModel, choicesModel, choiceRenderer, required);
    }
    
    @Override
    protected Component createInputComponent(String id) {
        DropDownChoicePanel panel = new DropDownChoicePanel(id);
        dropdownChoice = newDropDownChoice("select");
        panel.add(dropdownChoice);
        return panel;
    }

    protected DropDownChoice<T> newDropDownChoice(String id) {
        return new DropDownChoice<T>(id, inputModel, choicesModel, choiceRenderer);
    }
    
    @Override
    protected void addValidator(IValidator<T> validator) {
        dropdownChoice.add(validator);
    }

    @Override
    protected IFeedbackMessageFilter getFeedbackMessageFilter() {
        return new ComponentFeedbackMessageFilter(dropdownChoice);
    }

    @Override
    public FormComponent<T> getFormComponent() {
        return dropdownChoice;
    }

	@Override
	protected DropDownChoiceElement<T> self() {
		return this;
	}
}
