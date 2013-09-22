package com.pmease.gitop.web;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.markup.html.form.Form;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.wicket.editable.EditHelper;
import com.pmease.gitop.core.model.Repository;

@SuppressWarnings("serial")
public class HomePage extends BasePage {

	private static Repository repository = new Repository();
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final Repository cloned = SerializationUtils.clone(repository);
		final EditContext editContext = EditHelper.getContext(cloned);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				editContext.validate();
				if (!editContext.hasValidationError(true))
					repository = cloned;
			}
			
		};
		form.add(EditHelper.renderForEdit(editContext, "editor"));
		
		add(form);
	}

	@Override
	protected String getTitle() {
		return "Home Page";
	}

}