package com.pmease.gitplex.web.component.fullbranchmatch;

import java.io.Serializable;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.util.fullbranchmatch.FullBranchMatchUtils;

@SuppressWarnings("serial")
public class FullBranchMatchInput extends TextField<String> {

	private static final Logger logger = LoggerFactory.getLogger(FullBranchMatchInput.class);
	
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
					FullBranchMatchUtils.parse(validatable.getValue()); 
				} catch (final Exception e) {
					logger.error("Error parsing branch match string: " + validatable.getValue(), e);
					if (e.getCause().getMessage() != null) {
						validatable.error(new IValidationError() {

							@Override
							public Serializable getErrorMessage(IErrorMessageSource messageSource) {
								return "Syntax error: " + e.getCause().getMessage();
							}
							
						});
					} else {
						validatable.error(new IValidationError() {

							@Override
							public Serializable getErrorMessage(IErrorMessageSource messageSource) {
								return "Syntax error: " + e.getCause().getClass().getSimpleName();
							}
							
						});
					}
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
