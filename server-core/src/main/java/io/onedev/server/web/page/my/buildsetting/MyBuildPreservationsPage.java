package io.onedev.server.web.page.my.buildsetting;

import java.util.List;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.support.build.BuildPreservation;
import io.onedev.server.web.component.build.perservation.BuildPreservationListPanel;
import io.onedev.server.web.component.build.perservation.BuildPreservationsBean;

@SuppressWarnings("serial")
public class MyBuildPreservationsPage extends MyBuildSettingPage {

	public MyBuildPreservationsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BuildPreservationsBean bean = new BuildPreservationsBean();
		bean.setBuildPreservations(getBuildSetting().getBuildPreservations());
		add(new BuildPreservationListPanel("buildPreservations", bean) {
			
			@Override
			protected void onSaved(List<BuildPreservation> buildPreservations) {
				getBuildSetting().setBuildPreservations(buildPreservations);
				OneDev.getInstance(UserManager.class).save(getLoginUser());
				setResponsePage(MyBuildPreservationsPage.class);
			}
			
		});
	}

}
