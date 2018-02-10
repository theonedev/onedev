package com.turbodev.server.web.page.init;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import com.turbodev.utils.init.InitStage;
import com.turbodev.utils.init.ManualConfig;
import com.turbodev.utils.init.Skippable;
import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.UserManager;
import com.turbodev.server.model.User;
import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.web.WebSession;
import com.turbodev.server.web.component.wizard.ManualConfigStep;
import com.turbodev.server.web.component.wizard.Wizard;
import com.turbodev.server.web.page.base.BasePage;
import com.turbodev.server.web.page.dashboard.DashboardPage;

@SuppressWarnings("serial")
public class ServerInitPage extends BasePage {

	private InitStage initStage;
	
	public ServerInitPage() {
		InitStage initStage = TurboDev.getInstance().getInitStage();
		if (initStage != null) {
			if (!initStage.getManualConfigs().isEmpty()) {
				List<ManualConfig> clonedConfigs = new ArrayList<ManualConfig>();
				for (ManualConfig each: initStage.getManualConfigs())
					clonedConfigs.add(SerializationUtils.clone(each));
				
				final ManualConfig lastConfig = clonedConfigs.remove(clonedConfigs.size()-1);
				
				clonedConfigs.add(new ManualConfig(lastConfig.getMessage(), lastConfig.getSetting()) {
		
					@Override
					public Skippable getSkippable() {
						final Skippable skippable = lastConfig.getSkippable();
						if (skippable != null) {
							return new Skippable() {

								@Override
								public void skip() {
									skippable.skip();
									InitStage initStage = TurboDev.getInstance().getInitStage();
									if (initStage != null)
										initStage.finished();
								}
								
							};
						} else {
							return null;
						}
					}
		
					@Override
					public void complete() {
						lastConfig.complete();
						InitStage initStage = TurboDev.getInstance().getInitStage();
						if (initStage != null)
							initStage.finished();
					}
					
				});
				
				this.initStage = new InitStage(initStage.getMessage(), clonedConfigs);
			} else {
				this.initStage = new InitStage(initStage.getMessage());
			}
		} else {
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
					WebSession.get().logout();
					User root = TurboDev.getInstance(UserManager.class).getRoot();
					SecurityUtils.getSubject().runAs(root.getPrincipals());
					throw new RestartResponseException(DashboardPage.class);
				}
				
			});
		} else {
			add(new WebMarkupContainer("wizard").setVisible(false));
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ServerInitResourceReference()));
		
		if (initStage.getManualConfigs().isEmpty())
			response.render(OnDomReadyHeaderItem.forScript("$('#server-init').addClass('inited');"));
	}

	@Override
	protected int getPageRefreshInterval() {
		if (initStage.getManualConfigs().isEmpty())
			return 1;
		else
			return 0;
	}

}
