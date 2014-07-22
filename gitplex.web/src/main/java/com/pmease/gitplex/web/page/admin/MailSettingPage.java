package com.pmease.gitplex.web.page.admin;

import com.pmease.gitplex.core.GitPlex;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;

import com.pmease.commons.wicket.component.feedback.FeedbackPanel;
import com.pmease.commons.wicket.editable.BeanContext;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.setting.MailSetting;

@SuppressWarnings("serial")
public class MailSettingPage extends AdministrationPage {

	private MailSetting mailSetting;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		mailSetting = GitPlex.getInstance(ConfigManager.class).getMailSetting();
		if (mailSetting == null)
			mailSetting = new MailSetting();

		Form<?> form = new Form<Void>("form"){

			@Override
			protected void onSubmit() {
				GitPlex.getInstance(ConfigManager.class).saveMailSetting(mailSetting);
				success("Mail setting has been updated");
			}

			@Override
			protected void onError() {
				error("Fix errors below");
			}
			
		}; 
		form.add(BeanContext.editBean("editor", mailSetting));
		form.add(new FeedbackPanel("feedback", form));
		form.add(new AjaxSubmitLink("update") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				target.add(MailSettingPage.this);
			}
			
		});
		
		add(form);
	}

	@Override
	protected String getPageTitle() {
		return "Administration - Mail Server";
	}
}
