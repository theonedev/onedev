package com.pmease.gitplex.web.page.account.setting;

import java.io.Serializable;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.IErrorMessageSource;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;

import com.google.common.collect.Sets;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.wicket.editable.BeanDescriptor;
import com.pmease.commons.wicket.editable.BeanEditor;
import com.pmease.commons.wicket.editable.DefaultBeanDescriptor;
import com.pmease.commons.wicket.editable.PathSegment;
import com.pmease.commons.wicket.editable.reflection.ReflectionBeanEditor;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.web.component.confirmdelete.ConfirmDeleteAccountModal;
import com.pmease.gitplex.web.page.account.AccountPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class ProfileEditPage extends AccountSettingPage {

	private String newName;
	
	public ProfileEditPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		String oldName = getAccount().getName();
		newName = oldName;
		
		final Form<?> nameForm = new Form<Void>("name") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				GitPlex.getInstance(AccountManager.class).rename(getAccount().getId(), oldName, newName);
				setResponsePage(ProfileEditPage.class, paramsOf(newName));
				Session.get().success("Account has been renamed");
			}

			@Override
			protected void onError() {
				super.onError();
				add(AttributeAppender.append("class", "has-error"));
			}
			
		};
		TextField<String> input = new TextField<String>("input", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return newName;
			}

			@Override
			public void setObject(String object) {
				newName = object;
			}
			
		});
		input.setRequired(true);
		input.add(new IValidator<String>() {

			@Override
			public void validate(IValidatable<String> validatable) {
				Validator validator = AppLoader.getInstance(Validator.class);
				Set<?> violations = validator.validateValue(
						Account.class, "name", validatable.getValue());
				
				for (Object each: violations) {
					final ConstraintViolation<?> violation = (ConstraintViolation<?>) each;
					validatable.error(new IValidationError() {
						
						@Override
						public Serializable getErrorMessage(IErrorMessageSource messageSource) {
							return violation.getMessage();
						}
					});
				}
			}
			
		});
		nameForm.add(input);
		nameForm.add(new NotificationPanel("feedback", nameForm));
		
		add(nameForm);

		final BeanDescriptor beanDesciptor = new DefaultBeanDescriptor(Account.class, Sets.newHashSet("name", "password"));
		final BeanEditor<?> editor = new ReflectionBeanEditor("editor", beanDesciptor, new IModel<Serializable>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getAccount();
			}

			@Override
			public void setObject(Serializable object) {
				beanDesciptor.copyProperties(object, getAccount());
			}
			
		});
		
		Form<?> settingsForm = new Form<Void>("settings") {

			@Override
			protected void onError() {
				super.onError();
			}

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Account account = getAccount();
				AccountManager userManager = GitPlex.getInstance(AccountManager.class);
				Account accountWithSameName = userManager.findByName(account.getName());
				if (accountWithSameName != null && !accountWithSameName.equals(account)) {
					editor.getErrorContext(new PathSegment.Property("name"))
							.addError("This name has already been used by another account.");
				} else {
					userManager.save(account);
					Session.get().success("Profile has been updated");
					setResponsePage(ProfileEditPage.class, AccountPage.paramsOf(account));
				}
			}
			
		};
		settingsForm.add(editor);
		settingsForm.add(new SubmitLink("save"));

		add(settingsForm);
		
		add(new AjaxLink<Void>("delete") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				setVisible(!getAccount().isRoot());
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				new ConfirmDeleteAccountModal(target) {

					@Override
					protected void onDeleted(AjaxRequestTarget target) {
						setResponsePage(getApplication().getHomePage());
					}

					@Override
					protected Account getAccount() {
						return ProfileEditPage.this.getAccount();
					}
					
				};
			}
			
		});
		
	}

}
