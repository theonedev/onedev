package com.pmease.gitplex.web.page.account.list;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.Link;

import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.page.layout.MaintabPage;

@SuppressWarnings("serial")
public abstract class AccountEditPage extends MaintabPage {

	private final User account;
	
	public AccountEditPage(User account) {
		this.account = account;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (account.isNew())
			add(new Label("title", "Add New Account"));
		else
			add(new Label("title", "Edit Account " + account.getDisplayName()));
		
		final BeanEditor<?> editor = BeanContext.editBean("editor", account);
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
			}

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				UserManager userManager = GitPlex.getInstance(UserManager.class);
				User accountWithSameEmail = userManager.findByEmail(account.getEmail());
				User accountWithSameName = userManager.findByName(account.getName());
				boolean hasError = false;
				if (account.isNew()) {
					if (accountWithSameName != null) {
						editor.getErrorContext(new PathSegment.Property("name"))
								.addError("This name has already been used by another account.");
						hasError = true;
					}
					if (accountWithSameEmail != null) {
						editor.getErrorContext(new PathSegment.Property("email"))
								.addError("This email has already been used by another account.");
						hasError = true;
					}
				} else {
					if (accountWithSameName != null && !accountWithSameName.equals(account)) {
						editor.getErrorContext(new PathSegment.Property("name"))
								.addError("This name has already been used by another account.");
						hasError = true;
					}
					if (accountWithSameEmail != null && !accountWithSameEmail.equals(account)) {
						editor.getErrorContext(new PathSegment.Property("email"))
								.addError("This email has already been used by another account.");
						hasError = true;
					}
				}
				
				if (!hasError) {
					userManager.save(account);
					
					onComplete();
				}
			}
			
		};
		form.add(editor);
		
		form.add(new SubmitLink("save"));
		form.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				onComplete();
			}
			
		});
		add(form);
	}

	protected abstract void onComplete();
}
