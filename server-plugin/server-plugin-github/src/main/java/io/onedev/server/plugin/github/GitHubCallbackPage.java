package io.onedev.server.plugin.github;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.page.admin.sso.SsoProcessPage;
import io.onedev.server.web.page.base.BasePage;
import io.onedev.server.web.page.project.imports.ProjectImportPage;

/**
 * The landing page for GitHub oauth callbacks. Request will be dispatched to other pages for 
 * further processing based on state param
 * 
 * @author robin
 *
 */
@SuppressWarnings("serial")
public class GitHubCallbackPage extends BasePage {

	public static final String MOUNT_PATH = "github-callback";
	
	private static final String PARAM_STATE = "state";
	
	public GitHubCallbackPage(PageParameters params) {
		super(params);
		
		PageProvider pageProvider;
		
		String state = params.get(PARAM_STATE).toString();
		if (state.startsWith(GitHubImportPanel.STATE_PREFIX)) {
			ProjectImportPage.addParams(params, ProjectImportPage.STAGE_CALLBACK, GitHubImporter.NAME);
			pageProvider = new PageProvider(ProjectImportPage.class, params);
		} else {
			SsoProcessPage.addParams(params, SsoProcessPage.STAGE_CALLBACK, GitHubConnector.NAME); 
			pageProvider = new PageProvider(SsoProcessPage.class, params);
		}
		throw new RestartResponseException(pageProvider, RedirectPolicy.NEVER_REDIRECT);
	}

	public static String getUrl() {
		SettingManager settingManager = OneDev.getInstance(SettingManager.class);
		String serverUrl = settingManager.getSystemSetting().getServerUrl();
		return serverUrl + "/" + GitHubCallbackPage.MOUNT_PATH;
	}

}
