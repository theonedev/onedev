package io.onedev.server.web.page.project.setting.issue;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.issue.TransitionSpec;
import io.onedev.server.web.component.issue.statetransition.StateTransitionListPanel;
import io.onedev.server.web.component.issue.statetransition.UseDefaultListener;

@SuppressWarnings("serial")
public class StateTransitionsPage extends ProjectIssueSettingPage {

	private List<TransitionSpec> transitions;
	
	public StateTransitionsPage(PageParameters params) {
		super(params);
		transitions = getProjectSetting().getTransitionSpecs(true);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new StateTransitionListPanel("transitions", transitions) {

			@Override
			protected void onChanged(AjaxRequestTarget target) {
				getProjectSetting().setTransitionSpecs(transitions);
				OneDev.getInstance(ProjectManager.class).save(getProject());
			}

			@Override
			protected UseDefaultListener getUseDefaultListener() {
				if (getProjectSetting().getTransitionSpecs(false) != null) {
					return new UseDefaultListener() {
						
						@Override
						public void onUseDefault() {
							getProjectSetting().setTransitionSpecs(null);
							OneDev.getInstance(ProjectManager.class).save(getProject());
							setResponsePage(StateTransitionsPage.class, StateTransitionsPage.paramsOf(getProject()));
						}
						
					};
				} else {
					return null;
				}
			}
			
		});
	}
	
}
