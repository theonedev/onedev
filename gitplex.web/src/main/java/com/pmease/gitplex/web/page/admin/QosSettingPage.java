package com.pmease.gitplex.web.page.admin;

import org.apache.wicket.markup.html.form.Form;

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

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				GitPlex.getInstance(ConfigManager.class).saveQosSetting(qosSetting);
				getSession().success("QoS settings has been updated");
			}

			@Override
			protected void onError() {
				super.onError();
				getSession().error("Fix errors below");
			}
			
		};
		form.setOutputMarkupId(true);
		form.add(BeanContext.editBean("editor", qosSetting));
		
		sidebar.add(form);
	}

	@Override
	protected String getPageTitle() {
		return "Administration - QoS Settings";
	}
}
