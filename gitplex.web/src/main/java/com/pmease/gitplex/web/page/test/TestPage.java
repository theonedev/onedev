package com.pmease.gitplex.web.page.test;

import javax.validation.Validator;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.Link;

import com.pmease.commons.wicket.page.CommonPage;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.setting.BackupSetting;

@SuppressWarnings("serial")
public class TestPage extends CommonPage {

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Link<Void>("test") {

			@Override
			public void onClick() {
				BackupSetting backupSetting = new BackupSetting();
				backupSetting.setSchedule("0 * * * ?");
				backupSetting.setFolder("w:\\temp");
				System.out.println(GitPlex.getInstance(Validator.class).validate(backupSetting).size());
			}
			
		});
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	}

}
