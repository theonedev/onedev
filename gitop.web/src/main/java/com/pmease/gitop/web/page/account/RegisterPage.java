package com.pmease.gitop.web.page.account;

import java.io.Serializable;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;

import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.ErrorContext;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.ValuePath;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.page.BasePage;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class RegisterPage extends BasePage {

	private BeanEditor<Serializable> beanEditor;
	
	private User user = new User();
	
	@Override
	protected String getPageTitle() {
		return "Gitop - Sign Up";
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final Form<?> form = new Form<Void>("form") {

			@Override
			protected void onValidate() {
				super.onValidate();

				User user = (User) beanEditor.getConvertedInput();
				
				ErrorContext nameContext = beanEditor.getErrorContext(new ValuePath(new PathSegment.Property("name")));
				ErrorContext emailContext = beanEditor.getErrorContext(new ValuePath(new PathSegment.Property("email")));
				
				UserManager userManager = Gitop.getInstance(UserManager.class);

				if (!nameContext.hasErrors() && userManager.findByName(user.getName()) != null) 
					nameContext.addError("This name is already used by another user.");
				if (!emailContext.hasErrors() && userManager.findByEmail(user.getEmail()) != null) 
					emailContext.addError("This email address is already used by another user.");
			}
			
		};
		add(form);
		
		add(new NotificationPanel("feedback", form));
		
		form.add(beanEditor = BeanContext.edit("editor", user));

		form.add(new SubmitLink("submit", form) {

			@Override
			public void onSubmit() {
				super.onSubmit();

				UserManager userManager = Gitop.getInstance(UserManager.class);
				userManager.save(user);
				success("Account has been registered successfully.");
					
				// clear the form fields
				user = new User();
				form.replace(BeanContext.edit("editor", user));
			}
			
		});
	}

}
