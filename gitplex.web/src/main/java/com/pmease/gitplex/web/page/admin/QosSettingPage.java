package com.pmease.gitplex.web.page.admin;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;

import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.setting.QosSetting;

@SuppressWarnings("serial")
public class QosSettingPage extends AdministrationPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		final QosSetting qosSetting = GitPlex.getInstance(ConfigManager.class).getQosSetting();

		Form<?> form = new Form<Void>("form"); 
		form.setOutputMarkupId(true);
		form.add(BeanContext.editBean("editor", qosSetting));
		form.add(new FeedbackPanel("feedback", form));
		form.add(new AjaxSubmitLink("update") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);

				GitPlex.getInstance(ConfigManager.class).saveQosSetting(qosSetting);
				success("QoS settings has been updated");
				
				target.add(form);
			}
			
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				error("Fix errors below");
				target.add(form);
			}
			
		});
		
		sidebar.add(form);
	}

	@Override
	protected String getPageTitle() {
		return "Administration - QoS Settings";
	}
}
