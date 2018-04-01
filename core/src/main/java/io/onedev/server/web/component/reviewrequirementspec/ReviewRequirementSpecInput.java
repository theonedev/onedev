package io.onedev.server.web.component.reviewrequirementspec;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;

import io.onedev.server.model.Project;
import io.onedev.server.util.reviewrequirement.ReviewRequirement;

@SuppressWarnings("serial")
public class ReviewRequirementSpecInput extends TextField<String> {

	private final IModel<Project> projectModel;
	
	public ReviewRequirementSpecInput(String id, IModel<Project> projectModel, IModel<String> specModel) {
		super(id, specModel);
		
		this.projectModel = projectModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ReviewRequirementSpecAssistBehavior(projectModel));
		add(new IValidator<String>() {

			@Override
			public void validate(IValidatable<String> validatable) {
				try {
					ReviewRequirement.parse(validatable.getValue()); 
				} catch (Exception e) {
					validatable.error(new IValidationError() {

						@Override
						public Serializable getErrorMessage(IErrorMessageSource messageSource) {
							if (StringUtils.isNotBlank(e.getMessage()))
								return e.getMessage();
							else
								return "Syntax error";
						}
						
					});
				}
			}
			
		});
	}

	@Override
	protected void onDetach() {
		projectModel.detach();
		super.onDetach();
	}

}
