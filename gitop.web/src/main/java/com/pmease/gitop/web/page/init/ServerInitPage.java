package com.pmease.gitop.web.page.init;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import com.pmease.commons.util.init.InitStage;
import com.pmease.commons.util.init.ManualConfig;
import com.pmease.commons.wicket.component.wizard.ManualConfigStep;
import com.pmease.commons.wicket.component.wizard.Wizard;
import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.web.page.BasePage;

@SuppressWarnings("serial")
public class ServerInitPage extends BasePage {

	private InitStage initStage;
	
	public ServerInitPage() {
		initStage = Gitop.getInstance().getInitStage();
		if (initStage == null) {
			redirectToOriginal();
			
			redirect(getApplication().getHomePage());
		}
	}

	@Override
	protected void onPageInitialize() {
		super.onPageInitialize();

		add(new Label("message", initStage.getMessage()));
		
		if (!initStage.getManualConfigs().isEmpty()) {
			List<ManualConfigStep> configSteps = new ArrayList<ManualConfigStep>();
			for (ManualConfig each: initStage.getManualConfigs())
				configSteps.add(new ManualConfigStep(each));
			add(new Wizard("wizard", configSteps) {

				@Override
				protected void finished() {
					setResponsePage(ServerInitPage.class);
				}
				
			});
		} else {
			add(new WebMarkupContainer("wizard").setVisible(false));
		}
	}
	
	@Override
	protected String getPageTitle() {
		return "Server Initialization";
	}

	@Override
	protected int getPageRefreshInterval() {
		if (initStage.getManualConfigs().isEmpty())
			return 1;
		else
			return 0;
	}

}
