package com.pmease.gitplex.web.page.account.list;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;

import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.editable.bean.PasswordBean;
import com.pmease.gitplex.web.page.layout.MaintabPage;

@SuppressWarnings("serial")
public abstract class PasswordEditPage extends MaintabPage {
	
	private final User account;
	
	public PasswordEditPage(User account) {
		this.account = account;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("title", "Change Password of " + account.getDisplayName()));
		
		final PasswordBean bean = new PasswordBean();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				account.setPassword(bean.getPassword());
				GitPlex.getInstance(UserManager.class).save(account);
				onComplete();
			}
			
		};
		add(form);
		
		form.add(BeanContext.editBean("editor", bean));
		
		form.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				onComplete();
			}
			
		});
	}
	
	protected abstract void onComplete();
}
