package io.onedev.server.web.page.simple.serverinit;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.init.InitStage;
import io.onedev.server.util.init.ManualConfig;
import io.onedev.server.util.init.Skippable;
import io.onedev.server.web.WebSession;
import io.onedev.server.web.component.wizard.ManualConfigStep;
import io.onedev.server.web.component.wizard.Wizard;
import io.onedev.server.web.page.project.ProjectListPage;
import io.onedev.server.web.page.simple.SimplePage;

@SuppressWarnings("serial")
public class ServerInitPage extends SimplePage {

	private InitStage initStage;
	
	public ServerInitPage(PageParameters params) {
		super(params);
		
		InitStage initStage = OneDev.getInstance().getInitStage();
		if (initStage != null) {
			if (!initStage.getManualConfigs().isEmpty()) {
				List<ManualConfig> clonedConfigs = new ArrayList<ManualConfig>();
				for (ManualConfig each: initStage.getManualConfigs())
					clonedConfigs.add(SerializationUtils.clone(each));
				
				final ManualConfig lastConfig = clonedConfigs.remove(clonedConfigs.size()-1);
				
				clonedConfigs.add(new ManualConfig(lastConfig.getTitle(), lastConfig.getDescription(), 
						lastConfig.getSetting(), lastConfig.getExcludeProperties()) {
		
					@Override
					public Skippable getSkippable() {
						final Skippable skippable = lastConfig.getSkippable();
						if (skippable != null) {
							return new Skippable() {

								@Override
								public void skip() {
									skippable.skip();
									InitStage initStage = OneDev.getInstance().getInitStage();
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
						InitStage initStage = OneDev.getInstance().getInitStage();
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

		if (!initStage.getManualConfigs().isEmpty()) {
			List<ManualConfigStep> configSteps = new ArrayList<ManualConfigStep>();
			for (ManualConfig each: initStage.getManualConfigs())
				configSteps.add(new ManualConfigStep(each));
			add(new Wizard("wizard", configSteps) {

				@Override
				protected void finished() {
					WebSession.get().logout();
					User root = OneDev.getInstance(UserManager.class).getRoot();
					SecurityUtils.getSubject().runAs(root.getPrincipals());
					throw new RestartResponseException(ProjectListPage.class);
				}
				
			});
		} else {
			add(new WebMarkupContainer("wizard").setVisible(false));
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		if (initStage.getManualConfigs().isEmpty())
			response.render(OnDomReadyHeaderItem.forScript("$('.server-init').addClass('inited');"));
	}

	@Override
	protected int getPageRefreshInterval() {
		if (initStage.getManualConfigs().isEmpty())
			return 1;
		else
			return 0;
	}

	@Override
	protected String getTitle() {
		return initStage.getMessage();
	}

	@Override
	protected String getSubTitle() {
		return null;
	}

}
