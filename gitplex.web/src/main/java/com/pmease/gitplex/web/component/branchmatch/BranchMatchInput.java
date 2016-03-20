package com.pmease.gitplex.web.component.branchmatch;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;

import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.util.includeexclude.IncludeExcludeUtils;

@SuppressWarnings("serial")
public class BranchMatchInput extends TextField<String> {

	private final IModel<Depot> depotModel;
	
	public BranchMatchInput(String id, IModel<Depot> depotModel, IModel<String> branchMatchModel) {
		super(id, branchMatchModel);
		
		this.depotModel = depotModel;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new BranchMatchBehavior(depotModel));
		add(new IValidator<String>() {

			@Override
			public void validate(IValidatable<String> validatable) {
				try {
					IncludeExcludeUtils.parse(validatable.getValue()); 
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
		depotModel.detach();
		super.onDetach();
	}

}
