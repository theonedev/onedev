package io.onedev.server.web.page.admin.issuesetting;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.web.component.issue.statetransition.StateTransitionListPanel;
import io.onedev.server.web.component.issue.statetransition.UseDefaultListener;

@SuppressWarnings("serial")
public class StateTransitionsPage extends IssueSettingPage {

	public StateTransitionsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new StateTransitionListPanel("content", getSetting().getTransitionSpecs()) {

			@Override
			protected void onChanged(AjaxRequestTarget target) {
				OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
			}

			@Override
			protected UseDefaultListener getUseDefaultListener() {
				return null;
			}
			
		});
		
	}
	
}
