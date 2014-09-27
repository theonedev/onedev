package com.pmease.gitplex.web.page.repository.admin;

import java.io.Serializable;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.time.Duration;

import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.commons.wicket.editable.PropertyContext;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.permission.ObjectPermission;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class IntegrationSettingPage extends RepositoryPage {

	public IntegrationSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("integrationSettingForm");
		add(form);
		
		form.add(new FeedbackPanel("feedback", form).hideAfter(Duration.seconds(5)));
		
		form.add(PropertyContext.editModel("branchStrategiesEditor", new LoadableDetachableModel<Serializable>() {

			@Override
			public Serializable load() {
				return getRepository().getIntegrationSetting();
			}

		}, "branchStrategies"));
		
		form.add(PropertyContext.editModel("defaultStrategyEditor", new LoadableDetachableModel<Serializable>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable load() {
				return getRepository().getIntegrationSetting();
			}
			
		}, "defaultStrategy"));
		
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				GitPlex.getInstance(RepositoryManager.class).save(getRepository());
				success("Downstream integration strategies have been updated.");
				target.add(form);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				
				error("Fix errors below.");
				target.add(form);
			}
			
		});

	}

	@Override
	protected String getPageTitle() {
		return "Integration Setting - " + getRepository();
	}

	@Override
	protected boolean isPermitted() {
		return super.isPermitted() 
				&& SecurityUtils.getSubject().isPermitted(ObjectPermission.ofRepositoryAdmin(getRepository()));
	}

}
