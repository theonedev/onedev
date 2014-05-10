package com.pmease.gitop.web.page.account;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;

import com.pmease.commons.editable.EditContext;
import com.pmease.commons.editable.EditableUtils;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.page.BasePage;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class RegisterPage extends BasePage {

	private EditContext editContext = EditableUtils.getContext(new User());
	
	@Override
	protected String getPageTitle() {
		return "Gitop - Sign Up";
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final Form<?> form = new Form<Void>("form");
		add(form);
		
		add(new NotificationPanel("feedback", form));
		
		form.add((Component)editContext.renderForEdit("editor"));

		form.add(new SubmitLink("submit", form) {

			@Override
			public void onSubmit() {
				super.onSubmit();
				
				editContext.validate();

				EditContext nameContext = editContext.getChildContexts().get("name");
				EditContext emailContext = editContext.getChildContexts().get("email");
				UserManager userManager = Gitop.getInstance(UserManager.class);
				User user = (User) editContext.getBean();
				if (!nameContext.hasValidationErrors() && userManager.findByName(user.getName()) != null) 
					nameContext.addValidationError("This name is already used by another user.");
				if (!emailContext.hasValidationErrors() && userManager.findByEmail(user.getEmail()) != null) 
					nameContext.addValidationError("This email address is already used by another user.");
				
				if (!editContext.hasValidationErrors()) {
					userManager.save(user);
					success("Account has been registered successfully.");
					
					// clear the form fields
					editContext = EditableUtils.getContext(new User());
					form.replace((Component)editContext.renderForEdit("editor"));
				}
			}
			
		});
	}

}
