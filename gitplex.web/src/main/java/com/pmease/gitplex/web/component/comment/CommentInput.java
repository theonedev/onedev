package com.pmease.gitplex.web.component.comment;

import static de.agilecoders.wicket.jquery.JQuery.$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;

import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.commons.wicket.component.tabbable.AjaxActionTab;
import com.pmease.commons.wicket.component.tabbable.Tab;
import com.pmease.commons.wicket.component.tabbable.Tabbable;
import com.pmease.gitplex.web.component.markdown.MarkdownPanel;

@SuppressWarnings("serial")
public class CommentInput extends FormComponentPanel<String> {

	private static final String TABS_ID = "tabs";
	
	private TextArea<String> input;
	
	private MarkdownPanel preview;
	
	public CommentInput(String id, IModel<String> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<Tab> tabs = new ArrayList<>();
		tabs.add(new AjaxActionTab(Model.of("Write")) {

			@Override
			protected void onSelect(AjaxRequestTarget target) {
				target.appendJavaScript($(input).chain("show").get());
				target.appendJavaScript($(preview).chain("hide").get());
				target.add(CommentInput.this.get(TABS_ID));
			}
			
		}.setSelected(true));
		
		tabs.add(new AjaxActionTab(Model.of("Preview")){

			@Override
			protected void onSelect(AjaxRequestTarget target) {
				// show/hide textarea at client side in order not to reset its height 
				// in case user adjusted the height
				target.appendJavaScript($(input).chain("hide").get());
				target.add(preview);
				target.appendJavaScript($(preview).chain("show").get());
				target.add(CommentInput.this.get(TABS_ID));
			}
			
		});
		add(new Tabbable(TABS_ID, tabs).setOutputMarkupId(true));
		
		add(input = new TextArea<String>("input", getModel()));
		input.setOutputMarkupId(true);
		input.add(new INullAcceptingValidator<String>() {

			@Override
			public void validate(IValidatable<String> validatable) {
				if (StringUtils.isBlank(validatable.getValue()))
					validatable.error(new IValidationError() {
						
						@Override
						public Serializable getErrorMessage(IErrorMessageSource messageSource) {
							return "Comment should not be empty";
						}
					});
			}
			
		});
		input.add(new AjaxFormComponentUpdatingBehavior("blur") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				
			}
			
		});
		
		add(preview = new MarkdownPanel("preview", getModel()));
		preview.setOutputMarkupId(true);

		add(new FeedbackPanel("feedback", input).hideAfter(Duration.seconds(3)));
	}

	@Override
	protected void convertInput() {
		setConvertedInput(input.getConvertedInput());
	}

}
