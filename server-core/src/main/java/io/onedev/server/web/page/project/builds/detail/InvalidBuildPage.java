package io.onedev.server.web.page.project.builds.detail;

import javax.persistence.EntityNotFoundException;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.model.Build;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.builds.ProjectBuildsPage;
import io.onedev.server.web.util.ConfirmOnClick;

@SuppressWarnings("serial")
public class InvalidBuildPage extends ProjectPage {

	public static final String PARAM_BUILD = "build";
	
	private IModel<Build> buildModel;
	
	public InvalidBuildPage(PageParameters params) {
		super(params);
		
		buildModel = new LoadableDetachableModel<Build>() {

			@Override
			protected Build load() {
				Long buildNumber = params.get(PARAM_BUILD).toLong();
				Build build = OneDev.getInstance(BuildManager.class).find(getProject(), buildNumber);
				if (build == null)
					throw new EntityNotFoundException("Unable to find build #" + buildNumber + " in project " + getProject());
				Preconditions.checkState(!build.isValid());
				return build;
			}

		};
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Link<Void>("delete") {

			@Override
			public void onClick() {
				OneDev.getInstance(BuildManager.class).delete(getBuild());
				setResponsePage(ProjectBuildsPage.class, ProjectBuildsPage.paramsOf(getProject()));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(SecurityUtils.canManage(getBuild()));
			}
			
		}.add(new ConfirmOnClick("Do you really want to delete build #" + getBuild().getNumber() + "?")));
	}

	public static PageParameters paramsOf(Build build) {
		PageParameters params = ProjectPage.paramsOf(build.getProject());
		params.add(PARAM_BUILD, build.getNumber());
		return params;
	}
	
	private Build getBuild() {
		return buildModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		buildModel.detach();
		super.onDetach();
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.canAccess(getBuild());
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new InvalidBuildResourceReference()));
	}

}
