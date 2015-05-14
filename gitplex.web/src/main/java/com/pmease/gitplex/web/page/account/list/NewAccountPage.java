package com.pmease.gitplex.web.page.account.list;

import java.io.Serializable;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.page.account.setting.PasswordBean;
import com.pmease.gitplex.web.page.main.MainPage;

@SuppressWarnings("serial")
public class NewAccountPage extends MainPage {

	private final User account;
	
	public NewAccountPage(User account) {
		this.account = account;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final BeanEditor<?> editor = BeanContext.editBean("editor", account);
		final AllowLoginBean allowLoginBean = new AllowLoginBean();
		
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
				
				if (!hasError) {
					if (allowLoginBean.getPasswordBean() != null)
						account.setPassword(allowLoginBean.getPasswordBean().getPassword());
					userManager.save(account);
					continueToOriginalDestination();
				}
			}
			
		};
		form.add(editor);
		form.add(BeanContext.editBean("passwordEditor", allowLoginBean));
		
		form.add(new SubmitLink("save"));
		form.add(new Link<Void>("cancel") {

			@Override
			public void onClick() {
				setResponsePage(AccountListPage.class);
			}
			
		});
		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(NewAccountPage.class, "account-list.css")));
	}

	@Editable
	public static class AllowLoginBean implements Serializable {
		
		private PasswordBean passwordBean = new PasswordBean();

		@Editable(name="Allow this Account to Login")
		public PasswordBean getPasswordBean() {
			return passwordBean;
		}

		public void setPasswordBean(PasswordBean passwordBean) {
			this.passwordBean = passwordBean;
		}
		
	}
}
