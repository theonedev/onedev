package com.pmease.gitop.web.page.repository.settings;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.commons.wicket.editable.PropertyContext;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.RepositoryManager;

@SuppressWarnings("serial")
public class IntegrationSettingPage extends AbstractRepositorySettingPage {

	public IntegrationSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer container = new WebMarkupContainer("rebasibleBranchesContainer");
		add(container);
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Gitop.getInstance(RepositoryManager.class).save(getRepository());
				success("Rebasible branches have been updated.");
			}

			@Override
			protected void onError() {
				super.onError();
				
				error("Fix errors below.");
			}
			
		};
		container.add(form);

		form.add(new FeedbackPanel("feedback", form));
		
		form.add(PropertyContext.editModel("editor", new LoadableDetachableModel<Serializable>() {

			@Override
			public Serializable load() {
				return getRepository().getIntegrationSetting();
			}

		}, "rebasibleBranches"));
		
		container = new WebMarkupContainer("downstreamStrategiesContainer");
		add(container);
		form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				Gitop.getInstance(RepositoryManager.class).save(getRepository());
				success("Downstream integration strategies have been updated.");
			}

			@Override
			protected void onError() {
				super.onError();
				
				error("Fix errors below.");
			}
			
		};
		container.add(form);

		form.add(new FeedbackPanel("feedback", form));
		
		form.add(PropertyContext.editModel("editor", new LoadableDetachableModel<Serializable>() {

			@Override
			public Serializable load() {
				return getRepository().getIntegrationSetting();
			}

		}, "downstreamStrategies"));
		
		form.add(new AjaxSubmitLink("update") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				target.add(form);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				
				target.add(form);
			}
			
		});

		container = new WebMarkupContainer("upstreamStrategiesContainer");
		add(container);
	}

	@Override
	protected String getPageTitle() {
		return "Integration Setting - " + getRepository();
	}

}
