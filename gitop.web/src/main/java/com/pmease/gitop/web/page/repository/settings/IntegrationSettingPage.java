package com.pmease.gitop.web.page.repository.settings;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.time.Duration;

import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.commons.wicket.editable.PropertyContext;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.model.integrationsetting.IntegrationStrategy;

@SuppressWarnings("serial")
public class IntegrationSettingPage extends AbstractRepositorySettingPage {

	public IntegrationSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer rebasibleBranchesContainer = new WebMarkupContainer("rebasibleBranchesContainer");
		add(rebasibleBranchesContainer);
		Form<?> rebasibleBranchesForm = new Form<Void>("form");
		rebasibleBranchesContainer.add(rebasibleBranchesForm);

		rebasibleBranchesForm.add(new FeedbackPanel("feedback", rebasibleBranchesForm).hideAfter(Duration.seconds(5)));
		
		rebasibleBranchesForm.add(PropertyContext.editModel("editor", new LoadableDetachableModel<Serializable>() {

			@Override
			public Serializable load() {
				return getRepository().getIntegrationSetting();
			}

		}, "rebasibleBranches"));
		
		rebasibleBranchesForm.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				Gitop.getInstance(RepositoryManager.class).save(getRepository());
				success("Rebasible branches have been updated.");
				target.add(form);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				
				error("Fix errors below.");
				target.add(form);
			}
			
		});
		
		WebMarkupContainer downstreamStrategiesContainer = new WebMarkupContainer("downstreamStrategiesContainer");
		add(downstreamStrategiesContainer);
		Form<?> downstreamStrategiesForm = new Form<Void>("form");
		downstreamStrategiesContainer.add(downstreamStrategiesForm);

		downstreamStrategiesForm.add(new FeedbackPanel("feedback", downstreamStrategiesForm).hideAfter(Duration.seconds(5)));
		
		downstreamStrategiesForm.add(PropertyContext.editModel("strategiesEditor", new LoadableDetachableModel<Serializable>() {

			@Override
			public Serializable load() {
				return getRepository().getIntegrationSetting();
			}

		}, "downstreamStrategies"));
		
		downstreamStrategiesForm.add(BeanContext.editModel("defaultStrategyEditor", new IModel<Serializable>() {

			@Override
			public void detach() {
			}

			@Override
			public Serializable getObject() {
				return getRepository().getIntegrationSetting().getDefaultDownstreamStrategy();
			}

			@Override
			public void setObject(Serializable object) {
				getRepository().getIntegrationSetting().setDefaultDownstreamStrategy((IntegrationStrategy) object);
			}
			
		}));
		
		downstreamStrategiesForm.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				Gitop.getInstance(RepositoryManager.class).save(getRepository());
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

		WebMarkupContainer upstreamStrategiesContainer = new WebMarkupContainer("upstreamStrategiesContainer");
		add(upstreamStrategiesContainer);
		Form<?> upstreamStrategiesForm = new Form<Void>("form");
		upstreamStrategiesContainer.add(upstreamStrategiesForm);

		upstreamStrategiesForm.add(new FeedbackPanel("feedback", upstreamStrategiesForm).hideAfter(Duration.seconds(5)));
		
		upstreamStrategiesForm.add(PropertyContext.editModel("strategiesEditor", new LoadableDetachableModel<Serializable>() {

			@Override
			public Serializable load() {
				return getRepository().getIntegrationSetting();
			}

		}, "upstreamStrategies"));
		
		upstreamStrategiesForm.add(BeanContext.editModel("defaultStrategyEditor", new IModel<Serializable>() {

			@Override
			public Serializable getObject() {
				return getRepository().getIntegrationSetting().getDefaultUpstreamStrategy();			
			}

			@Override
			public void setObject(Serializable object) {
				getRepository().getIntegrationSetting().setDefaultUpstreamStrategy((IntegrationStrategy) object);				
			}

			@Override
			public void detach() {
			}
			
		}));

		upstreamStrategiesForm.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				Gitop.getInstance(RepositoryManager.class).save(getRepository());
				success("Upstream integration strategies have been updated.");
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

}
