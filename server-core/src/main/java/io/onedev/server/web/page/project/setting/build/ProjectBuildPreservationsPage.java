package io.onedev.server.web.page.project.setting.build;

import java.util.List;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.support.build.BuildPreservation;
import io.onedev.server.web.component.build.perservation.BuildPreservationListPanel;
import io.onedev.server.web.component.build.perservation.BuildPreservationsBean;
import io.onedev.server.web.component.link.SettingInOwnerLink;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.admin.user.buildsetting.UserBuildPreservationsPage;
import io.onedev.server.web.page.my.buildsetting.MyBuildPreservationsPage;

@SuppressWarnings("serial")
public class ProjectBuildPreservationsPage extends ProjectBuildSettingPage {

	public ProjectBuildPreservationsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		BuildPreservationsBean bean = new BuildPreservationsBean();
		bean.setBuildPreservations(getProject().getBuildSetting().getBuildPreservations());
		add(new BuildPreservationListPanel("projectSpecificBuildPreservations", bean) {
			
			@Override
			protected void onSaved(List<BuildPreservation> buildPreservations) {
				getProject().getBuildSetting().setBuildPreservations(buildPreservations);
				OneDev.getInstance(ProjectManager.class).save(getProject());
				setResponsePage(ProjectBuildPreservationsPage.class, 
						ProjectBuildPreservationsPage.paramsOf(getProject()));
			}
			
		});
		
		add(PropertyContext.view("inheritedBuildPreservations", 
				getProject().getOwner().getBuildSetting(), "buildPreservations"));

		add(new SettingInOwnerLink("owner", projectModel, UserBuildPreservationsPage.class, 
				MyBuildPreservationsPage.class));
		
	}

}
