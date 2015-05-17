package com.pmease.gitplex.web.page.account.setting;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.editable.DefaultBeanDescriptor;
import com.pmease.commons.editable.PropertyDescriptor;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.reflection.ReflectionBeanEditor;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.page.account.AccountPage;

@SuppressWarnings("serial")
public class ProfileEditPage extends AccountSettingPage {

	public ProfileEditPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		sidebar.add(new Label("title", "Profile of " + getAccount().getDisplayName()));
		
		final ProfileDescriptor profileDesciptor = new ProfileDescriptor();
		final BeanEditor<?> editor = new ReflectionBeanEditor("editor", profileDesciptor, new IModel<Serializable>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getAccount();
			}

			@Override
			public void setObject(Serializable object) {
				profileDesciptor.copyProperties(object, getAccount());
			}
			
		});
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onError() {
				super.onError();
			}

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				User account = getAccount();
				UserManager userManager = GitPlex.getInstance(UserManager.class);
				User accountWithSameName = userManager.findByName(account.getName());
				if (accountWithSameName != null && !accountWithSameName.equals(account)) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another account.");
				} else {
					userManager.save(account);
					Session.get().success("Profile has been updated");
					setResponsePage(ProfileEditPage.class, AccountPage.paramsOf(account));
					backToPrevPage();
				}
			}
			
		};
		form.add(editor);
		
		form.add(new SubmitLink("save"));
		form.add(new Link<Void>("cancel") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(prevPageRef != null);
			}

			@Override
			public void onClick() {
				backToPrevPage();
			}
			
		});
		sidebar.add(form);
	}

	private static class ProfileDescriptor extends DefaultBeanDescriptor {

		public ProfileDescriptor() {
			super(User.class);
			
			for (Iterator<PropertyDescriptor> it = propertyDescriptors.iterator(); it.hasNext();) {
				if (it.next().getPropertyName().equals("password"))
					it.remove();
			}
		}
		
	}
}
