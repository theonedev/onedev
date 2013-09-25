package com.pmease.gitop.web;

import org.apache.wicket.markup.html.form.Form;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.wicket.editable.EditHelper;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.Repository;

@SuppressWarnings("serial")
public class TestPage extends BasePage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final EditContext editContext = EditHelper.getContext(new Repository());
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				editContext.validate();
				if (!editContext.hasValidationError(true)) {
					Repository repository = (Repository) editContext.getBean();
					repository.setOwner(Gitop.getInstance(UserManager.class).getRootUser());
					Gitop.getInstance(RepositoryManager.class).save(repository);
				}
			}
			
		};
		
		form.add(EditHelper.renderForEdit(editContext, "editor"));
		
		add(form);
	}

	@Override
	protected String getTitle() {
		return "Test page used by Robin";
	}

}
