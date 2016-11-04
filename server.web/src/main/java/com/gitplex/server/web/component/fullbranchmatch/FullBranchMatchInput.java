package com.gitplex.server.web.component.fullbranchmatch;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;

import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.util.fullbranchmatch.FullBranchMatchUtils;

@SuppressWarnings("serial")
public class FullBranchMatchInput extends TextField<String> {

	private final IModel<Depot> depotModel;
	
	public FullBranchMatchInput(String id, IModel<Depot> depotModel, IModel<String> fullBranchMatchModel) {
		super(id, fullBranchMatchModel);
		
		this.depotModel = depotModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new FullBranchMatchBehavior(depotModel));
		add(new IValidator<String>() {

			@Override
			public void validate(IValidatable<String> validatable) {
				try {
					FullBranchMatchUtils.validate(validatable.getValue()); 
				} catch (final Exception e) {
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
		depotModel.detach();
		super.onDetach();
	}

}
