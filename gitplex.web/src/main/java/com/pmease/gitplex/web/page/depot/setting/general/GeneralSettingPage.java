package com.pmease.gitplex.web.page.depot.setting.general;

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
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.web.component.confirmdelete.ConfirmDeleteRepoModal;
import com.pmease.gitplex.web.page.account.depots.AccountDepotsPage;
import com.pmease.gitplex.web.page.depot.setting.DepotSettingPage;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

@SuppressWarnings("serial")
public class GeneralSettingPage extends DepotSettingPage {

	private BeanEditor<?> editor;
	
	private String newName;
	
	public GeneralSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		String oldName = getDepot().getName();
		newName = oldName;
		
		final Form<?> nameForm = new Form<Void>("name") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				GitPlex.getInstance(DepotManager.class).rename(getDepot().getOwner(), getDepot().getId(), oldName, newName);
				setResponsePage(GeneralSettingPage.class, paramsOf(getAccount(), newName));
				Session.get().success("Repository has been renamed");
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
						Depot.class, "name", validatable.getValue());
				
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
		
		BeanDescriptor beanDescriptor = new DefaultBeanDescriptor(Depot.class, Sets.newHashSet("name"));
		editor = new ReflectionBeanEditor("editor", beanDescriptor, new IModel<Serializable>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getDepot();
			}

			@Override
			public void setObject(Serializable object) {
				editor.getBeanDescriptor().copyProperties(object, getDepot());
			}
			
		});
		
		Form<?> settingsForm = new Form<Void>("settings") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Depot depot = getDepot();
				DepotManager depotManager = GitPlex.getInstance(DepotManager.class);
				Depot depotWithSameName = depotManager.findBy(getAccount(), depot.getName());
				if (depotWithSameName != null && !depotWithSameName.equals(depot)) {
					String errorMessage = "This name has already been used by another repository in account " 
							+ getAccount().getName() + "."; 
					editor.getErrorContext(new PathSegment.Property("name")).addError(errorMessage);
				} else {
					depotManager.save(depot);
					Session.get().success("General setting has been updated");
					setResponsePage(GeneralSettingPage.class, paramsOf(depot));
				}
			}
			
		};
		settingsForm.add(editor);
		settingsForm.add(new SubmitLink("save"));
		add(settingsForm);

		add(new AjaxLink<Void>("delete") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				new ConfirmDeleteRepoModal(target) {
					
					@Override
					protected void onDeleted(AjaxRequestTarget target) {
						setResponsePage(AccountDepotsPage.class, paramsOf(getAccount()));						
					}
					
					@Override
					protected Depot getDepot() {
						return GeneralSettingPage.this.getDepot();
					}
				};
			}
			
		});
	}

	@Override
	protected String getPageTitle() {
		return "General Setting - " + getDepot();
	}

}
