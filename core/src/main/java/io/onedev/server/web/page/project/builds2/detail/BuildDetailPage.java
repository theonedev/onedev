package io.onedev.server.web.page.project.builds2.detail;

import javax.persistence.EntityNotFoundException;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.Build2Manager;
import io.onedev.server.model.Build2;
import io.onedev.server.web.component.build.log.BuildLogPanel;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public class BuildDetailPage extends ProjectPage {

	public static final String PARAM_BUILD = "build";
	
	private final IModel<Build2> buildModel;
	
	public BuildDetailPage(PageParameters params) {
		super(params);
		
		buildModel = new LoadableDetachableModel<Build2>() {

			@Override
			protected Build2 load() {
				Long buildNumber = params.get(PARAM_BUILD).toLong();
				Build2 build = OneDev.getInstance(Build2Manager.class).find(getProject(), buildNumber);
				if (build == null)
					throw new EntityNotFoundException("Unable to find build #" + buildNumber + " in project " + getProject());
				return build;
			}

		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	
		add(new Label("number", "#" + getBuild().getNumber()));
		add(new BuildLogPanel("log", buildModel));
	}
	
	private Build2 getBuild() {
		return buildModel.getObject();
	}

	@Override
	protected void onDetach() {
		buildModel.detach();
		super.onDetach();
	}

	public static PageParameters paramsOf(Build2 build) {
		PageParameters params = ProjectPage.paramsOf(build.getProject());
		params.add(PARAM_BUILD, build.getNumber());
		return params;
	}
	
}
