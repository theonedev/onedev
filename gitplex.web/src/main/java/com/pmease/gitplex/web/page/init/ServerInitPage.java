package com.pmease.gitplex.web.page.init;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.util.init.InitStage;
import com.pmease.commons.util.init.ManualConfig;
import com.pmease.commons.wicket.component.wizard.ManualConfigStep;
import com.pmease.commons.wicket.component.wizard.Wizard;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.web.page.base.BasePage;

@SuppressWarnings("serial")
public class ServerInitPage extends BasePage {

	private InitStage initStage;
	
	public ServerInitPage() {
		initStage = GitPlex.getInstance().getInitStage();
		if (initStage == null) {
			continueToOriginalDestination();
			
			throw new RestartResponseException(getApplication().getHomePage());
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("title", initStage.getMessage()));
		
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
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(ServerInitPage.class, "server-init.css")));
	}

	@Override
	protected int getPageRefreshInterval() {
		if (initStage.getManualConfigs().isEmpty())
			return 1;
		else
			return 0;
	}

}
